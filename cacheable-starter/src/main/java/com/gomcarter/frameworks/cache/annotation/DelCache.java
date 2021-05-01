package com.gomcarter.frameworks.cache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author gomcarter on 2019-09-05 16:00:46
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DelCache {
    /**
     * @return key
     */
    String value();

    /**
     * 将方法的参数并入到key中（参数将作toString处理，为null则是null字符串）
     * argsIndex为方法本身参数的索引，如argsIndex={0,2}及取方法的第1个和第3个参数拼接到key中。
     * 如果不需要参数拼接到key中，则忽略此参数即可
     *
     * @return argsIndex
     */
    int[] argsIndex() default {};
}
