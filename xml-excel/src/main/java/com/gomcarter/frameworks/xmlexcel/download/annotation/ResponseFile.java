package com.gomcarter.frameworks.xmlexcel.download.annotation;

import java.lang.annotation.*;

/**
 * @author gaopeng 2021/2/18
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ResponseFile {

    /**
     * 文件名
     */
    String name() default "";

    /**
     * 是否异步下载文件
     */
    boolean async() default false;
}
