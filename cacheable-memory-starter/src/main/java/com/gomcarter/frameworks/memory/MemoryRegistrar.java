package com.gomcarter.frameworks.memory;

import com.gomcarter.frameworks.memory.annotation.EnableMemory;
import com.gomcarter.frameworks.config.utils.BeanRegistrationUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author  gomcarter on 2019-11-09 23:31:48
 */
@Order
public class MemoryRegistrar implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata,
                                        BeanDefinitionRegistry registry) {

        AnnotationAttributes attributes = AnnotationAttributes
                .fromMap(annotationMetadata.getAnnotationAttributes(EnableMemory.class.getName()));
        if (attributes == null) {
            throw new RuntimeException("未配置：EnableRedis");
        }

        String[] keys = attributes.getStringArray("value");

        // 注入切面
        BeanRegistrationUtils.registerBeanDefinitionIfNotExists(registry, "memoryConfiguration", MemoryConfiguration.class);
    }
}
