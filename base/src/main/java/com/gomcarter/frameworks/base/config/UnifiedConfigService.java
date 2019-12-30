package com.gomcarter.frameworks.base.config;

import com.gomcarter.frameworks.base.converter.Convertable;
import com.gomcarter.frameworks.base.mapper.JsonMapper;
import org.springframework.core.io.support.SpringFactoriesLoader;

import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * 统一配置中心，SPI 接口，提供商在 META-INF/spring.factories 中写下：
 * <p>
 * com.gomcarter.frameworks.base.config.UnifiedConfigService=具体实现类，如：
 * <p>
 * com.gomcarter.frameworks.base.config.UnifiedConfigService=com.gomcarter.frameworks.base.config.NacosConfigServiceImpl
 *
 * @author gomcarter on 2019-12-30 10:30:33
 */
public interface UnifiedConfigService {
    class Holder {
        private static UnifiedConfigService service;

        static {
            List<UnifiedConfigService> serviceList = SpringFactoriesLoader.loadFactories(UnifiedConfigService.class, null);
            if (serviceList.size() != 1) {
                throw new RuntimeException("配置中心只允许设置一个，但是这里设置了" + serviceList.size() + "个");
            }

            service = serviceList.get(0);
        }
    }

    /**
     * 获取配置中心实例
     *
     * @return 配置中心实例
     */
    static UnifiedConfigService getInstance() {
        return Holder.service;
    }

    /**
     * 获取配置中心 server 地址
     *
     * @return 配置中心 server 地址
     */
    String server();

    /**
     * 获取配置中心 namespace
     *
     * @return 配置中心 namespace
     */
    String namespace();

    /**
     * 获取配置信息
     *
     * @param timeoutMs              超时时间，单位：毫秒
     * @param theKeysForConfigCenter 根据不同的配置中心，设置不同的配置参数，详见具体的实现
     * @return 获取配置
     */
    String getConfig(long timeoutMs, String... theKeysForConfigCenter);

    /**
     * 获取配置信息
     *
     * @param theKeysForConfigCenter 根据不同的配置中心，设置不同的配置参数，详见具体的实现
     * @return 获取配置
     */
    default String getConfig(String... theKeysForConfigCenter) {
        return getConfig(5000, theKeysForConfigCenter);
    }

    /**
     * 监听配置变化
     *
     * @param consumer               回调
     * @param theKeysForConfigCenter 根据不同的配置中心，设置不同的配置参数，详见具体的实现
     */
    void addListener(Consumer<String> consumer, String... theKeysForConfigCenter);

    /**
     * 获取配置为一个实际类
     *
     * @param kls                    kls convert to
     * @param <T>                    kls T
     * @param theKeysForConfigCenter 根据不同的配置中心，设置不同的配置参数，详见具体的实现
     * @return config result
     */
    default <T> T getConfigAsObject(Class<T> kls, String... theKeysForConfigCenter) {
        return JsonMapper.buildNonNullMapper().fromJson(getConfig(theKeysForConfigCenter), kls);
    }

    /**
     * 获取配置为一个实际类
     *
     * @param kls                    kls convert to
     * @param <T>                    kls T
     * @param timeoutMs              read time out
     * @param theKeysForConfigCenter 根据不同的配置中心，设置不同的配置参数，详见具体的实现
     * @return config result
     */
    default <T> T getConfigAsObject(Class<T> kls, long timeoutMs, String... theKeysForConfigCenter) {
        return JsonMapper.buildNonNullMapper().fromJson(getConfig(timeoutMs, theKeysForConfigCenter), kls);
    }

    /**
     * 获取配置为一个实际类的 list
     *
     * @param kls                    kls convert to
     * @param <T>                    kls T
     * @param theKeysForConfigCenter 根据不同的配置中心，设置不同的配置参数，详见具体的实现
     * @return config result
     */
    default <T> List<T> getConfigAsListObject(Class<T> kls, String... theKeysForConfigCenter) {
        return JsonMapper.buildNonNullMapper().fromJsonToList(getConfig(theKeysForConfigCenter), kls);
    }

    /**
     * 获取配置为一个实际类的 list
     *
     * @param kls                    kls convert to
     * @param <T>                    kls T
     * @param timeoutMs              read time out
     * @param theKeysForConfigCenter 根据不同的配置中心，设置不同的配置参数，详见具体的实现
     * @return config result
     */
    default <T> List<T> getConfigAsListObject(Class<T> kls, long timeoutMs, String... theKeysForConfigCenter) {
        return JsonMapper.buildNonNullMapper().fromJsonToList(getConfig(timeoutMs, theKeysForConfigCenter), kls);
    }

    /**
     * 获取配置为一个Properties
     *
     * @param theKeysForConfigCenter 根据不同的配置中心，设置不同的配置参数，详见具体的实现
     * @return config result
     */
    default Properties getConfigAsProperties(String... theKeysForConfigCenter) {
        return Convertable.PROPERTIES_CONVERTER.convert(getConfig(theKeysForConfigCenter), null);
    }

    /**
     * 获取配置为一个Properties
     *
     * @param timeoutMs              read time out
     * @param theKeysForConfigCenter 根据不同的配置中心，设置不同的配置参数，详见具体的实现
     * @return config result
     */
    default Properties getConfigAsProperties(long timeoutMs, String... theKeysForConfigCenter) {
        return Convertable.PROPERTIES_CONVERTER.convert(getConfig(timeoutMs, theKeysForConfigCenter), null);
    }

    /**
     * 监听配置为一个实际类的 list
     *
     * @param consumer               consumer
     * @param <T>                    kls T
     * @param kls                    kls convert to
     * @param theKeysForConfigCenter 根据不同的配置中心，设置不同的配置参数，详见具体的实现
     */
    default <T> void addListenerAsListObject(Consumer<List<T>> consumer, Class<T> kls, String... theKeysForConfigCenter) {
        addListener((content) -> consumer.accept(JsonMapper.buildNonNullMapper().fromJsonToList(content, kls)), theKeysForConfigCenter);
    }

    /**
     * 监听配置为一个实际类
     *
     * @param consumer               consumer
     * @param <T>                    kls T
     * @param kls                    kls convert to* @return config result
     * @param theKeysForConfigCenter 根据不同的配置中心，设置不同的配置参数，详见具体的实现
     */
    default <T> void addListenerAsObject(Consumer<T> consumer, Class<T> kls, String... theKeysForConfigCenter) {
        addListener((content) -> consumer.accept(JsonMapper.buildNonNullMapper().fromJson(content, kls)), theKeysForConfigCenter);
    }

    /**
     * 监听配置为一个Properties
     *
     * @param consumer               consumer
     * @param theKeysForConfigCenter 根据不同的配置中心，设置不同的配置参数，详见具体的实现
     */
    default void addListenerAsProperties(Consumer<Properties> consumer, String... theKeysForConfigCenter) {
        addListener((content) -> consumer.accept(Convertable.PROPERTIES_CONVERTER.convert(content, null)), theKeysForConfigCenter);
    }

    static void main(String[] args) {
        System.out.println(getInstance().getConfig("ITEM", "CONFIG"));
    }
}
