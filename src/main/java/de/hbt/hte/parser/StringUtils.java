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
package de.hbt.hte.parser;

import java.io.*;

import lombok.experimental.*;

@UtilityClass
class StringUtils {

    private static void unescapeJava(Writer out, String str) throws IOException {
        int sz = str.length();
        StringBuffer unicode = new StringBuffer(4);
        boolean hadSlash = false;
        boolean inUnicode = false;
        for (int i = 0; i < sz; i++) {
            char ch = str.charAt(i);
            if (inUnicode) {
                unicode.append(ch);
                if (unicode.length() == 4) {
                    try {
                        int value = Integer.parseInt(unicode.toString(), 16);
                        out.write((char) value);
                        unicode.setLength(0);
                        inUnicode = false;
                        hadSlash = false;
                    } catch (NumberFormatException nfe) {
                        throw new RuntimeException("Unable to parse unicode value: " + unicode, nfe);
                    }
                }
                continue;
            }
            if (hadSlash) {
                hadSlash = false;
                switch (ch) {
                case '\\':
                    out.write('\\');
                    break;
                case '\'':
                    out.write('\'');
                    break;
                case '\"':
                    out.write('"');
                    break;
                case 'r':
                    out.write('\r');
                    break;
                case 'f':
                    out.write('\f');
                    break;
                case 't':
                    out.write('\t');
                    break;
                case 'n':
                    out.write('\n');
                    break;
                case 'b':
                    out.write('\b');
                    break;
                case 'u': {
                    inUnicode = true;
                    break;
                }
                default:
                    out.write(ch);
                    break;
                }
                continue;
            } else if (ch == '\\') {
                hadSlash = true;
                continue;
            }
            out.write(ch);
        }
        if (hadSlash) {
            out.write('\\');
        }
    }

    static String unescapeJava(String str) {
        StringWriter writer = new StringWriter();
        try {
            unescapeJava(writer, str);
            String res = writer.toString();
            writer.close();
            return res;
        } catch (IOException e) {
            throw new AssertionError("Should not happen!");
        }
    }

}
