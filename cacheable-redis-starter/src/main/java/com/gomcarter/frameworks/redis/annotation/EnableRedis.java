package com.gomcarter.frameworks.cache.annotation;

import com.gomcarter.frameworks.redis.RedisRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author gomcarter on 2019-09-05 16:00:46
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(RedisRegistrar.class)
public @interface EnableRedis {

    /**
     * 找到配置内容的 keys
     *
     * @return 根据配置中心不同，传入不同的配置 key
     */
    String[] value() default {"CONNECTION", "REDIS"};
}
