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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
public @Data class SequenceNode extends Node implements Iterable<Node> {

    public final List<Node> children = new ArrayList<Node>();

    public SequenceNode add(Node... children) {
        for (Node n : children) {
            this.children.add(n);
        }
        return this;
    }

    public boolean isEmpty() {
        return children.isEmpty();
    }

    public Node get(int i) {
        return children.get(i);
    }

    public int size() {
        return children.size();
    }

    public void accept(NodeVisitor visitor) {
        visitor.visitSequenceNode(this);
    }

    public Iterator<Node> iterator() {
        return children.iterator();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Node n : children) {
            sb.append(n.toString());
        }
        return sb.toString();
    }

    @Override
    public String toApiString() {
        StringBuilder sb = new StringBuilder();
        sb.append("new ");
        sb.append(SequenceNode.class.getSimpleName());
        sb.append("(");
        Iterator<Node> it = children.iterator();
        while (it.hasNext()) {
            Node n = it.next();
            sb.append(n.toApiString());
            if (it.hasNext()) {
                sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }

}
