package com.gomcarter.frameworks.http.annotation;

import com.gomcarter.frameworks.http.HttpRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author gomcarter
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(HttpRegistrar.class)
public @interface EnableHttps {
    /**
     * 基础包路径
     */
    String[] value() default {};
}
