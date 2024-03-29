/**
 * Copyright (c) 2005-2011 springside.org.cn
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * <p>
 * $Id: Fixtures.java 1593 2011-05-11 10:37:12Z calvinxiu $
 */
package com.gomcarter.frameworks.base.common;

import com.gomcarter.frameworks.base.exception.CustomException;

import java.util.Collection;

/**
 * 与Spring Assert功能类似, 代码基本从org.springframework.util.Assert复制, 增加如下功能:
 * <p>
 * 1. 修改类名, 免得一天到晚和org.junit.Assert冲突.
 * 2. 可抛出指定的业务异常类, 而不是通用的CustomException.
 * <p>
 * 代码示例:
 * <pre class="code">AssertUtils.hasText(uName, new IllegalBizArgumentsException(ErrorCode.USERNAME_ERROR));</pre>
 *
 * @author badqiu
 * @author calvin
 */
public abstract class AssertUtils {

    /**
     * Assert a boolean expression, throwing <code>CustomException</code>
     * if the test result is <code>false</code>.
     * <pre class="code">Assert.isTrue(i &gt; 0);</pre>
     *
     * @param expression a boolean expression
     * @throws CustomException if expression is <code>false</code>
     */
    public static void isTrue(boolean expression) {
        isTrue(expression, "[Assertion failed] - this expression must be true");
    }

