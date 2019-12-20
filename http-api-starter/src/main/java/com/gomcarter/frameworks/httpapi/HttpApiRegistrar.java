package com.gomcarter.frameworks.httpapi;

import com.gomcarter.frameworks.base.common.AssertUtils;
import com.gomcarter.frameworks.base.common.NacosClientUtils;
import com.gomcarter.frameworks.base.common.ReflectionUtils;
import com.gomcarter.frameworks.httpapi.annotation.EnableHttp;
import com.gomcarter.frameworks.httpapi.annotation.HttpResource;
import com.gomcarter.frameworks.httpapi.proxy.HttpApiProxyHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.WeakHashMap;

/**
 * @author gomcarter on 2019-11-09 23:31:48
 */
@Order
@Slf4j
public class HttpApiRegistrar implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // registry reference
        this.registerHttpApi(bean);

        // registry reference
        this.registerReference(bean);

        return bean;
    }

    private void registerHttpApi(Object bean) {
        EnableHttp http = bean.getClass().getAnnotation(EnableHttp.class);
        if (http != null) {
            String dataId = http.dataId(),
                    group = http.group();
            AssertUtils.isTrue(StringUtils.isNotBlank(dataId), new RuntimeException("未配置：dataId"));
            AssertUtils.isTrue(StringUtils.isNotBlank(group), new RuntimeException("未配置：group"));

            // 从Nacos中读取配置
            Properties properties = NacosClientUtils.getConfigAsProperties(dataId, group);

            // 添加 http url 路由
            HttpApiProxyHandler.addRouter(properties);
        }
    }

    private WeakHashMap<Class, Object> ioc = new WeakHashMap<>();

    private void registerReference(Object bean) {
        for (Field field : ReflectionUtils.findAllField(bean.getClass())) {
            HttpResource reference = field.getAnnotation(HttpResource.class);
            if (reference == null) {
                continue;
            }

            Class<?> apiClass = field.getType();
            Object api = ioc.get(apiClass);
            if (api == null) {
                api = new HttpApiProxyHandler().getProxy(apiClass);
                ioc.put(apiClass, api);
            }

            ReflectionUtils.setField(bean, field, api);
        }
    }
}
