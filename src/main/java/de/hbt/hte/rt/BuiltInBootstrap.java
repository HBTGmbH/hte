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

class BuiltInBootstrap {

    private static final MethodHandle CHECK_CLASSES;
    private static final MethodHandle LINK;
    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            CHECK_CLASSES = lookup.findStatic(BuiltInBootstrap.class, "checkClasses",
                    MethodType.methodType(boolean.class, Class[].class, Object[].class));
            LINK = lookup.findStatic(BuiltInBootstrap.class, "link", MethodType.methodType(Object.class,
                    InliningCacheCallSite.class, Environment.class, Object[].class));
        } catch (Exception e) {
            throw (AssertionError) new AssertionError("Could not initialize OperatorBootstrap").initCause(e);
        }
    }

    private static final class InliningCacheCallSite extends MutableCallSite {
        private final MethodHandles.Lookup lookup;
        private final String name;

        InliningCacheCallSite(Lookup lookup, String name, MethodType type) {
            super(type);
            this.lookup = lookup;
            this.name = name;
        }
    }

    private static CallSite createCallSite(MethodHandles.Lookup lookup, String name, MethodType type,
            MethodHandle linkMethod) {
        InliningCacheCallSite callSite = new InliningCacheCallSite(lookup, name, type);
        linkMethod = linkMethod.bindTo(callSite).asType(type);
        callSite.setTarget(linkMethod);
        return callSite;
    }

    public static CallSite bootstrap(MethodHandles.Lookup lookup, String name, MethodType type, int constant)
            throws Throwable {
        return createCallSite(lookup, name, type, LINK.asCollector(Object[].class, type.parameterCount() - 1));
    }

    @SuppressWarnings("unused")
    private static boolean checkClasses(Class<?>[] classes, Object[] operands) {
        for (int i = 0; i < classes.length; i++) {
            if (!classes[i].isInstance(operands[i]))
                return false;
        }
        return true;
    }

    @SuppressWarnings("unused")
    private static Object link(InliningCacheCallSite callSite, Environment env, Object... thizAndOperands)
            throws Throwable {
        Class<?>[] classes = new Class<?>[thizAndOperands.length];
        for (int i = 0; i < thizAndOperands.length; i++)
            classes[i] = thizAndOperands[i].getClass();
        MethodHandle target;
        target = callSite.lookup.unreflect(env.getOperatorLookup().lookup(callSite.name, classes).m);
        target = target.asSpreader(Object[].class, thizAndOperands.length);
        target = MethodHandles.dropArguments(target, 0, Environment.class);
        target = target.asType(target.type().changeReturnType(Object.class));
        MethodHandle fallback = callSite.getTarget();
        fallback = fallback.asSpreader(Object[].class, thizAndOperands.length);
        MethodHandle test = CHECK_CLASSES.bindTo(classes);
        test = MethodHandles.dropArguments(test, 0, Environment.class);
        callSite.setTarget(MethodHandles.guardWithTest(test, target, fallback).asCollector(Object[].class,
                thizAndOperands.length));
        return target.invokeWithArguments(env, thizAndOperands);
    }

}
