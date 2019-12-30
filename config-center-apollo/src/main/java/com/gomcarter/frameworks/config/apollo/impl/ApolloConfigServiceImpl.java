package com.gomcarter.frameworks.config.apollo.impl;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.gomcarter.frameworks.base.common.AssertUtils;
import com.gomcarter.frameworks.base.config.UnifiedConfigService;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * @author gomcarter on 2019-11-15 15:20:46
 */
public class ApolloConfigServiceImpl implements UnifiedConfigService {
    private static final Logger logger = LoggerFactory.getLogger(ApolloConfigServiceImpl.class);

    /**
     * get apollo meta
     * -Dapollo.meta=yy is first
     * <p>
     * export APOLLO_META=yy is backup
     *
     * @return apollo server meta
     */
    @Override
    public String server() {
        return server0();
    }

    /**
     * @param timeoutMs       超时时间，单位：毫秒
     * @param keyAndNamespace keyAndNamespace[0] = key, keyAndNamespace[1] = namespace(or null) namespace 可以不设置
     * @return 配置内容
     */
    @Override
    public String getConfig(long timeoutMs, String... keyAndNamespace) {
        if (keyAndNamespace == null || keyAndNamespace.length == 0) {
            throw new RuntimeException("请配置 key（必填）, namespace（非必填）");
        }
        String key = keyAndNamespace[0];
        Config config = keyAndNamespace.length > 1 ? ConfigService.getConfig(keyAndNamespace[1]) : ConfigService.getAppConfig();

        String content = config.getProperty(key, StringUtils.EMPTY);

        logger.info("加载apollo配置 keyAndNamespace: {}, content: {}", StringUtils.join(keyAndNamespace, "-"), content);
        return content;
    }

    /**
     * 监听配置变化
     *
     * @param consumer        回调
     * @param keyAndNamespace keyAndNamespace[0] = key, keyAndNamespace[1] = namespace(or null) namespace 可以不设置
     */
    @Override
    public void addListener(Consumer<String> consumer, String... keyAndNamespace) {
        if (keyAndNamespace == null || keyAndNamespace.length == 0) {
            throw new RuntimeException("请配置 key（必填）, namespace（非必填）");
        }
        String key = keyAndNamespace[0];
        Config config = keyAndNamespace.length > 1 ? ConfigService.getConfig(keyAndNamespace[1]) : ConfigService.getAppConfig();

        config.addChangeListener(changeEvent -> {
            for (String key1 : changeEvent.changedKeys()) {
                ConfigChange change = changeEvent.getChange(key1);
                String newValue = change.getNewValue();
                logger.info("收到apollo配置变化：keyAndNamespace: {},\nold: {} \nnew: {}", key1 + "-" + change.getNamespace(), change.getOldValue(), newValue);

                consumer.accept(newValue);
            }
        }, Sets.newHashSet(key));
    }

    /**
     * not support
     *
     * @return namespace
     */
    @Override
    public String namespace() {
        return namespace0();
    }

    /**
     * get apollo namespace
     * -Dapollo.namespace=xx is first
     * <p>
     * export APOLLO_NAMESPACE=xx is backup
     *
     * @return namespace or null
     */
    private static String namespace0() {
        return StringUtils.defaultIfBlank(System.getProperty("apollo.namespace"), System.getenv("APOLLO_NAMESPACE"));
    }

    /**
     * get apollo meta
     * -Dapollo.meta=yy is first
     * <p>
     * export APOLLO_META=yy is backup
     *
     * @return serverAddr
     */
    private static String server0() {
        String serverAddr = StringUtils.defaultIfBlank(System.getProperty("apollo.meta"), System.getenv("APOLLO_META"));
        AssertUtils.isTrue(StringUtils.isNotBlank(serverAddr), new RuntimeException("未找到 APOLLO META 信息"));
        return serverAddr;
    }
}
