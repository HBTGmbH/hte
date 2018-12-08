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
package de.hbt.hte.codegen;

import java.lang.invoke.*;
import java.lang.invoke.MethodHandles.*;
import java.lang.reflect.*;

import lombok.experimental.*;

@UtilityClass
class ClassDefineUtils {
    private static final MethodHandle Unsafe_defineAnonymousClass;
    private static final MethodHandle ClassLoader_defineClass;
    private static final MethodHandle Lookup_defineClass;
    private static final MethodHandle MethodHandles_privateLookupIn;
    private static final Lookup lookup = MethodHandles.lookup();

    static {
        MethodHandle Unsafe_defineAnonymousClassMH = null;
        MethodHandle ClassLoader_defineClassMH = null;
        MethodHandle MethodHandles_privateLookupInMH = null;
        MethodHandle Lookup_defineClassMH = null;
        Object unsafe = null;
        /* Try the Java 9+ way with Lookup.defineClass() */
        try {
            Method Lookup_defineClass = MethodHandles.Lookup.class.getDeclaredMethod("defineClass", byte[].class);
            Lookup_defineClassMH = lookup.unreflect(Lookup_defineClass);
            Method MethodHandles_privateLookupInMethod = MethodHandles.class.getDeclaredMethod("privateLookupIn",
                    Class.class, Lookup.class);
            MethodHandles_privateLookupInMH = lookup.unreflect(MethodHandles_privateLookupInMethod);
        } catch (@SuppressWarnings("unused") Exception e) {
            /* No Lookup.defineClass() */
        }
        /* Try the Java 8 way with Unsafe.defineAnonymousClass() */
        try {
            Class<?> unsafeClass = ClassLoader.getSystemClassLoader().loadClass("sun.misc.Unsafe");
            Field theUnsafeField = unsafeClass.getDeclaredField("theUnsafe");
            theUnsafeField.setAccessible(true);
            unsafe = theUnsafeField.get(null);
            Method defineAnonymousClassMethod = unsafeClass.getDeclaredMethod("defineAnonymousClass", Class.class,
                    byte[].class, Object[].class);
            MethodHandle mh = lookup.unreflect(defineAnonymousClassMethod);
            Unsafe_defineAnonymousClassMH = mh.asType(mh.type().changeParameterType(0, Object.class)).bindTo(unsafe);
        } catch (@SuppressWarnings("unused") Exception e) {
            /* No Unsafe.defineAnonymousClass() */
        }
        /* Try the Java 8 way with ClassLoader.defineClass() */
        try {
            if (Lookup_defineClassMH == null) {
                Method defineClassM = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class,
                        int.class, int.class);
                defineClassM.setAccessible(true);
                ClassLoader_defineClassMH = MethodHandles.lookup().unreflect(defineClassM);
            }
        } catch (@SuppressWarnings("unused") Exception e) {
            /* No ClassLoader.defineClass() */
        }
        Unsafe_defineAnonymousClass = Unsafe_defineAnonymousClassMH;
        ClassLoader_defineClass = ClassLoader_defineClassMH;
        Lookup_defineClass = Lookup_defineClassMH;
        MethodHandles_privateLookupIn = MethodHandles_privateLookupInMH;
    }

    static <T> Class<T> defineClass(ClassLoader cl, Class<?> hostClass, String name, byte[] definition,
            Object[] constantPoolPatch) {
        try {
            if (Unsafe_defineAnonymousClass != null && hostClass != null) {
                return (Class<T>) Unsafe_defineAnonymousClass.invokeExact(hostClass, definition, constantPoolPatch);
            }
            if (Lookup_defineClass != null && hostClass != null) {
                return (Class<T>) Lookup_defineClass
                        .invokeExact((Lookup) MethodHandles_privateLookupIn.invokeExact(hostClass, lookup), definition);
            }
            return (Class<T>) ClassLoader_defineClass.invokeExact(cl, name.replace('/', '.'), definition, 0,
                    definition.length);
        } catch (Throwable e) {
            throw new RuntimeException("Could not define class in JVM: " + name, e);
        }
    }

}
