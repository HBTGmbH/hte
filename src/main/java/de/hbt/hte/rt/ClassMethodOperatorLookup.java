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

import java.lang.reflect.*;
import java.util.*;

class ClassMethodOperatorLookup implements MethodLookup {

    private final Class<?> clazz;

    public ClassMethodOperatorLookup(Class<?> clazz) {
        this.clazz = clazz;
    }

    public LookupResult lookup(String name, Class<?>... paramTypes) {
        List<Method> applicable = new ArrayList<Method>();
        Method[] meths = clazz.getMethods();
        for (Method m : meths) {
            if (!Modifier.isStatic(m.getModifiers()) || paramTypes.length != m.getParameterTypes().length
                    || m.getReturnType() == void.class || m.getReturnType() == Void.class) {
                continue;
            }
            ImplementsOperator ann = m.getAnnotation(ImplementsOperator.class);
            if (ann != null && !name.equals(ann.value()) || ann == null && !m.getName().equals(name)) {
                continue;
            }
            applicable.add(m);
        }
        Method m = ClassUtils.getMatchingAccessibleMethod(applicable, paramTypes);
        Method im = ClassUtils.getInverseMatchingAccessibleMethod(applicable, paramTypes);
        LookupResult res = new LookupResult();
        res.m = m;
        res.isBotton = m == im;
        return res;
    }

    public static void main(String[] args) {
        new ClassMethodOperatorLookup(SimplePredefinedOperators.class).lookup("+", Object.class, Object.class);
    }

}
