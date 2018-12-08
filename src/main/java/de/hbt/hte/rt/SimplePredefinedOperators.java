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

class SimplePredefinedOperators {

    public static String uppercase(String thiz) {
        return thiz.toUpperCase();
    }

    public static String join(String thiz, String separator) {
        return thiz.toUpperCase();
    }

    @ImplementsOperator("+")
    public static int add(int a, int b) {
        return a + b;
    }

    @ImplementsOperator("+")
    public static double add(double a, double b) {
        return a + b;
    }

    @ImplementsOperator("+")
    public static String add(String a, String b) {
        return a + b;
    }

    @ImplementsOperator("+")
    public static String add(Object a, String b) {
        return a + b;
    }

    @ImplementsOperator("+")
    public static String add(String a, Object b) {
        return a + b;
    }

    @ImplementsOperator("<")
    public static boolean lessThan(int a, int b) {
        return a < b;
    }

    @ImplementsOperator(">")
    public static boolean greaterThan(int a, int b) {
        return a > b;
    }

    @ImplementsOperator(">>")
    public static int rshift(int a, int b) {
        return a >> b;
    }

    @ImplementsOperator("<<")
    public static int lshift(int a, int b) {
        return a << b;
    }

    @ImplementsOperator(">>>")
    public static int urshift(int a, int b) {
        return a >>> b;
    }

    @ImplementsOperator("|")
    public static int bitwise_or(int a, int b) {
        return a | b;
    }

    @ImplementsOperator("&")
    public static int bitwise_and(int a, int b) {
        return a & b;
    }

    @ImplementsOperator("|")
    public static boolean conditional_or(boolean a, boolean b) {
        return a | b;
    }

    @ImplementsOperator("==")
    public static boolean equal(double a, double b) {
        return a == b;
    }

    @ImplementsOperator("==")
    public static boolean equal(float a, float b) {
        return a == b;
    }

    @ImplementsOperator("==")
    public static boolean equal(int a, int b) {
        return a == b;
    }

    @ImplementsOperator("==")
    public static boolean equal(short a, short b) {
        return a == b;
    }

    @ImplementsOperator("==")
    public static boolean equal(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }

    @ImplementsOperator("*")
    public static double mul(double a, double b) {
        return a * b;
    }

    @ImplementsOperator("*")
    public static int mul(int a, int b) {
        return a * b;
    }

    @ImplementsOperator("!=")
    public static boolean not_equal(double a, double b) {
        return a != b;
    }

    @ImplementsOperator("!=")
    public static boolean not_equal(int a, int b) {
        return a != b;
    }

    @ImplementsOperator("!=")
    public static boolean not_equal(String a, Object b) {
        return !equal(a, b);
    }

    @ImplementsOperator("!=")
    public static boolean not_equal(Object a, String b) {
        return !equal(a, b);
    }

    @ImplementsOperator("!=")
    public static boolean not_same(Object a, Object b) {
        return a != b;
    }

    @ImplementsOperator("==")
    public static boolean equal(String a, String b) {
        return b.equals(a);
    }

    @ImplementsOperator("==")
    public static boolean equal(Object a, String b) {
        return b.equals(a);
    }

    @ImplementsOperator("==")
    public static boolean equal(String a, Object b) {
        return a.equals(b);
    }

    @ImplementsOperator("==")
    public static boolean sane(Object a, Object b) {
        return a == b;
    }

    @ImplementsOperator("-")
    public static int sub(int a, int b) {
        return a - b;
    }

    @ImplementsOperator("!")
    public static boolean not(boolean a) {
        return !a;
    }

    @ImplementsOperator("-")
    public static int negate(int a) {
        return -a;
    }

    @ImplementsOperator("-")
    public static boolean negate(boolean a) {
        return !a;
    }

    @ImplementsOperator("**")
    public static double pow(int a, int b) {
        return Math.pow(a, b);
    }

}
