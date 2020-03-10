package com.gomcarter.frameworks.config.diamond.impl;

import com.gomcarter.frameworks.config.UnifiedConfigService;
import com.taobao.diamond.client.DiamondConfigure;
import com.taobao.diamond.client.DiamondConfigureUtil;
import com.taobao.diamond.client.impl.DiamondEnv;
import com.taobao.diamond.common.Constants;
import com.taobao.diamond.manager.ManagerListener;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * @author gomcarter on 2019-11-15 15:20:46
 */
public class DiamondConfigServiceImpl implements UnifiedConfigService {
    /**
     * export DIAMOND_CONFIG_SERVER_ADDRESS=yy
     * export DIAMOND_CONFIG_SERVER_PORT=8401
     *
     * @return DIAMOND_CONFIG_SERVER_ADDRESS + ":" + DIAMOND_CONFIG_SERVER_PORT
     */
    @Override
    public String server() {
        DiamondConfigure diamondConfigure = DiamondConfigureUtil.getFromEnv();
        return diamondConfigure.getConfigServerAddress() + ":" + diamondConfigure.getConfigServerPort();
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
            DiamondEnv diamondEnv = DiamondEnv.instance();
            return diamondEnv.getConfig(dataId, group, Constants.GETCONFIG_LOCAL_SERVER_SNAPSHOT, timeoutMs);
        } catch (IOException e) {
            throw new RuntimeException("获取diamond配置示例失败", e);
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

        DiamondEnv diamondEnv = DiamondEnv.instance();
        diamondEnv.addListeners(dataId, group, new ManagerListener() {
            @Override
            public Executor getExecutor() {
                return null;
            }

            @Override
            public void receiveConfigInfo(String content) {
                consumer.accept(content);
            }
        });
    }

    /**
     * not support
     *
     * @return namespace
     */
    @Override
    public String namespace() {
        return null;
    }
}
