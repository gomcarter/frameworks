package com.gomcarter.frameworks.mybatis.annotation;

import java.lang.annotation.*;

/**
 * @author gomcarter on 2019-11-09 22:53:32
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Joinables {

    Joinable[] value();
}
