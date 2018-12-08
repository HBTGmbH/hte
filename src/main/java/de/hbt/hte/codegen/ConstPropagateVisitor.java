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

import java.util.*;

import de.hbt.hte.ast.*;

public class ConstPropagateVisitor implements ExpressionVisitor {

    @Override
    public void visitBinaryExpression(BinaryExpression e) {
        e.left.accept(this);
        e.right.accept(this);
        e.constant = e.left.constant && e.right.constant;
        e.cost = e.left.cost + e.right.cost + 1;
    }

    @Override
    public void visitConditionalExpression(ConditionalExpression e) {
        e.condition.accept(this);
        e.thenPart.accept(this);
        e.elsePart.accept(this);
        e.constant = e.condition.constant && e.thenPart.constant && e.elsePart.constant;
        e.cost = e.condition.cost + e.thenPart.cost + e.elsePart.cost + 2;
    }

    @Override
    public void visitDotIntoExpression(AttributeSelectExpression e) {
        e.source.accept(this);
        e.constant = e.source.constant;
        e.cost = e.source.cost + 1;
    }

    @Override
    public void visitExpressionWithDefault(ExpressionWithDefault e) {
        e.expression.accept(this);
        e.defaultExpression.accept(this);
        e.constant = e.expression.constant && e.defaultExpression.constant;
        e.cost = e.expression.cost + e.defaultExpression.cost + 2;
    }

    @Override
    public void visitIndexExpression(IndexExpression e) {
        e.indexSourceExpression.accept(this);
        e.indexKeyExpression.accept(this);
        e.constant = e.indexSourceExpression.constant && e.indexKeyExpression.constant;
        e.cost = e.indexSourceExpression.cost + e.indexKeyExpression.cost + 1;
    }

    @Override
    public void visitLiteralExpression(LiteralExpression e) {
        e.constant = true;
        e.cost = 0;
    }

    @Override
    public void visitParenthesizedExpression(ParenthesizedExpression e) {
        e.expression.accept(this);
        e.constant = e.expression.constant;
        e.cost = e.expression.cost;
    }

    @Override
    public void visitSymbolRef(SymbolRef e) {
        e.cost = 10;
    }

    @Override
    public void visitUnaryExpression(UnaryExpression e) {
        e.expression.accept(this);
        e.constant = e.expression.constant;
        e.cost = e.expression.cost + 1;
    }

    @Override
    public void visitRecordConstructorExpression(RecordConstructorExpression e) {
        for (Map.Entry<String, RecordConstructorExpression.RecordAttribute> def : e) {
            def.getValue().def.expression.accept(this);
            def.getValue().constant = def.getValue().def.expression.constant;
        }
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression e) {
        e.receiver.accept(this);
        for (Expression arg : e.arguments) {
            arg.accept(this);
        }
    }

}
