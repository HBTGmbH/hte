/*
 * (C) Copyright 2018 Kai Burjack

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.

 */
package de.hbt.hte.rt;

import java.lang.invoke.*;
import java.lang.invoke.MethodHandles.*;
import java.lang.reflect.*;
import java.util.*;

class SymbolRefBootstrap {

    private static final MethodHandle CHECK_CLASS;
    private static final MethodHandle LINK;
    private static final MethodHandle MAP_LOOKUP;
    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            CHECK_CLASS = lookup.findStatic(SymbolRefBootstrap.class, "checkClass",
                    MethodType.methodType(boolean.class, Class.class, Object.class));
            LINK = lookup.findStatic(SymbolRefBootstrap.class, "link",
                    MethodType.methodType(Object.class, InliningCacheCallSite.class, Object.class));
            MAP_LOOKUP = lookup.findStatic(SymbolRefBootstrap.class, "mapLookup", MethodType.methodType(Object.class,
                    Map.class, MethodHandles.Lookup.class, String.class, int.class));
        } catch (Exception e) {
            throw (AssertionError) new AssertionError("Could not initialize AttributeBootstrap").initCause(e);
        }
    }

    private static final class InliningCacheCallSite extends MutableCallSite {
        private final MethodHandles.Lookup lookup;
        private final String name;
        private final int flags;

        InliningCacheCallSite(Lookup lookup, String name, MethodType type, int flags) {
            super(type);
            this.lookup = lookup;
            this.name = name;
            this.flags = flags;
        }
    }

    public static CallSite bootstrapDynamic(MethodHandles.Lookup lookup, String name, MethodType type, int flags) {
        InliningCacheCallSite callSite = new InliningCacheCallSite(lookup, name, type, flags);
        MethodHandle fallback = LINK.bindTo(callSite);
        fallback = fallback.asType(type);
        callSite.setTarget(fallback);
        return callSite;
    }

    @SuppressWarnings("unused")
    private static boolean checkClass(Class<?> clazz, Object receiver) {
        return clazz.isInstance(receiver);
    }

    @SuppressWarnings("unused")
    private static Object mapLookup(Map<?, ?> map, MethodHandles.Lookup lookup, String key, int flags) {
        if (!map.containsKey(key)) {
            if (Flags.shouldThrowURE(flags)) {
                throw new UndefinedRaisedException("No such key in map: " + key);
            } else if (Flags.shouldThrowNPE(flags)) {
                throw new NullPointerException("No such key in map: " + key);
            } else {
                return null;
            }
        } else {
            return map.get(key);
        }
    }

    private static String firstUpper(String str) {
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    private static Method findPojoGetter(MethodHandles.Lookup lookup, Class<?> receiver, String name, int flags) {
        String getterName = "get" + firstUpper(name);
        String isserName = "is" + firstUpper(name);
        try {
            Method getterMethod = ClassUtils.getOverriddenMethod(receiver.getMethod(getterName));
            if (getterMethod.getReturnType() != void.class && getterMethod.getParameterTypes().length == 0) {
                return getterMethod;
            } else {
                throw new NoSuchMethodException("Getter has unexpected signature: " + getterMethod.toString());
            }
        } catch (NoSuchMethodException e) {
            try {
                Method isserMethod = receiver.getMethod(isserName);
                if ((isserMethod.getReturnType() == boolean.class || isserMethod.getReturnType() == Boolean.class)
                        && isserMethod.getParameterTypes().length == 0) {
                    return isserMethod;
                } else {
                    throw new NoSuchMethodException("Getter has unexpected signature: " + isserMethod.toString());
                }
            } catch (NoSuchMethodException e2) {
                if ((flags & 3) == Flags.FLAG_UNRESOLVED_SHOULD_GENERATE_NULL_OR_THROW_NPE) {
                    return null;
                } else if ((flags & 3) == Flags.FLAG_UNRESOLVED_SHOULD_THROW_NPE) {
                    throw new NullPointerException("No such attribute " + receiver.getName() + "::" + name);
                } else {
                    throw new UndefinedRaisedException("No such attribute " + receiver.getName() + "::" + name);
                }
            }
        }
    }

    @SuppressWarnings("unused")
    private static Object link(InliningCacheCallSite callSite, Object receiver) throws Throwable {
        MethodType type = callSite.type();
        Class<?> receiverClass = receiver.getClass();
        Class<?> testClass;
        MethodHandle target;
        if (Map.class.isAssignableFrom(receiverClass)) {
            /*
             * Generate static invoke to AttributeBootstrap#mapLookup method.
             */
            target = MethodHandles.insertArguments(MAP_LOOKUP, 1, callSite.lookup, callSite.name, callSite.flags);
            testClass = Map.class;
        } else if (Collection.class.isAssignableFrom(receiverClass)) {
            if ("size".equals(callSite.name))
                target = callSite.lookup.findVirtual(Collection.class, "size", MethodType.methodType(int.class));
            else
                throw new UndefinedRaisedException("No such Collection property: " + callSite.name);
            target = target.asType(target.type().changeReturnType(Object.class));
            testClass = Collection.class;
        } else {
            /* Find an appropriate getter on the receiver class. */
            Method pojoGetter = findPojoGetter(callSite.lookup, receiverClass, callSite.name, callSite.flags);
            if (pojoGetter == null) {
                testClass = receiverClass;
                /* Generate null or throw NPE. */
                if (callSite.type().returnType().isPrimitive()) {
                    /* Generate NPE */
                    target = generateNPE(callSite.name, receiverClass);
                } else {
                    /* Generate null */
                    target = MethodHandles.dropArguments(MethodHandles.constant(Object.class, null), 0, Object.class);
                }
            } else {
                testClass = pojoGetter.getDeclaringClass();
                target = callSite.lookup.unreflect(pojoGetter);
            }
        }
        /* Do any necessary return type conversions. */
        target = target.asType(type);
        /* Bind the first argument of 'checkClass' to the receiver type. */
        MethodHandle test = CHECK_CLASS.bindTo(testClass);
        test = test.asType(test.type().changeParameterType(0, type.parameterType(0)));
        callSite.setTarget(MethodHandles.guardWithTest(test, target, callSite.getTarget()));
        return target.invokeWithArguments(receiver);
    }

    private static MethodHandle generateNPE(String attributeName, Class<?> receiverType) {
        return MethodHandles.dropArguments(
                MethodHandles.insertArguments(MethodHandles.throwException(void.class, NullPointerException.class), 0,
                        new NullPointerException("unresolved attribute: " + receiverType + "::" + attributeName)),
                0, Object.class);
    }

}
