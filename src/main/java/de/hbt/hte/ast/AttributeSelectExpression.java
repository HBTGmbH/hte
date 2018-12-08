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
public @Data class AttributeSelectExpression extends Expression {

    public final Expression source;
    public final SymbolRef attribute;
    public final boolean nullSafe;

    public void accept(ExpressionVisitor visitor) {
        visitor.visitDotIntoExpression(this);
    }

    public String toString() {
        return source + (nullSafe ? "?." : ".") + attribute;
    }

    public String toApiString() {
        if (nullSafe) {
            return "new " + AttributeSelectExpression.class.getSimpleName() + "(" + source.toApiString() + ", "
                    + attribute.toApiString() + ", true)";
        } else {
            return "new " + AttributeSelectExpression.class.getSimpleName() + "(" + source.toApiString() + ", "
                    + attribute.toApiString() + ")";
        }
    }

}
