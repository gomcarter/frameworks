package com.gomcarter.frameworks.mybatis.factory;

import com.gomcarter.frameworks.base.config.UnifiedConfigService;
import com.gomcarter.frameworks.mybatis.datasource.ReadWriteDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.Properties;

/**
 * 完成从配置中心读取mysql配置
 * 以及读库，主库故障转移
 * 基于druid实现，如果以后用别的数据库连接，需要重新实现
 *
 * @author gomcarter on 2018年2月28日 11:26:07
 */
public class NacosReadWriteDataSourceFactory implements FactoryBean<ReadWriteDataSource>, InitializingBean, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(NacosReadWriteDataSourceFactory.class);

    private String[] keys;

    private ReadWriteDataSource readWriteDataSource = new ReadWriteDataSource();

    @Override
    public ReadWriteDataSource getObject() throws Exception {
        return readWriteDataSource;
    }

    @Override
    public Class<?> getObjectType() {
        return ReadWriteDataSource.class;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        UnifiedConfigService configService = UnifiedConfigService.getInstance();
        configService.addListenerAsProperties((properties -> {
            try {
                this.initDatasource(properties);
            } catch (Exception e) {
                logger.error("update datasource failed:", e);
            }
        }), keys);

        Properties properties = configService.getConfigAsProperties(this.keys);
        this.initDatasource(properties);
    }

    private void initDatasource(Properties properties) throws Exception {
        // 销毁之前的
        this.destroy();

        ReadWriteDataSourceBuilder.createDataSource(this.readWriteDataSource, properties);
    }

    @Override
    public void destroy() throws Exception {
        ReadWriteDataSourceBuilder.destroy(readWriteDataSource);
    }

    public String[] getKeys() {
        return keys;
    }

    public NacosReadWriteDataSourceFactory setKeys(String[] keys) {
        this.keys = keys;
        return this;
    }
}
