package com.gomcarter.frameworks.http.factory;

import com.gomcarter.frameworks.config.UnifiedConfigService;
import com.gomcarter.frameworks.http.proxy.HttpApiProxyHandler;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;
import java.util.Properties;

public class HttpClientFactoryBean implements FactoryBean<Object>, InitializingBean, ApplicationContextAware {
    private Class<?> type;
    private Map<String, Object> attributes;
    private Object proxy;
    private ApplicationContext applicationContext;

    @Override
    public Object getObject() {
        if (proxy == null) {
            String[] keys = (String[]) this.attributes.get("value");
            String host = (String) attributes.get("host");
            if (!host.startsWith("http")) {
                Properties properties = UnifiedConfigService.getInstance().getConfigAsProperties(keys);
                host = properties.getProperty(host);
            }
            this.proxy = HttpApiProxyHandler.getProxy(this.type, host);
        }
        return proxy;
    }

    @Override
    public Class<?> getObjectType() {
        return this.type;
    }

    @Override
    public void afterPropertiesSet() {
        System.out.println(this.type);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public Class<?> getType() {
        return type;
    }

    public HttpClientFactoryBean setType(Class<?> type) {
        this.type = type;
        return this;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public HttpClientFactoryBean setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
        return this;
    }
}
