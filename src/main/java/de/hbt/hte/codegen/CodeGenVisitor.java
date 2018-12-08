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

import static org.objectweb.asm.Opcodes.*;

import java.lang.invoke.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.asm.*;

import de.hbt.hte.ast.*;
import de.hbt.hte.rt.*;
import de.hbt.hte.rt.MethodLookup.LookupResult;

public class CodeGenVisitor implements NodeVisitor, ExpressionVisitor {

    private static final int MAX_STRING_CONSTANT_SIZE = 0xFFFF / 3;
    private static final AtomicInteger sequence = new AtomicInteger();
    private static final Type OBJECT_TYPE = Type.getType(Object.class);

    private static final Map<Class<?>, Class<?>> primitiveWrapperMap = new HashMap<Class<?>, Class<?>>();
    static {
        primitiveWrapperMap.put(Boolean.TYPE, Boolean.class);
        primitiveWrapperMap.put(Byte.TYPE, Byte.class);
        primitiveWrapperMap.put(Character.TYPE, Character.class);
        primitiveWrapperMap.put(Short.TYPE, Short.class);
        primitiveWrapperMap.put(Integer.TYPE, Integer.class);
        primitiveWrapperMap.put(Long.TYPE, Long.class);
        primitiveWrapperMap.put(Double.TYPE, Double.class);
        primitiveWrapperMap.put(Float.TYPE, Float.class);
        primitiveWrapperMap.put(Void.TYPE, Void.TYPE);
    }

    private static final Map<Class<?>, Class<?>> wrapperPrimitiveMap = new HashMap<Class<?>, Class<?>>();
    static {
        for (Class<?> primitiveClass : primitiveWrapperMap.keySet()) {
            Class<?> wrapperClass = primitiveWrapperMap.get(primitiveClass);
            if (!primitiveClass.equals(wrapperClass)) {
                wrapperPrimitiveMap.put(wrapperClass, primitiveClass);
            }
        }
    }

    private static class VariableInScope {
        private int local;
        private Type type;
        private Class<?> staticType;

        private VariableInScope(int local, Type type, Class<?> staticType) {
            this.local = local;
            this.type = type;
            this.staticType = staticType;
        }
    }

    private Environment env;
    private ClassWriter cw;
    private MethodVisitor ctor;
    private MethodVisitor mv;
    private String className;
    private int thisLocal = 0;
    private int writerLocal = 1;
    private int ctxLocal = 2;
    private int nextLocal = 3;
    private int nextCtorLocal = 2;
    private Stack<Map<String, VariableInScope>> scopes = new Stack<>();
    private ExpressionVisitor constPropagateVisitor = new ConstPropagateVisitor();

    public CodeGenVisitor(Environment env) {
        this.env = env;
    }

    private int nextLocal(Type t) {
        int local = nextLocal;
        nextLocal += t.getSize();
        return local;
    }

    private VariableInScope searchName(String name) {
        for (int i = scopes.size() - 1; i >= 0; i--)
            if (scopes.get(i).containsKey(name))
                return scopes.get(i).get(name);
        return null;
    }

    public Class<? extends CompiledTemplate> compile(TemplateNode node) {
        cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        className = "de/hbt/hte/rt/CompiledTemplate$" + sequence.getAndIncrement();
        cw.visit(V1_7, ACC_PUBLIC | ACC_SYNTHETIC | ACC_SUPER, className, null, "java/lang/Object",
                new String[] { "de/hbt/hte/rt/CompiledTemplate" });
        cw.visitField(ACC_PRIVATE | ACC_FINAL, "$env", "Lde/hbt/hte/rt/Environment;", null, null);
        ctor = cw.visitMethod(ACC_PUBLIC, "<init>", "(Lde/hbt/hte/rt/Environment;)V", null, null);
        ctor.visitCode();
        ctor.visitVarInsn(ALOAD, thisLocal);
        ctor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        ctor.visitVarInsn(ALOAD, thisLocal);
        ctor.visitVarInsn(ALOAD, 1);
        ctor.visitFieldInsn(PUTFIELD, className, "$env", "Lde/hbt/hte/rt/Environment;");
        MethodVisitor write = cw.visitMethod(ACC_PUBLIC, "write", "(Ljava/io/Writer;)V", null, null);
        write.visitCode();
        write.visitVarInsn(ALOAD, thisLocal);
        write.visitVarInsn(ALOAD, writerLocal);
        write.visitInsn(ACONST_NULL);
        write.visitMethodInsn(INVOKEVIRTUAL, className, "write", "(Ljava/io/Writer;Ljava/lang/Object;)V", false);
        write.visitInsn(RETURN);
        write.visitMaxs(-1, -1);
        write.visitEnd();
        mv = cw.visitMethod(ACC_PUBLIC, "write", "(Ljava/io/Writer;Ljava/lang/Object;)V", null, null);
        mv.visitCode();
        this.visitTemplateNode(node);
        mv.visitInsn(RETURN);
        mv.visitMaxs(-1, -1);
        mv.visitEnd();
        ctor.visitInsn(RETURN);
        ctor.visitMaxs(-1, -1);
        ctor.visitEnd();
        cw.visitEnd();
        byte[] bytes = cw.toByteArray();
        return ClassDefineUtils.defineClass(CompiledTemplate.class.getClassLoader(), CompiledTemplate.class, className,
                bytes, null);
    }

