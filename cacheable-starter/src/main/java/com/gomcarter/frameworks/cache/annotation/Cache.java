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
public @interface Cache {
    /**
     * 缓存到redis中的key
     *
     * @return key
     */
    String value() default "";

    /**
     * 缓存过期时间，默认5分钟：   60 * 5 = 300
     * <p>
     * 小于0表示永久
     *
     * @return time
     */
    int time() default 0x12C;

    /**
     * 将方法的参数并入到key中（参数将作toString处理，为null则是null字符串）
     * argsIndex为方法本身参数的索引，如argsIndex={0,2}及取方法的第1个和第3个参数拼接到key中。
     * 如果不需要参数拼接到key中，则忽略此参数即可
     *
     * @return argsIndex
     */
    int[] argsIndex() default {};

    /**
     * 是否缓存空数据
     *
     * @return nullable
     */
    boolean nullable() default true;

    /**
     * <p>
     * 如果前面已经存在一个线程在调用此方法，当前线程是否等待，单位：毫秒；
     * <p>
     * 为了线程阻塞严重，使用short类型，所以最大等待时间为32秒左右
     * </p>
     * <p>
     * 小于0等于0：不等待（直接返回服务器繁忙）；
     * <p>
     * 大于0：表示最大等待时间（默认10s）
     * </p>
     *
     * @return await
     */
    short await() default 10000;
}