    /**
     * Assert a boolean expression, throwing <code>CustomException</code>
     * if the test result is <code>false</code>.
     * <pre class="code">Assert.isTrue(i &gt; 0, "The value must be greater than zero");</pre>
     *
     * @param expression a boolean expression
     * @param message    the exception message to use if the assertion fails
     * @throws CustomException if expression is <code>false</code>
     */
    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new CustomException(message);
        }
    }

    /**
     * @param expression        a boolean expression
     * @param throwIfAssertFail if expression is <code>false</code>
     * @throws CustomException if expression is <code>false</code>
     */
    public static void isTrue(boolean expression, CustomException throwIfAssertFail) {
        if (!expression) {
            throw throwIfAssertFail;
        }
    }

    /**
     * Assert that an object is <code>null</code> .
     * <pre class="code">Assert.isNull(value);</pre>
     *
     * @param object the object to check
     * @throws CustomException if the object is not <code>null</code>
     */
    public static void isNull(Object object) {
        isNull(object, "[Assertion failed] - the object argument must be null");
    }

    /**
     * Assert that an object is <code>null</code> .
     * <pre class="code">Assert.isNull(value, "The value must be null");</pre>
     *
     * @param object  the object to check
     * @param message the exception message to use if the assertion fails
     * @throws CustomException if the object is not <code>null</code>
     */
    public static void isNull(Object object, String message) {
        if (object != null) {
            throw new CustomException(message);
        }
    }

    public static void isNull(Object object, CustomException throwIfAssertFail) {
        if (object != null) {
            throw throwIfAssertFail;
        }
    }

    /**
     * Assert that an object is not <code>null</code> .
     * <pre class="code">Assert.notNull(clazz);</pre>
     *
     * @param object the object to check
     * @throws CustomException if the object is <code>null</code>
     */
    public static void notNull(Object object) {
        notNull(object, "[Assertion failed] - this argument is required; it must not be null");
    }

    /**
     * Assert that an object is not <code>null</code> .
     * <pre class="code">Assert.notNull(clazz, "The class must not be null");</pre>
     *
     * @param object  the object to check
     * @param message the exception message to use if the assertion fails
     * @throws CustomException if the object is <code>null</code>
     */
    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new CustomException(message);
        }
    }

    public static void notNull(Object object, CustomException throwIfAssertFail) {
        if (object == null) {
            throw throwIfAssertFail;
        }
    }

    /**
     * Assert that an array has no null elements.
     * Note: Does not complain if the array is empty!
     * <pre class="code">Assert.noNullElements(array, "The array must have non-null elements");</pre>
     *
     * @param array   the array to check
     * @param message the exception message to use if the assertion fails
     * @throws CustomException if the object array contains a <code>null</code> element
     */
    public static void noNullElements(Object[] array, String message) {
        if (array != null) {
            for (Object element : array) {
                if (element == null) {
                    throw new CustomException(message);
                }
            }
        }
    }

    /**
     * Assert that an array has no null elements.
     * Note: Does not complain if the array is empty!
     * <pre class="code">Assert.noNullElements(array);</pre>
     *
     * @param array the array to check
     * @throws CustomException if the object array contains a <code>null</code> element
     */
    public static void noNullElements(Object[] array) {
        noNullElements(array, "[Assertion failed] - this array must not contain any null elements");
    }

    public static void noNullElements(Object[] array, CustomException throwIfAssertFail) {
        if (array != null) {
            for (Object element : array) {
                if (element == null) {
                    throw throwIfAssertFail;
                }
            }
        }
    }

    /**
     * Assert that a collection has elements; that is, it must not be
     * <code>null</code> and must have at least one element.
     * <pre class="code">Assert.notEmpty(collection, "Collection must have elements");</pre>
     *
     * @param collection the collection to check
     * @param message    the exception message to use if the assertion fails
     * @throws CustomException if the collection is <code>null</code> or has no elements
     */
    @SuppressWarnings("rawtypes")
    public static void notEmpty(Collection collection, String message) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new CustomException(message);
        }
    }

    /**
     * Assert that a collection has elements; that is, it must not be
     * <code>null</code> and must have at least one element.
     * <pre class="code">Assert.notEmpty(collection, "Collection must have elements");</pre>
     *
     * @param collection the collection to check
     * @throws CustomException if the collection is <code>null</code> or has no elements
     */
    @SuppressWarnings("rawtypes")
    public static void notEmpty(Collection collection) {
        notEmpty(collection,
                "列表不能为空");
    }

    @SuppressWarnings("rawtypes")
    public static void notEmpty(Collection collection, CustomException throwIfAssertFail) {
        if (CollectionUtils.isEmpty(collection)) {
            throw throwIfAssertFail;
        }
    }

    /**
     * Assert that the provided object is an instance of the provided class.
     * <pre class="code">Assert.instanceOf(Foo.class, foo);</pre>
     *
     * @param type    the type to check against
     * @param obj     the object to check
     * @param message a message which will be prepended to the message produced by
     *                the function itself, and which may be used to provide context. It should
     *                normally end in a ": " or ". " so that the function generate message looks
     *                ok when prepended to it.
     * @throws CustomException if the object is not an instance of clazz
     * @see Class#isInstance
     */
    @SuppressWarnings("rawtypes")
    public static void isInstanceOf(Class type, Object obj, String message) {
        notNull(type, "Type to check against must not be null");
        if (!type.isInstance(obj)) {
            throw new CustomException(message + "Object of class ["
                    + (obj != null ? obj.getClass().getName() : "null") + "] must be an instance of " + type);
        }
    }

    /**
     * Assert that the provided object is an instance of the provided class.
     * <pre class="code">Assert.instanceOf(Foo.class, foo);</pre>
     *
     * @param clazz the required class
     * @param obj   the object to check
     * @throws CustomException if the object is not an instance of clazz
     * @see Class#isInstance
     */
    @SuppressWarnings("rawtypes")
    public static void isInstanceOf(Class clazz, Object obj) {
        isInstanceOf(clazz, obj, "");
    }

    @SuppressWarnings("rawtypes")
    public static void isInstanceOf(Class type, Object obj, CustomException throwIfAssertFail) {
        notNull(type, "Type to check against must not be null");
        if (!type.isInstance(obj)) {
            throw throwIfAssertFail;
        }
    }

    /**
     * Assert that <code>superType.isAssignableFrom(subType)</code> is <code>true</code>.
     * <pre class="code">Assert.isAssignable(Number.class, myClass);</pre>
     *
     * @param superType the super type to check against
     * @param subType   the sub type to check
     * @param message   a message which will be prepended to the message produced by
     *                  the function itself, and which may be used to provide context. It should
     *                  normally end in a ": " or ". " so that the function generate message looks
     *                  ok when prepended to it.
     * @throws CustomException if the classes are not assignable
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void isAssignable(Class superType, Class subType, String message) {
        notNull(superType, "Type to check against must not be null");
        if (subType == null || !superType.isAssignableFrom(subType)) {
            throw new CustomException(message + subType + " is not assignable to " + superType);
        }
    }

    /**
     * Assert that <code>superType.isAssignableFrom(subType)</code> is <code>true</code>.
     * <pre class="code">Assert.isAssignable(Number.class, myClass);</pre>
     *
     * @param superType the super type to check
     * @param subType   the sub type to check
     * @throws CustomException if the classes are not assignable
     */
    @SuppressWarnings("rawtypes")
    public static void isAssignable(Class superType, Class subType) {
        isAssignable(superType, subType, "");
    }

    /**
     * Assert a boolean expression, throwing <code>IllegalStateException</code>
     * if the test result is <code>false</code>. Call isTrue if you wish to
     * throw CustomException on an assertion failure.
     * <pre class="code">Assert.state(id == null, "The id property must not already be initialized");</pre>
     *
     * @param expression a boolean expression
     * @param message    CustomException exception message to use if the assertion fails
     * @throws CustomException if expression is <code>false</code>
     */
    public static void state(boolean expression, String message) {
        if (!expression) {
            throw new CustomException(message);
        }
    }

    /**
     * Assert a boolean expression, throwing {@link CustomException}
     * if the test result is <code>false</code>.
     * <p>Call {@link #isTrue(boolean)} if you wish to
     * throw {@link CustomException} on an assertion failure.
     * <pre class="code">Assert.state(id == null);</pre>
     *
     * @param expression a boolean expression
     * @throws CustomException if the supplied expression is <code>false</code>
     */
    public static void state(boolean expression) {
        state(expression, "[Assertion failed] - this state invariant must be true");
    }
}
