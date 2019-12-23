/*
 * Copyright (c) 2011-2020, baomidou (jobob@qq.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.gomcarter.frameworks.base.common;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

/**
 * AopUtils Aop工具类， copy from com.baomidou.mybatisplus.extension.toolkit.AopUtils
 *
 * @author Caratacus
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
        if (!ClassUtils.isProxy(proxy.getClass())) {
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

}
