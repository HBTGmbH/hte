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

import java.util.HashMap;
import java.util.Map;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
public @Data class TemplateNode extends Node {

    public final Node body;
    public final Map<String, String> attributes;

    public TemplateNode(Map<String, String> attributes, Node... children) {
        super();
        this.attributes = new HashMap<String, String>(attributes);
        if (children.length == 1) {
            this.body = children[0];
        } else {
            SequenceNode sn = new SequenceNode();
            for (Node n : children) {
                sn.add(n);
            }
            this.body = sn;
        }
    }

    public TemplateNode(Node... children) {
        this(new HashMap<String, String>(), children);
    }

    public String getTemplateId() {
        return attributes.get("id");
    }

    public void setTemplateId(String templateId) {
        attributes.put("id", templateId);
    }

    public String getTemplateVersion() {
        return attributes.get("version");
    }

    public void setTemplateVersion(String templateVersion) {
        attributes.put("version", templateVersion);
    }

    public void accept(NodeVisitor visitor) {
        visitor.visitTemplateNode(this);
    }

    @Override
    public String toString() {
        return body.toString();
    }

    @Override
    public String toApiString() {
        StringBuilder sb = new StringBuilder();
        sb.append("new ");
        sb.append(TemplateNode.class.getSimpleName());
        sb.append("(");
        if (body != null) {
            sb.append(body.toApiString());
        }
        sb.append(")");
        return sb.toString();
    }

}
