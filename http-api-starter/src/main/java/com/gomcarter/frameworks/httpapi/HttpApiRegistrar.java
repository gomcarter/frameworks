package com.gomcarter.frameworks.httpapi;

import com.gomcarter.frameworks.config.UnifiedConfigService;
import com.gomcarter.frameworks.config.utils.ReflectionUtils;
import com.gomcarter.frameworks.httpapi.annotation.HttpBean;
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
        this.registerReference(bean);

        return bean;
    }

    private WeakHashMap<Class, Object> ioc = new WeakHashMap<>();
    private WeakHashMap<String, Boolean> cache = new WeakHashMap<>();

    private void registerReference(Object bean) {
        for (Field field : ReflectionUtils.findAllField(bean.getClass())) {
            HttpResource reference = field.getAnnotation(HttpResource.class);
            if (reference == null) {
                continue;
            }

            Class<?> apiClass = field.getType();

            HttpBean httpBean = apiClass.getAnnotation(HttpBean.class);
            if (httpBean != null) {
                String[] keys = httpBean.value();
                if (keys.length == 0) {
                    throw new RuntimeException("未配置@EnableHttp 的 value");
                }

                String cacheKey = StringUtils.join(keys, ",");
                if (cache.get(cacheKey) == null) {
                    UnifiedConfigService configService = UnifiedConfigService.getInstance();
                    // 从配置中心读取配置
                    Properties properties = configService.getConfigAsProperties(keys);
                    // 监听配置变化
                    configService.addListenerAsProperties(HttpApiProxyHandler::addRouter, keys);

                    // 添加 http url 路由
                    HttpApiProxyHandler.addRouter(properties);

                    // 缓存起来
                    cache.put(cacheKey, true);
                }
            }

            Object api = ioc.get(apiClass);
            if (api == null) {
                api = new HttpApiProxyHandler().getProxy(apiClass);
                ioc.put(apiClass, api);
            }

            ReflectionUtils.setField(bean, field, api);
        }
    }
}
