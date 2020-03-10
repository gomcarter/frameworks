package com.gomcarter.frameworks.config.utils;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * AopUtils Aop工具类， copy from com.baomidou.mybatisplus.extension.toolkit.AopUtils
 *
 * @author gomcarter
 */
@Slf4j
public class AopUtils {
    public static final String CGLIB_CLASS_SEPARATOR = "$$";

    /**
     * 对于被cglib AOP过的对象, 取得真实的Class类型.
     *
     * @param clazz clazz
     * @return the real Class
     */
    public static Class<?> getTargetClass(Class<?> clazz) {
        if (clazz != null && clazz.getName().contains(CGLIB_CLASS_SEPARATOR)) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null && !Object.class.equals(superClass)) {
                return superClass;
            }
        }
        return clazz;
    }

    /**
     * 获取源目标对象
     *
     * @param proxy ignore
     * @param <T>   ignore
     * @return ignore
     */
    public static <T> T getTargetObject(T proxy) {
        if (!isProxy(proxy.getClass())) {
            return proxy;
        }
        try {
            if (org.springframework.aop.support.AopUtils.isJdkDynamicProxy(proxy)) {
                return getJdkDynamicProxyTargetObject(proxy);
            } else if (org.springframework.aop.support.AopUtils.isCglibProxy(proxy)) {
                return getCglibProxyTargetObject(proxy);
            } else {
                log.warn("Warn: The proxy object processing method is not supported.");
                return proxy;
            }
        } catch (Exception e) {
            throw new RuntimeException("Error: Get proxy targetObject exception !  Cause:", e);
        }
    }

    /**
     * 获取Cglib源目标对象
     *
     * @param proxy ignore
     * @param <T>   ignore
     * @return ignore
     */
    @SuppressWarnings("unchecked")
    private static <T> T getCglibProxyTargetObject(T proxy) throws Exception {
        Field cglibField = proxy.getClass().getDeclaredField("CGLIB$CALLBACK_0");
        cglibField.setAccessible(true);
        Object dynamicAdvisedInterceptor = cglibField.get(proxy);
        Field advised = dynamicAdvisedInterceptor.getClass().getDeclaredField("advised");
        advised.setAccessible(true);
        Object target = ((org.springframework.aop.framework.AdvisedSupport) advised.get(dynamicAdvisedInterceptor)).getTargetSource().getTarget();
        return (T) target;
    }

    /**
     * 获取JdkDynamic源目标对象
     *
     * @param proxy ignore
     * @param <T>   ignore
     * @return ignore
     */
    @SuppressWarnings("unchecked")
    private static <T> T getJdkDynamicProxyTargetObject(T proxy) throws Exception {
        Field jdkDynamicField = proxy.getClass().getSuperclass().getDeclaredField("jdkDynamicField");
        jdkDynamicField.setAccessible(true);
        org.springframework.aop.framework.AopProxy aopProxy = (org.springframework.aop.framework.AopProxy) jdkDynamicField.get(proxy);
        Field advised = aopProxy.getClass().getDeclaredField("advised");
        advised.setAccessible(true);
        Object target = ((org.springframework.aop.framework.AdvisedSupport) advised.get(aopProxy)).getTargetSource().getTarget();
        return (T) target;
    }

    private static final char PACKAGE_SEPARATOR = '.';

    /**
     * 代理 class 的名称
     */
    private static final List<String> PROXY_CLASS_NAMES = Arrays.asList("net.sf.cglib.proxy.Factory"
            // cglib
            , "org.springframework.cglib.proxy.Factory"
            , "javassist.util.proxy.ProxyObject"
            // javassist
            , "org.apache.ibatis.javassist.util.proxy.ProxyObject");

    /**
     * <p>
     * 判断是否为代理对象
     * </p>
     *
     * @param clazz 传入 class 对象
     * @return 如果对象class是代理 class，返回 true
     */
    public static boolean isProxy(Class<?> clazz) {
        if (clazz != null) {
            for (Class<?> cls : clazz.getInterfaces()) {
                if (PROXY_CLASS_NAMES.contains(cls.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * <p>
     * 获取当前对象的 class
     * </p>
     *
     * @param clazz 传入
     * @return 如果是代理的class，返回父 class，否则返回自身
     */
    public static Class<?> getUserClass(Class<?> clazz) {
        return isProxy(clazz) ? clazz.getSuperclass() : clazz;
    }

    /**
     * <p>
     * 获取当前对象的class
     * </p>
     *
     * @param object 对象
     * @return 返回对象的 user class
     */
    public static Class<?> getUserClass(Object object) {
        if (object == null) {
            throw new RuntimeException("Error: Instance must not be null");
        }
        return getUserClass(object.getClass());
    }

    /**
     * <p>
     * 请仅在确定类存在的情况下调用该方法
     * </p>
     *
     * @param name 类名称
     * @return 返回转换后的 Class
     */
    public static Class<?> toClassConfident(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("找不到指定的class！请仅在明确确定会有 class 的时候，调用该方法", e);
        }
    }


    /**
     * Determine the name of the package of the given class,
     * e.g. "java.lang" for the {@code java.lang.String} class.
     *
     * @param clazz the class
     * @return the package name, or the empty String if the class
     * is defined in the default package
     */
    public static String getPackageName(Class<?> clazz) {
        if (clazz == null) {
            throw new RuntimeException("Class must not be null");
        }
        return getPackageName(clazz.getName());
    }

    /**
     * Determine the name of the package of the given fully-qualified class name,
     * e.g. "java.lang" for the {@code java.lang.String} class name.
     *
     * @param fqClassName the fully-qualified class name
     * @return the package name, or the empty String if the class
     * is defined in the default package
     */
    public static String getPackageName(String fqClassName) {
        if (fqClassName == null) {
            throw new RuntimeException("Class name must not be null");
        }

        int lastDotIndex = fqClassName.lastIndexOf(PACKAGE_SEPARATOR);
        return (lastDotIndex != -1 ? fqClassName.substring(0, lastDotIndex) : "");
    }
}
