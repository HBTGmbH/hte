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
package de.hbt.hte.ast;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
public @Data class LiteralExpression extends Expression {

    public final Object value;

    public void accept(ExpressionVisitor visitor) {
        visitor.visitLiteralExpression(this);
    }

    private static String quoteQuotes(String s) {
        return s.replace("\"", "\\\"");
    }

    @Override
    public String toString() {
        if (value == null) {
            return "null";
        } else if (value instanceof Float) {
            return value + "f";
        } else if (value instanceof Long) {
            return value + "L";
        } else if (value instanceof Character) {
            return "'" + value + "'";
        } else if (value instanceof String) {
            String string = (String) value;
            return "\"" + quoteQuotes(string) + "\"";
        }
        return value.toString();
    }

    @Override
    public String toApiString() {
        return "new " + LiteralExpression.class.getSimpleName() + "(" + toString() + ")";
    }

}
