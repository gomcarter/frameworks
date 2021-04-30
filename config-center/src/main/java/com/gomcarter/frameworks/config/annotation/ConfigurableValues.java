package com.gomcarter.frameworks.config.annotation;

import java.lang.annotation.*;

/**
 * 先在入口Application加上  {@link EnableValueAutoConfiguration}
 *
 * @author gomcarter on 2019-11-15 17:34:26
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigurableValues {
    /**
     * 见：ConfigurableValue
     */
    String[] value();
}
