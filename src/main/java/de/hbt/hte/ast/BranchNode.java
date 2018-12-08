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
@AllArgsConstructor
@RequiredArgsConstructor
public @Data class BranchNode extends Node {

    public final Expression condition;
    public final Node thenCase;
    public Node elseCase;

    public void accept(NodeVisitor visitor) {
        visitor.visitBranchNode(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("<#if ");
        sb.append(condition.toString());
        sb.append(">");
        if (thenCase != null) {
            sb.append(thenCase.toString());
        }
        if (elseCase != null) {
            sb.append("<#else>");
            sb.append(elseCase.toString());
        }
        sb.append("</#if>");
        return sb.toString();
    }

    @Override
    public String toApiString() {
        return "new " + BranchNode.class.getSimpleName() + "(" + condition.toApiString() + ", " + thenCase.toApiString()
                + ", " + elseCase.toApiString() + ")";
    }

}
