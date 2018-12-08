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

import java.io.*;
import java.net.*;

import lombok.*;

public @Data class SimpleEnvironment implements Environment {

    private final MethodLookup operatorLookup = new ClassMethodOperatorLookup(SimplePredefinedOperators.class);
    private final Profile profile = new SimpleProfile();

    public void writeUndefined(Writer writer, String source, int line, int column, UndefinedRaisedException exception)
            throws IOException {
    }

    @Override
    public boolean coerceToBoolean(Object v) {
        if (v instanceof Boolean)
            return Boolean.TRUE.equals(v);
        else
            throw new TemplateException(v + " cannot be coerced to boolean");
    }

    @Override
    public void writeAs(Writer writer, Object value, String as) throws IOException {
        String str = String.valueOf(value);
        if (value instanceof String && "qpval".equals(as)) {
            /* The value is a query parameter value. Encode it. */
            str = URLEncoder.encode(str, "UTF-8");
        }
        writer.append(str);
    }

}
