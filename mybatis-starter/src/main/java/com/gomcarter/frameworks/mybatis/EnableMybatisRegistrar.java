package com.gomcarter.frameworks.mybatis;

import com.gomcarter.frameworks.base.common.AssertUtils;
import com.gomcarter.frameworks.base.common.BeanRegistrationUtils;
import com.gomcarter.frameworks.mybatis.annotation.EnableMybatis;
import com.gomcarter.frameworks.mybatis.factory.NacosReadWriteDataSourceFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotationMetadata;

import java.util.HashMap;

/**
 * @author gomcarter on 2019-11-09 23:31:48
 */
@Order
public class EnableMybatisRegistrar implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata,
                                        BeanDefinitionRegistry registry) {

        AnnotationAttributes attributes = AnnotationAttributes
                .fromMap(annotationMetadata.getAnnotationAttributes(EnableMybatis.class.getName()));
        AssertUtils.notNull(attributes, new RuntimeException("未配置：@EnableMybatis"));

        String[] keys = attributes.getStringArray("value");
        if (keys.length == 0) {
            throw new RuntimeException("未配置@EnableMybatis的 value");
        }

        MybatisConfigHolder.DB_TYPE = attributes.getString("dbType");
        MybatisConfigHolder.DAO_XML_PATH = attributes.getString("daoXmlPath");
        MybatisConfigHolder.DAO_BASE_PACKAGE = attributes.getStringArray("daoBasePackage");
        MybatisConfigHolder.TRANSACTION_POINTCUT_EXPRESSION = attributes.getString("transactionPointcut");
        MybatisConfigHolder.TRANSACTION_REQUIRED_NAME_MAP = attributes.getStringArray("transactionRequiredNameMap");

        // 注入datasource
        BeanRegistrationUtils.registerBeanDefinitionIfNotExists(registry, "readWriteDataSource",
                NacosReadWriteDataSourceFactory.class, new HashMap<String, Object>(2, 1) {{
                    put("keys", keys);
                }});

        // 注入事务切面，主从选择切面，mapper注入等
        BeanRegistrationUtils.registerBeanDefinitionIfNotExists(registry, "mybatisConfiguration", MybatisConfiguration.class);
    }


}
