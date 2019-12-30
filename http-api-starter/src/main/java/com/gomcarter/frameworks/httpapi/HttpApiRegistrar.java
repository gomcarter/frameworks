package com.gomcarter.frameworks.httpapi;

import com.gomcarter.frameworks.base.common.AssertUtils;
import com.gomcarter.frameworks.base.common.ReflectionUtils;
import com.gomcarter.frameworks.base.config.UnifiedConfigService;
import com.gomcarter.frameworks.httpapi.annotation.EnableHttp;
import com.gomcarter.frameworks.httpapi.annotation.HttpResource;
import com.gomcarter.frameworks.httpapi.proxy.HttpApiProxyHandler;
import lombok.extern.slf4j.Slf4j;
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
            String[] keys = http.value();
            AssertUtils.isTrue(keys.length > 0, new RuntimeException("未配置@EnableHttp 的 value"));

            UnifiedConfigService configService = UnifiedConfigService.getInstance();
            // 从配置中心读取配置
            Properties properties = configService.getConfigAsProperties(keys);
            // 监听配置变化
            configService.addListenerAsProperties(HttpApiProxyHandler::addRouter, keys);

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
