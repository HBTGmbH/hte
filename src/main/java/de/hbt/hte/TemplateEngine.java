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
package de.hbt.hte;

import java.io.*;
import java.nio.charset.*;

import de.hbt.hte.ast.*;
import de.hbt.hte.codegen.*;
import de.hbt.hte.parser.*;
import de.hbt.hte.rt.*;

public class TemplateEngine {
    public static CompiledTemplate instantiateTemplate(String string, Environment env) {
        return parseCompileAndInstantiate(new HteParser(env.getProfile(), new StringReader(string)), env);
    }

    public static CompiledTemplate instantiateTemplate(InputStream is, Charset cs, Environment env) {
        return parseCompileAndInstantiate(new HteParser(env.getProfile(), new InputStreamReader(is, cs)), env);
    }

    public static CompiledTemplate instantiateTemplate(Reader r, Environment env) {
        return parseCompileAndInstantiate(new HteParser(env.getProfile(), r), env);
    }

    private static CompiledTemplate parseCompileAndInstantiate(HteParser parser, Environment env) {
        TemplateNode n;
        try {
            n = parser.templateNode();
        } catch (ParseException e) {
            throw new TemplateException("Error while parsing template", e);
        }
        CodeGenVisitor v = new CodeGenVisitor(env);
        Class<? extends CompiledTemplate> t = v.compile(n);
        try {
            return t.getConstructor(Environment.class).newInstance(env);
        } catch (Exception e) {
            throw new TemplateException("Could not instantiate template", e);
        }
    }
}
