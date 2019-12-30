package com.gomcarter.frameworks.redis.factory;

import com.gomcarter.frameworks.base.config.UnifiedConfigService;
import com.gomcarter.frameworks.redis.tool.RedisProxy;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.Properties;

/**
 * @author gomcarter on 2019-11-19 11:07:19
 */
public class NacosRedisFactory implements FactoryBean<RedisProxy>, InitializingBean {
    private String dataId = "CONNECTION";
    private String group = "REDIS";

    private RedisProxy proxy = new RedisProxy();

    @Override
    public RedisProxy getObject() throws Exception {
        return proxy;
    }

    @Override
    public Class<? extends RedisProxy> getObjectType() {
        return (this.proxy != null ? this.proxy.getClass() : RedisProxy.class);
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        UnifiedConfigService configService = UnifiedConfigService.getInstance();

        configService.addListenerAsProperties((p) -> RedisConnectionBuilder.of(proxy, p), dataId, group);

        Properties properties = configService.getConfigAsProperties(this.dataId, this.group);
        RedisConnectionBuilder.of(proxy, properties);
    }

    public NacosRedisFactory setGroup(String group) {
        this.group = group;
        return this;
    }

    public NacosRedisFactory setDataId(String dataId) {
        this.dataId = dataId;
        return this;
    }
}
