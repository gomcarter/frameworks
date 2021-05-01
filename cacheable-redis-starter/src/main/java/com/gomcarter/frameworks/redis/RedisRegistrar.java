package com.gomcarter.frameworks.redis;

import com.gomcarter.frameworks.config.utils.BeanRegistrationUtils;
import com.gomcarter.frameworks.cache.annotation.EnableRedis;
import com.gomcarter.frameworks.redis.factory.RedisFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotationMetadata;

import java.util.HashMap;

/**
 * @author  gomcarter on 2019-11-09 23:31:48
 */
@Order
public class RedisRegistrar implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata,
                                        BeanDefinitionRegistry registry) {

        AnnotationAttributes attributes = AnnotationAttributes
                .fromMap(annotationMetadata.getAnnotationAttributes(EnableRedis.class.getName()));
        if (attributes == null) {
            throw new RuntimeException("未配置：EnableRedis");
        }

        String[] keys = attributes.getStringArray("value");

        // 注入redis
        BeanRegistrationUtils.registerBeanDefinitionIfNotExists(registry, "redisProxy",
                RedisFactory.class, new HashMap<String, Object>(1, 1) {{
                    put("keys", keys);
                }});

        // 注入切面
        BeanRegistrationUtils.registerBeanDefinitionIfNotExists(registry, "redisConfiguration", RedisConfiguration.class);
    }


}
