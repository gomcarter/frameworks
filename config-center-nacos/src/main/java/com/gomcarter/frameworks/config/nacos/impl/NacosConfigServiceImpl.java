package com.gomcarter.frameworks.config.nacos.impl;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.gomcarter.frameworks.config.UnifiedConfigService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * @author gomcarter on 2019-11-15 15:20:46
 */
public class NacosConfigServiceImpl implements UnifiedConfigService {
    private static final Logger logger = LoggerFactory.getLogger(NacosConfigServiceImpl.class);

    /**
     * get naocs serverAddr
     * -Dnacos.server.addr=yy is first
     * <p>
     * export NACOS_SERVER_ADDR=yy is backup
     *
     * @return serverAddr
     */
    @Override
    public String server() {
        return server0();
    }

    /**
     * @param timeoutMs      超时时间，单位：毫秒
     * @param dataIdAndGroup dataIdAndGroup[0] = dataId, dataIdAndGroup[1] = group
     * @return 配置内容
     */
    @Override
    public String getConfig(long timeoutMs, String... dataIdAndGroup) {
        if (dataIdAndGroup == null || dataIdAndGroup.length != 2) {
            throw new RuntimeException("请配置 dataId 和 group");
        }
        String dataId = dataIdAndGroup[0],
                group = dataIdAndGroup[1];

        try {
            String content = Holder.configService.getConfig(dataId, group, timeoutMs);
            logger.info("加载nacos配置 dataId: {}, group: {}, content: {}", dataId, group, content);

            return content;
        } catch (NacosException e) {
            logger.error("获取nacos配置示例失败：{} - {}", dataId, group, e);
            throw new RuntimeException("获取nacos配置示例失败");
        }
    }

    /**
     * 监听配置变化
     *
     * @param consumer       回调
     * @param dataIdAndGroup dataIdAndGroup[0] = dataId, dataIdAndGroup[1] = group
     */
    @Override
    public void addListener(Consumer<String> consumer, String... dataIdAndGroup) {
        if (dataIdAndGroup == null || dataIdAndGroup.length != 2) {
            throw new RuntimeException("请配置 dataId 和 group");
        }
        String dataId = dataIdAndGroup[0],
                group = dataIdAndGroup[1];

        try {
            Holder.configService.addListener(dataId, group, new Listener() {
                @Override
                public Executor getExecutor() {
                    return null;
                }

                @Override
                public void receiveConfigInfo(String content) {
                    logger.info("收到nacos配置变化：dataId: {}, group: {}, content: {}", dataId, group, content);
                    consumer.accept(content);
                }
            });
        } catch (NacosException e) {
            logger.error("监听nacos配置示例失败：{} - {}", dataId, group, e);
            throw new RuntimeException("监听nacos配置示例失败");
        }
    }

    private static class Holder {
        static ConfigService configService;

        static {
            String namespace = namespace0();
            Properties properties = new Properties();
            if (StringUtils.isNotBlank(namespace)) {
                properties.put(PropertyKeyConst.NAMESPACE, namespace);
            }
            properties.put(PropertyKeyConst.SERVER_ADDR, server0());
            try {
                configService = NacosFactory.createConfigService(properties);
            } catch (NacosException e) {
                logger.error("获取nacos配置示例失败", e);
            }
        }
    }

    /**
     * get naocs namespace
     * -Dnacos.namespace=xx is first
     * <p>
     * export NACOS_NAMESPACE=xx is backup
     *
     * @return namespace
     */
    @Override
    public String namespace() {
        return namespace0();
    }

    /**
     * get naocs namespace
     * -Dnacos.namespace=xx is first
     * <p>
     * export NACOS_NAMESPACE=xx is backup
     *
     * @return namespace
     */
    private static String namespace0() {
        return StringUtils.defaultIfBlank(System.getProperty("nacos.namespace"), System.getenv("NACOS_NAMESPACE"));
    }

    /**
     * get naocs serverAddr
     * -Dnacos.server.addr=yy is first
     * <p>
     * export NACOS_SERVER_ADDR=yy is backup
     *
     * @return serverAddr
     */
    private static String server0() {
        String serverAddr = StringUtils.defaultIfBlank(System.getProperty("nacos.server.addr"), System.getenv("NACOS_SERVER_ADDR"));
        if (StringUtils.isBlank(serverAddr)) {
            throw new RuntimeException("请指定 -Dnacos.server.addr");
        }

        return serverAddr;
    }
}
