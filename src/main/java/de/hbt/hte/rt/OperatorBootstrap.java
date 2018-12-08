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

import de.hbt.hte.ast.*;

class OperatorBootstrap {

    private static final MethodHandle CHECK_1_CLASS;
    private static final MethodHandle CHECK_2_CLASSES;
    private static final MethodHandle LINK_UNARY, LINK_BINARY;
    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            CHECK_1_CLASS = lookup.findStatic(OperatorBootstrap.class, "check1Class",
                    MethodType.methodType(boolean.class, Class.class, Object.class));
            CHECK_2_CLASSES = lookup.findStatic(OperatorBootstrap.class, "check2Classes",
                    MethodType.methodType(boolean.class, Class.class, Class.class, Object.class, Object.class));
            LINK_UNARY = lookup.findStatic(OperatorBootstrap.class, "linkUnary",
                    MethodType.methodType(Object.class, InliningCacheCallSite.class, Environment.class, Object.class));
            LINK_BINARY = lookup.findStatic(OperatorBootstrap.class, "linkBinary", MethodType.methodType(Object.class,
                    InliningCacheCallSite.class, Environment.class, Object.class, Object.class));
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

    public static CallSite bootstrapBinary(MethodHandles.Lookup lookup, String name, MethodType type, int constant)
            throws Throwable {
        return createCallSite(lookup, name, type, LINK_BINARY);
    }

    public static CallSite bootstrapUnary(MethodHandles.Lookup lookup, String name, MethodType type, int constant) {
        return createCallSite(lookup, name, type, LINK_UNARY);
    }

    @SuppressWarnings("unused")
    private static boolean check1Class(Class<?> clazz1, Object object1) {
        return object1.getClass() == clazz1;
    }

    @SuppressWarnings("unused")
    private static boolean check2Classes(Class<?> clazz1, Class<?> clazz2, Object object1, Object object2) {
        return object1.getClass() == clazz1 && object2.getClass() == clazz2;
    }

    @SuppressWarnings("unused")
    private static Object linkUnary(InliningCacheCallSite callSite, Environment env, Object operand) throws Throwable {
        Class<?> operandClass = operand.getClass();
        MethodHandle target;
        target = callSite.lookup.unreflect(env.getOperatorLookup().lookup(callSite.name, operandClass).m);
        target = MethodHandles.dropArguments(target, 0, Environment.class);
        target = target.asType(callSite.type());
        MethodHandle test = CHECK_1_CLASS.bindTo(operandClass);
        test = MethodHandles.dropArguments(test, 0, Environment.class);
        test = test.asType(test.type().changeParameterType(1, target.type().parameterType(1)));
        callSite.setTarget(MethodHandles.guardWithTest(test, target, callSite.getTarget()));
        return target.invokeWithArguments(env, operand);
    }

    @SuppressWarnings("unused")
    private static Object linkBinary(InliningCacheCallSite callSite, Environment env, Object leftOperand,
            Object rightOperand) throws Throwable {
        Class<?> leftClass = leftOperand.getClass();
        Class<?> rightClass = rightOperand.getClass();
        MethodHandle target;
        target = callSite.lookup.unreflect(
                env.getOperatorLookup().lookup(AstUtils.unescapeOpName(callSite.name), leftClass, rightClass).m);
        target = MethodHandles.dropArguments(target, 0, Environment.class);
        target = target.asType(callSite.type());
        MethodHandle test = CHECK_2_CLASSES.bindTo(leftClass).bindTo(rightClass);
        test = MethodHandles.dropArguments(test, 0, Environment.class);
        test = test.asType(test.type().changeParameterType(1, target.type().parameterType(1)));
        test = test.asType(test.type().changeParameterType(2, target.type().parameterType(2)));
        callSite.setTarget(MethodHandles.guardWithTest(test, target, callSite.getTarget()));
        return target.invokeWithArguments(env, leftOperand, rightOperand);
    }

}
