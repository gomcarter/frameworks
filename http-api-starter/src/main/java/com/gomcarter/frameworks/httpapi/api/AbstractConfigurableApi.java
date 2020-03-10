package com.gomcarter.frameworks.httpapi.api;


import com.gomcarter.frameworks.config.UnifiedConfigService;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author gomcarter 2018年2月28日 18:31:53
 */
public abstract class AbstractConfigurableApi extends BaseApi {

    private Map<String, Properties> propertiesMap = new HashMap<>();

    /**
     * 获取连接到配置中心的内容的 keys
     *
     * @return 配置中心的内容的 keys
     */
    public abstract String[] getConfigKeys();

    @Override
    protected Map<String, String> getUrlRouter() {
        Map<String, String> urlRouterMap = new HashMap<>();
        try {
            Properties properties = this.getProperties();
            for (Object key : properties.keySet()) {
                urlRouterMap.put((String) key, properties.getProperty((String) key));
            }
        } catch (IOException e) {
            logger.error("初始化接口地址失败：{}", StringUtils.join(this.getConfigKeys(), ","), e);
        }

        return urlRouterMap;
    }

    synchronized private Properties getProperties() throws IOException {
        String[] keys = getConfigKeys();
        String cacheKey = StringUtils.join(keys, "_");

        Properties properties = propertiesMap.get(cacheKey);
        if (properties != null) {
            return properties;
        }

        properties = UnifiedConfigService.getInstance().getConfigAsProperties(keys);

        propertiesMap.put(cacheKey, properties);

        return properties;
    }
}
