package com.gomcarter.frameworks.xmlexcel.upload.annotation;

import java.lang.annotation.*;

/**
 * @author gaopeng 2021/2/18
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestFile {

    String name() default "";

    Class<?> dataClass() default Object.class;

    boolean required() default true;
}
