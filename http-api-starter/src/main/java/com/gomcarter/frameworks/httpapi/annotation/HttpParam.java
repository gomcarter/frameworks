package com.gomcarter.frameworks.httpapi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author gomcarter
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpParam {

    /**
     * @return 参数名称
     */
    String value() default "";

    /**
     * @return 参数类型
     */
    ParamType type() default ParamType.DEFAULT;

    /**
     * Whether param is required.
     * <p>Default is {@code true}, leading to an exception thrown in case
     * there is no param. Switch this to {@code false} if you prefer
     * {@code null} to be passed when the param is {@code null}.
     *
     * @return true -- param is required, false -- param is not required
     */
    boolean required() default true;

    /**
     * @return default value
     */
    String defaultValue() default "";
}
