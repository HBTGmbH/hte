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

import java.util.*;
import java.util.Map.*;

import de.hbt.hte.ast.RecordConstructorExpression.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
public @Data class RecordConstructorExpression extends Expression
        implements Iterable<Map.Entry<String, RecordAttribute>> {
    public static @lombok.Data class RecordAttribute {
        public final SymbolDef def;
        public boolean constant;

        public RecordAttribute(SymbolDef def) {
            this.def = def;
        }
    }

    public final Map<String, RecordAttribute> attributes = new LinkedHashMap<>();

    public RecordConstructorExpression add(RecordAttribute attribute) {
        this.attributes.put(attribute.def.name.image, attribute);
        return this;
    }

    public RecordConstructorExpression add(SymbolDef def) {
        RecordAttribute attr = new RecordAttribute(def);
        return add(attr);
    }

    public void accept(ExpressionVisitor visitor) {
        visitor.visitRecordConstructorExpression(this);
    }

    public Iterator<Entry<String, RecordAttribute>> iterator() {
        return attributes.entrySet().iterator();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        for (Map.Entry<String, RecordAttribute> entry : this) {
            sb.append("  ");
            sb.append(entry.getKey());
            sb.append(" = ");
            sb.append(entry.getValue().def.expression);
            sb.append("\n");
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String toApiString() {
        StringBuilder sb = new StringBuilder();
        sb.append("new ");
        sb.append(RecordConstructorExpression.class.getSimpleName());
        sb.append("()");
        for (Map.Entry<String, RecordAttribute> e : this) {
            String name = e.getKey();
            RecordAttribute attr = e.getValue();
            sb.append(".add(\"");
            sb.append(name);
            sb.append("\", ");
            sb.append(attr.def.expression.toApiString());
            sb.append(")");
        }
        return sb.toString();
    }

}
