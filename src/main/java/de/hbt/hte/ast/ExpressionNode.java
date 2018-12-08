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

import de.hbt.hte.parser.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
public @Data class ExpressionNode extends Node {

    public final Expression expression;
    public final Token as;

    public void accept(NodeVisitor visitor) {
        visitor.visitExpressionNode(this);
    }

    @Override
    public String toString() {
        return "${" + expression + (as != null ? " as " + as.image : "") + "}";
    }

    @Override
    public String toApiString() {
        if (as != null)
            return "new " + ExpressionNode.class.getSimpleName() + "(" + expression.toApiString() + ", \"" + as.image + "\")";
        else
            return "new " + ExpressionNode.class.getSimpleName() + "(" + expression.toApiString() + ")";
    }

}
