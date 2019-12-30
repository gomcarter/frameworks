package com.gomcarter.frameworks.base.annotation;

import com.gomcarter.frameworks.base.config.BeanFieldConfigurableProcessor;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 自动注入配置到变量
 * {@link ConfigurableValue}
 *
 * @author gomcarter on 2019-11-15 17:34:26
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(BeanFieldConfigurableProcessor.class)
public @interface EnableValueAutoConfiguration {

}