    private void convert(Class<?> from, Class<?> to) {
        if (from == to)
            return;
        if (wrapperPrimitiveMap.containsKey(from) && primitiveWrapperMap.containsKey(to)) {
            if (from == Boolean.class) {
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
            } else if (from == Integer.class) {
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
            } else if (from == Long.class) {
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false);
            } else if (from == Float.class) {
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F", false);
            } else if (from == Double.class) {
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D", false);
            } else {
                throw new IllegalArgumentException();
            }
        } else if (primitiveWrapperMap.containsKey(from) && wrapperPrimitiveMap.containsKey(to)) {
            if (from == boolean.class) {
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
            } else if (from == int.class) {
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
            } else if (from == long.class) {
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
            } else if (from == float.class) {
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
            } else if (from == double.class) {
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
            } else {
                throw new IllegalArgumentException();
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    private Class<?> wrap(Class<?> t) {
        if (!t.isPrimitive())
            return t;
        if (t == boolean.class) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
            return Boolean.class;
        } else if (t == int.class) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
            return Integer.class;
        } else if (t == long.class) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
            return Integer.class;
        } else if (t == float.class) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
            return Integer.class;
        } else if (t == double.class) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
            return Double.class;
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void visitBinaryExpression(BinaryExpression e) {
        String op = e.op.name.image;
        OperatorDescription opdesc = this.env.getProfile().getPredefinedOp(op);
        if (opdesc != null && opdesc.scop == ShortcircuitOp.AND) {
            e.staticType = handleShortcircuit(e, IFEQ, ICONST_1, ICONST_0);
        } else if (opdesc != null && opdesc.scop == ShortcircuitOp.OR) {
            e.staticType = handleShortcircuit(e, IFNE, ICONST_0, ICONST_1);
        } else {
            e.left.accept(this);
            Type leftType = Type.getType(e.left.staticType);
            int leftLocal = nextLocal(leftType);
            mv.visitVarInsn(leftType.getOpcode(ISTORE), leftLocal);
            e.right.accept(this);
            Type rightType = Type.getType(e.right.staticType);
            int rightLocal = nextLocal(rightType);
            mv.visitVarInsn(rightType.getOpcode(ISTORE), rightLocal);
            LookupResult lookup = env.getOperatorLookup().lookup(op, e.left.staticType, e.right.staticType);
            if (lookup.m != null && lookup.isBotton) {
                mv.visitVarInsn(leftType.getOpcode(ILOAD), leftLocal);
                convert(e.left.staticType, lookup.m.getParameterTypes()[0]);
                mv.visitVarInsn(rightType.getOpcode(ILOAD), rightLocal);
                convert(e.right.staticType, lookup.m.getParameterTypes()[1]);
                mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(lookup.m.getDeclaringClass()), lookup.m.getName(),
                        Type.getMethodDescriptor(lookup.m), false);
                e.staticType = lookup.m.getReturnType();
                return;
            }
            loadEnv();
            mv.visitVarInsn(leftType.getOpcode(ILOAD), leftLocal);
            mv.visitVarInsn(rightType.getOpcode(ILOAD), rightLocal);
            mv.visitInvokeDynamicInsn(AstUtils.escapeOpName(op),
                    MethodType.methodType(e.staticType, Environment.class, e.left.staticType, e.right.staticType)
                            .toMethodDescriptorString(),
                    new Handle(Opcodes.H_INVOKESTATIC, "de/hbt/hte/rt/OperatorBootstrap", "bootstrapBinary",
                            MethodType.methodType(CallSite.class, MethodHandles.Lookup.class, String.class,
                                    MethodType.class, int.class).toMethodDescriptorString(),
                            false),
                    e.constant ? 1 : 0);
        }
    }

    private void loadEnv() {
        mv.visitVarInsn(ALOAD, thisLocal);
        mv.visitFieldInsn(GETFIELD, className, "$env", "Lde/hbt/hte/rt/Environment;");
    }

    private Class<?> handleShortcircuit(BinaryExpression e, int cmp, int first, int second) {
        e.left.accept(this);
        coerceToBoolean(e.left.staticType);
        Label label = new Label();
        mv.visitJumpInsn(cmp, label);
        e.right.accept(this);
        coerceToBoolean(e.right.staticType);
        mv.visitJumpInsn(cmp, label);
        mv.visitInsn(first);
        Label endLabel = new Label();
        mv.visitJumpInsn(GOTO, endLabel);
        mv.visitLabel(label);
        mv.visitInsn(second);
        mv.visitLabel(endLabel);
        return boolean.class;
    }

    private void coerceToBoolean(Class<?> t) {
        if (t == boolean.class)
            return;
        if (t == Boolean.class)
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
        else {
            loadEnv();
            mv.visitInsn(SWAP);
            wrap(t);
            mv.visitMethodInsn(INVOKEINTERFACE, "de/hbt/hte/rt/Environment", "coerceToBoolean", "(Ljava/lang/Object;)Z",
                    true);
        }
    }

    private static Class<?> intersect(Class<?> left, Class<?> right) {
        if (left == right)
            return left;
        else if (left == Object.class || right == Object.class)
            return left;
        else if (left == primitiveWrapperMap.get(right)) {
            return right;
        } else if (left == wrapperPrimitiveMap.get(right)) {
            return left;
        } else if (left.isAssignableFrom(right)) {
            return left;
        } else if (right.isAssignableFrom(left)) {
            return right;
        }
        throw new IllegalArgumentException();
    }

    @Override
    public void visitConditionalExpression(ConditionalExpression e) {
        e.condition.accept(this);
        coerceToBoolean(e.condition.staticType);
        Label elseLabel = new Label();
        mv.visitJumpInsn(IFEQ, elseLabel);
        e.thenPart.accept(this);
        Label endLabel = new Label();
        mv.visitJumpInsn(GOTO, endLabel);
        mv.visitLabel(elseLabel);
        e.elsePart.accept(this);
        mv.visitLabel(endLabel);
        e.staticType = intersect(e.thenPart.staticType, e.elsePart.staticType);
    }

    @Override
    public void visitDotIntoExpression(AttributeSelectExpression e) {
        e.source.accept(this);
        mv.visitInvokeDynamicInsn(e.attribute.name.image,
                MethodType.methodType(Object.class, e.source.staticType).toMethodDescriptorString(),
                new Handle(Opcodes.H_INVOKESTATIC, "de/hbt/hte/rt/SymbolRefBootstrap", "bootstrapDynamic",
                        MethodType.methodType(CallSite.class, MethodHandles.Lookup.class, String.class,
                                MethodType.class, int.class).toMethodDescriptorString(),
                        false),
                0);
    }

    @Override
    public void visitExpressionWithDefault(ExpressionWithDefault e) {
        Label start = new Label();
        mv.visitLabel(start);
        e.expression.accept(this);
        wrap(e.expression.staticType);
        int local = nextLocal(OBJECT_TYPE);
        mv.visitVarInsn(ASTORE, local);
        Label after = new Label();
        mv.visitJumpInsn(GOTO, after);
        Label end = new Label();
        mv.visitLabel(end);
        mv.visitInsn(POP);
        e.defaultExpression.accept(this);
        wrap(e.defaultExpression.staticType);
        mv.visitVarInsn(ASTORE, local);
        mv.visitLabel(after);
        mv.visitTryCatchBlock(start, end, end, "java/lang/Exception");
        mv.visitVarInsn(ALOAD, local);
        e.staticType = Object.class;
    }

    @Override
    public void visitIndexExpression(IndexExpression e) {
        throw new UnsupportedOperationException("NYI");
    }

    @Override
    public void visitLiteralExpression(LiteralExpression e) {
        if (e.value == null) {
            mv.visitInsn(ACONST_NULL);
        } else if (e.value instanceof Integer) {
            mv.visitLdcInsn(e.value);
            e.staticType = int.class;
        } else if (e.value instanceof Long) {
            mv.visitLdcInsn(e.value);
            e.staticType = long.class;
        } else if (e.value instanceof Float) {
            mv.visitLdcInsn(e.value);
            e.staticType = float.class;
        } else if (e.value instanceof Double) {
            mv.visitLdcInsn(e.value);
            e.staticType = double.class;
        } else if (Boolean.TRUE.equals(e.value)) {
            mv.visitInsn(ICONST_1);
            e.staticType = boolean.class;
        } else if (Boolean.FALSE.equals(e.value)) {
            mv.visitInsn(ICONST_0);
            e.staticType = boolean.class;
        } else if (e.value instanceof String) {
            mv.visitLdcInsn(e.value);
            e.staticType = String.class;
        } else {
            throw new TemplateException("Unkonwn literal type: " + e.getValue().getClass());
        }
    }

    @Override
    public void visitParenthesizedExpression(ParenthesizedExpression e) {
        e.expression.accept(this);
        e.staticType = e.expression.staticType;
    }

    @Override
    public void visitSymbolRef(SymbolRef e) {
        String name = e.name.image;
        VariableInScope scopeLocal = searchName(name);
        if (scopeLocal != null) {
            mv.visitVarInsn(scopeLocal.type.getOpcode(ILOAD), scopeLocal.local);
            e.staticType = scopeLocal.staticType;
        } else {
            mv.visitVarInsn(ALOAD, ctxLocal);
            mv.visitInvokeDynamicInsn(name,
                    MethodType.methodType(Object.class, Object.class).toMethodDescriptorString(),
                    new Handle(Opcodes.H_INVOKESTATIC, "de/hbt/hte/rt/SymbolRefBootstrap", "bootstrapDynamic",
                            MethodType.methodType(CallSite.class, MethodHandles.Lookup.class, String.class,
                                    MethodType.class, int.class).toMethodDescriptorString(),
                            false),
                    0);
        }
    }

    @Override
    public void visitUnaryExpression(UnaryExpression e) {
        String op = e.op.name.image;
        e.expression.accept(this);
        loadEnv();
        mv.visitInsn(SWAP);
        mv.visitInvokeDynamicInsn(AstUtils.escapeOpName(op),
                MethodType.methodType(Object.class, Environment.class, e.expression.staticType)
                        .toMethodDescriptorString(),
                new Handle(H_INVOKESTATIC, "de/hbt/hte/rt/OperatorBootstrap", "bootstrapUnary",
                        MethodType.methodType(CallSite.class, MethodHandles.Lookup.class, String.class,
                                MethodType.class, int.class).toMethodDescriptorString(),
                        false),
                0);
    }

    @Override
    public void visitBranchNode(BranchNode n) {
        n.condition.accept(this);
        coerceToBoolean(n.condition.staticType);
        Label endLabel = new Label();
        if (n.elseCase != null) {
            Label elseLabel = new Label();
            mv.visitJumpInsn(IFEQ, elseLabel);
            n.thenCase.accept(this);
            mv.visitJumpInsn(GOTO, endLabel);
            mv.visitLabel(elseLabel);
            n.elseCase.accept(this);
        } else {
            mv.visitJumpInsn(IFEQ, endLabel);
            n.thenCase.accept(this);
        }
        mv.visitLabel(endLabel);
    }

    private void generateConstantField(Expression e) {
        String fieldName = "$const" + sequence.getAndIncrement();
        MethodVisitor mv0 = mv;
        mv = ctor;
        int oldNextLocal = nextLocal;
        nextLocal = nextCtorLocal;
        e.accept(this);
        Type t = Type.getType(e.staticType);
        int local = nextLocal(t);
        mv.visitVarInsn(t.getOpcode(ISTORE), local);
        mv.visitVarInsn(ALOAD, thisLocal);
        mv.visitVarInsn(t.getOpcode(ILOAD), local);
        cw.visitField(ACC_PRIVATE | ACC_FINAL, fieldName, t.getDescriptor(), null, null);
        mv.visitFieldInsn(PUTFIELD, className, fieldName, t.getDescriptor());
        mv = mv0;
        nextCtorLocal = nextLocal;
        nextLocal = oldNextLocal;
        mv.visitVarInsn(ALOAD, thisLocal);
        mv.visitFieldInsn(GETFIELD, className, fieldName, t.getDescriptor());
    }

    @Override
    public void visitExpressionNode(ExpressionNode n) {
        n.expression.accept(constPropagateVisitor);
        if (n.expression.constant && n.expression.cost > 0) {
            generateConstantField(n.expression);
        } else {
            n.expression.accept(this);
        }
        wrap(n.expression.staticType);
        int local = nextLocal(OBJECT_TYPE);
        mv.visitVarInsn(ASTORE, local);
        loadEnv();
        mv.visitVarInsn(ALOAD, writerLocal);
        mv.visitVarInsn(ALOAD, local);
        if (n.as != null)
            mv.visitLdcInsn(n.as.image);
        else
            mv.visitInsn(ACONST_NULL);
        mv.visitMethodInsn(INVOKEINTERFACE, "de/hbt/hte/rt/Environment", "writeAs",
                "(Ljava/io/Writer;Ljava/lang/Object;Ljava/lang/String;)V", true);
    }

    @Override
    public void visitForEachNode(ForEachNode n) {
        n.collectionExpression.accept(this);
        mv.visitTypeInsn(CHECKCAST, "java/lang/Iterable");
        mv.visitMethodInsn(INVOKEINTERFACE, "java/lang/Iterable", "iterator", "()Ljava/util/Iterator;", true);
        int iteratorLocal = nextLocal(OBJECT_TYPE);
        int iteratorValue = nextLocal(OBJECT_TYPE);
        mv.visitVarInsn(ASTORE, iteratorLocal);
        Label condition = new Label();
        mv.visitJumpInsn(GOTO, condition);
        Label body = new Label();
        mv.visitLabel(body);
        mv.visitVarInsn(ALOAD, iteratorLocal);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true);
        mv.visitVarInsn(ASTORE, iteratorValue);
        scopes.push(Collections.singletonMap(n.counter.name.image,
                new VariableInScope(iteratorValue, Type.getType(Object.class), Object.class)));
        n.body.accept(this);
        scopes.pop();
        mv.visitLabel(condition);
        mv.visitVarInsn(ALOAD, iteratorLocal);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true);
        mv.visitJumpInsn(IFNE, body);
    }

    @Override
    public void visitSequenceNode(SequenceNode n) {
        for (Node c : n.children)
            c.accept(this);
    }

    @Override
    public void visitTemplateNode(TemplateNode n) {
        n.body.accept(this);
    }

    @Override
    public void visitTextNode(TextNode n) {
        int charsHandled = 0;
        String text = n.text;
        while (charsHandled < text.length()) {
            int remaining = text.length() - charsHandled;
            String subStr = text.substring(charsHandled, charsHandled + Math.min(remaining, MAX_STRING_CONSTANT_SIZE));
            mv.visitVarInsn(ALOAD, writerLocal);
            mv.visitLdcInsn(subStr);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/Writer", "write", "(Ljava/lang/String;)V", false);
            charsHandled += subStr.length();
        }
    }

    @Override
    public void visitRecordConstructorExpression(RecordConstructorExpression e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitAssignNode(AssignNode n) {
        for (Map.Entry<String, RecordConstructorExpression.RecordAttribute> def : n.definitions) {
            def.getValue().def.expression.accept(constPropagateVisitor);
            def.getValue().def.expression.accept(this);
            Class<?> clazz = def.getValue().def.expression.staticType;
            Type type = Type.getType(def.getValue().def.expression.staticType);
            int local = nextLocal(type);
            mv.visitVarInsn(type.getOpcode(ISTORE), local);
            scopes.push(Collections.singletonMap(def.getKey(), new VariableInScope(local, type, clazz)));
        }
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression e) {
        int receiver = nextLocal(OBJECT_TYPE);
        e.receiver.accept(this);
        mv.visitVarInsn(ASTORE, receiver);
        for (int i = 0; i < e.arguments.size(); i++) {
            e.arguments.get(i).accept(this);
            Type t = Type.getType(e.arguments.get(i).staticType);
            mv.visitVarInsn(t.getOpcode(ISTORE), nextLocal(t));
        }
        loadEnv();
        mv.visitVarInsn(ALOAD, receiver);
        int l = receiver + 1;
        for (int i = 0; i < e.arguments.size(); i++) {
            Type t = Type.getType(e.arguments.get(i).staticType);
            mv.visitVarInsn(ALOAD, l);
            l += t.getSize();
        }
        mv.visitInvokeDynamicInsn(e.name.name.image,
                MethodType.genericMethodType(2 + e.arguments.size()).changeParameterType(0, Environment.class)
                        .toMethodDescriptorString(),
                new Handle(H_INVOKESTATIC, "de/hbt/hte/rt/BuiltInBootstrap", "bootstrap",
                        MethodType.methodType(CallSite.class, MethodHandles.Lookup.class, String.class,
                                MethodType.class, int.class).toMethodDescriptorString(),
                        false),
                0);
    }

}
