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

public class ClassUtils {

    private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];
    private static final Class<?>[] ORDERED_PRIMITIVE_TYPES = { Byte.TYPE, Short.TYPE, Character.TYPE, Integer.TYPE,
            Long.TYPE, Float.TYPE, Double.TYPE };

    private static final Map<Class<?>, Class<?>> primitiveWrapperMap = new HashMap<Class<?>, Class<?>>();
    static {
        primitiveWrapperMap.put(Boolean.TYPE, Boolean.class);
        primitiveWrapperMap.put(Byte.TYPE, Byte.class);
        primitiveWrapperMap.put(Character.TYPE, Character.class);
        primitiveWrapperMap.put(Short.TYPE, Short.class);
        primitiveWrapperMap.put(Integer.TYPE, Integer.class);
        primitiveWrapperMap.put(Long.TYPE, Long.class);
        primitiveWrapperMap.put(Double.TYPE, Double.class);
        primitiveWrapperMap.put(Float.TYPE, Float.class);
        primitiveWrapperMap.put(Void.TYPE, Void.TYPE);
    }

    private static final Map<Class<?>, Class<?>> wrapperPrimitiveMap = new HashMap<Class<?>, Class<?>>();
    static {
        for (Class<?> primitiveClass : primitiveWrapperMap.keySet()) {
            Class<?> wrapperClass = primitiveWrapperMap.get(primitiveClass);
            if (!primitiveClass.equals(wrapperClass)) {
                wrapperPrimitiveMap.put(wrapperClass, primitiveClass);
            }
        }
    }

    private static boolean isSameLength(Object[] array1, Object[] array2) {
        return getLength(array1) == getLength(array2);
    }

    private static int getLength(Object array) {
        if (array == null) {
            return 0;
        }
        return Array.getLength(array);
    }

    private static boolean isAssignable(Class<?>[] classArray, Class<?>[] toClassArray, boolean autoboxing) {
        if (!isSameLength(classArray, toClassArray)) {
            return false;
        }
        if (classArray == null) {
            classArray = EMPTY_CLASS_ARRAY;
        }
        if (toClassArray == null) {
            toClassArray = EMPTY_CLASS_ARRAY;
        }
        for (int i = 0; i < classArray.length; i++) {
            if (!isAssignable(classArray[i], toClassArray[i], autoboxing)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isAssignable(Class<?> cls, Class<?> toClass) {
        return isAssignable(cls, toClass, true);
    }

    private static boolean isAssignable(Class<?> cls, Class<?> toClass, boolean autoboxing) {
        if (toClass == null) {
            return false;
        }
        if (cls == null) {
            return !toClass.isPrimitive();
        }
        if (autoboxing) {
            if (cls.isPrimitive() && !toClass.isPrimitive()) {
                cls = primitiveToWrapper(cls);
                if (cls == null) {
                    return false;
                }
            }
            if (toClass.isPrimitive() && !cls.isPrimitive()) {
                cls = wrapperToPrimitive(cls);
                if (cls == null) {
                    return false;
                }
            }
        }
        if (cls.equals(toClass)) {
            return true;
        }
        if (cls.isPrimitive()) {
            if (!toClass.isPrimitive()) {
                return false;
            }
            if (Integer.TYPE.equals(cls)) {
                return Long.TYPE.equals(toClass) || Float.TYPE.equals(toClass) || Double.TYPE.equals(toClass);
            }
            if (Long.TYPE.equals(cls)) {
                return Float.TYPE.equals(toClass) || Double.TYPE.equals(toClass);
            }
            if (Boolean.TYPE.equals(cls)) {
                return false;
            }
            if (Double.TYPE.equals(cls)) {
                return false;
            }
            if (Float.TYPE.equals(cls)) {
                return Double.TYPE.equals(toClass);
            }
            if (Character.TYPE.equals(cls)) {
                return Integer.TYPE.equals(toClass) || Long.TYPE.equals(toClass) || Float.TYPE.equals(toClass)
                        || Double.TYPE.equals(toClass);
            }
            if (Short.TYPE.equals(cls)) {
                return Integer.TYPE.equals(toClass) || Long.TYPE.equals(toClass) || Float.TYPE.equals(toClass)
                        || Double.TYPE.equals(toClass);
            }
            if (Byte.TYPE.equals(cls)) {
                return Short.TYPE.equals(toClass) || Integer.TYPE.equals(toClass) || Long.TYPE.equals(toClass)
                        || Float.TYPE.equals(toClass) || Double.TYPE.equals(toClass);
            }
            return false;
        }
        return toClass.isAssignableFrom(cls);
    }

    private static Class<?> primitiveToWrapper(Class<?> cls) {
        Class<?> convertedClass = cls;
        if (cls != null && cls.isPrimitive()) {
            convertedClass = primitiveWrapperMap.get(cls);
        }
        return convertedClass;
    }

    private static Class<?> wrapperToPrimitive(Class<?> cls) {
        return wrapperPrimitiveMap.get(cls);
    }

    private static boolean isAccessible(Member m) {
        return m != null && Modifier.isPublic(m.getModifiers()) && !m.isSynthetic();
    }

    private static int compareParameterTypes(Class<?>[] left, Class<?>[] right, Class<?>[] actual) {
        float leftCost = getTotalTransformationCost(actual, left);
        float rightCost = getTotalTransformationCost(actual, right);
        return leftCost < rightCost ? -1 : rightCost < leftCost ? 1 : 0;
    }

    private static float getTotalTransformationCost(Class<?>[] srcArgs, Class<?>[] destArgs) {
        float totalCost = 0.0f;
        for (int i = 0; i < srcArgs.length; i++) {
            Class<?> srcClass, destClass;
            srcClass = srcArgs[i];
            destClass = destArgs[i];
            totalCost += getObjectTransformationCost(srcClass, destClass);
        }
        return totalCost;
    }

    private static float getObjectTransformationCost(Class<?> srcClass, Class<?> destClass) {
        if (destClass.isPrimitive())
            return getPrimitivePromotionCost(srcClass, destClass);
        float cost = 0.0f;
        while (srcClass != null && !destClass.equals(srcClass)) {
            if (destClass.isInterface() && isAssignable(srcClass, destClass)) {
                cost += 0.25f;
                break;
            }
            cost++;
            srcClass = srcClass.getSuperclass();
        }
        if (srcClass == null)
            cost += 1.5f;
        return cost;
    }

    private static float getPrimitivePromotionCost(Class<?> srcClass, Class<?> destClass) {
        float cost = 0.0f;
        Class<?> cls = srcClass;
        if (!cls.isPrimitive()) {
            cost += 0.1f;
            cls = wrapperToPrimitive(cls);
        }
        for (int i = 0; cls != destClass && i < ORDERED_PRIMITIVE_TYPES.length; i++) {
            if (cls == ORDERED_PRIMITIVE_TYPES[i]) {
                cost += 0.1f;
                if (i < ORDERED_PRIMITIVE_TYPES.length - 1)
                    cls = ORDERED_PRIMITIVE_TYPES[i + 1];
            }
        }
        return cost;
    }

    private static Method getAccessibleMethod(Method method) {
        if (!isAccessible(method))
            return null;
        return method;
    }

    static Method getOverriddenMethod(Method m) {
        Class<?> declaringClass = m.getDeclaringClass();
        Class<?> clazz = declaringClass.getSuperclass();
        Method method = m;
        while (clazz != null) {
            try {
                Method candidate = clazz.getMethod(m.getName(), m.getParameterTypes());
                if (candidate.getModifiers() == m.getModifiers()
                        && candidate.getDeclaringClass().getModifiers() == declaringClass.getModifiers())
                    method = candidate;
            } catch (Exception e) {
            }
            clazz = clazz.getSuperclass();
        }
        return method;
    }

    static Method getMatchingAccessibleMethod(List<Method> methods, Class<?>... parameterTypes) {
        Method bestMatch = null;
        for (Method method : methods) {
            if (isAssignable(parameterTypes, method.getParameterTypes(), true)) {
                Method accessibleMethod = getAccessibleMethod(method);
                if (accessibleMethod != null
                        && (bestMatch == null || compareParameterTypes(accessibleMethod.getParameterTypes(),
                                bestMatch.getParameterTypes(), parameterTypes) < 0)) {
                    bestMatch = accessibleMethod;
                }
            }
        }
        return bestMatch;
    }

    static Method getInverseMatchingAccessibleMethod(List<Method> methods, Class<?>... parameterTypes) {
        Method bestMatch = null;
        for (Method method : methods) {
            if (isAssignable(method.getParameterTypes(), parameterTypes, true)) {
                Method accessibleMethod = getAccessibleMethod(method);
                if (accessibleMethod != null
                        && (bestMatch == null || compareParameterTypes(accessibleMethod.getParameterTypes(),
                                bestMatch.getParameterTypes(), parameterTypes) > 0)) {
                    bestMatch = accessibleMethod;
                }
            }
        }
        return bestMatch;
    }

}
