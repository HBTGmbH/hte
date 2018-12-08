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

import java.util.*;

import de.hbt.hte.ast.*;

public class SimpleProfile implements Profile {

    private static final List<OperatorDescription> predefinedOps = new ArrayList<OperatorDescription>();
    static {
        predefinedOps.add(new OperatorDescription("||", 1, Associativity.LEFT, true, ShortcircuitOp.OR));
        predefinedOps.add(new OperatorDescription("&&", 2, Associativity.LEFT, true, ShortcircuitOp.AND));
        predefinedOps.add(new OperatorDescription("==", 3, Associativity.RIGHT, true, null));
        predefinedOps.add(new OperatorDescription("<", 4, Associativity.NONE, false, null));
        predefinedOps.add(new OperatorDescription(">", 4, Associativity.NONE, false, null));
        predefinedOps.add(new OperatorDescription("+", 5, Associativity.NONE, true, null));
        predefinedOps.add(new OperatorDescription("-", 5, Associativity.LEFT, false, null));
        predefinedOps.add(new OperatorDescription("*", 6, Associativity.NONE, true, null));
        predefinedOps.add(new OperatorDescription("/", 6, Associativity.LEFT, false, null));
        predefinedOps.add(new OperatorDescription("**", 7, Associativity.LEFT, false, null));
    }

    @Override
    public OperatorDescription getPredefinedOp(String op) {
        for (OperatorDescription od : predefinedOps) {
            if (od.op.equals(op)) {
                return od;
            }
        }
        return null;
    }

}
