package com.gomcarter.frameworks.mybatis.annotation;

import java.lang.annotation.*;

/**
 * @author gomcarter on 2019-11-09 22:53:32
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface ConfigurableSql {

    /**
     * 找到配置内容的 keys
     *
     * @return 根据配置中心不同，传入不同的配置 key
     */
    String[] value();
}
