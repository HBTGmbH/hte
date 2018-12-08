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

import de.hbt.hte.rt.*;
import lombok.experimental.*;

@UtilityClass
public class AstUtils {

    public static String stripLeft(String str) {
        if (str.startsWith("\n")) {
            return str.substring(1);
        }
        return str;
    }

    public static String escapeOpName(String op) {
        return op.replace("<", "$lt$").replace(">", "$gt$");
    }

    public static String unescapeOpName(String op) {
        return op.replace("$lt$", "<").replace("$gt$", ">");
    }

    public static Expression repairLeftAssoc(Profile profile, Expression expr) {
        throw new UnsupportedOperationException("NYI");
    }

    public static Expression repairRightAssoc(Profile profile, Expression expr) {
        BinaryExpression e;
        if (expr instanceof BinaryExpression) {
            e = (BinaryExpression) expr;
        } else {
            return expr;
        }

        int rightPrecedence = Integer.MIN_VALUE;
        int precedence = Integer.MIN_VALUE;
        Associativity associativity = Associativity.LEFT;

        /*
         * Check if it is a predefined operator or a infix function. Operators always take precedence over functions and
         * functions are left-associative.
         */
        OperatorDescription op = profile.getPredefinedOp(e.op.name.image);
        if (op != null) {
            /* It's a predefined operator */
            precedence = op.precedence;
            associativity = op.associativity;
        }

        Expression r = e.right;
        if (r instanceof BinaryExpression) {
            BinaryExpression e2 = (BinaryExpression) r;
            OperatorDescription op2 = profile.getPredefinedOp(e2.op.name.image);

            if (op2 != null) {
                rightPrecedence = op2.precedence;
            }

            /* Right operator has higher precedence -> leave as is! */
            if (rightPrecedence > precedence) {
                return expr;
            }

            /*
             * If right operator has lower precedence or left operator has 'left' associativity, then -> reorder!
             */
            if (rightPrecedence < precedence || associativity == Associativity.LEFT) {
                Expression right = e2.right;
                Expression left = e2.left;

                e2.left = e.left;
                // e2.setBeginLine(e.getLeft().getBeginLine());
                e2.right = left;
                // e2.setEndLine(left.getEndLine());

                e.right = right;
                // e.setEndLine(right.getEndLine());
                e.left = e2;
                // e.setBeginLine(e2.getBeginLine());

                SymbolRef eop = e.op;
                SymbolRef rop = e2.op;
                e2.op = eop;
                e.op = rop;

                repairRightAssoc(profile, e.left);
            }
        }

        return e;
    }

}
