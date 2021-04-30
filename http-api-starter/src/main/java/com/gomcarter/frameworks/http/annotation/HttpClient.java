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
public @interface HttpClient {
    /**
     * 找到配置内容的 keys
     *
     * @return 根据配置中心不同，传入不同的配置 key
     */
    String[] value();

    /**
     * host对应的配置key，如果以http开头，则默认是固定写死的
     */
    String host() default "";

    // 更多配置后续拓展
}
