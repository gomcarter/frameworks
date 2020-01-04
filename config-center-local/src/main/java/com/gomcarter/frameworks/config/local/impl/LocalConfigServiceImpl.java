package com.gomcarter.frameworks.config.local.impl;

import com.gomcarter.frameworks.base.config.UnifiedConfigService;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.*;
import java.nio.charset.Charset;
import java.util.function.Consumer;

/**
 * 可以是本地文件，也可以是classpath中的配置文件
 *
 * @author gomcarter on 2019-11-15 15:20:46
 */
public class LocalConfigServiceImpl implements UnifiedConfigService {
    private static final Logger logger = LoggerFactory.getLogger(LocalConfigServiceImpl.class);

    /**
     * not support
     *
     * @return null
     */
    @Override
    public String server() {
        throw new RuntimeException("not support");
    }

    /**
     * not support
     *
     * @return null
     */
    @Override
    public String namespace() {
        throw new RuntimeException("not support");
    }

    /**
     * @param timeoutMs 超时时间，单位：毫秒
     * @param filePath  filePath[0] = 文件路径
     * @return 配置内容
     */
    @Override
    public String getConfig(long timeoutMs, String... filePath) {
        if (filePath == null || filePath.length == 0) {
            throw new RuntimeException("请配置 filePath");
        }

        StringBuilder sb = new StringBuilder();
        for (String path : filePath) {
            try {
                File f = new File(filePath[0]);
                if (f.exists()) {
                    sb.append(FileUtils.readFileToString(f, Charset.defaultCharset()));
                } else {
                    Resource resource = new ClassPathResource(path);

                    sb.append(FileUtils.readFileToString(resource.getFile(), Charset.defaultCharset()));
                }
            } catch (Exception e) {
                logger.error("获取{}配置示例失败：", path, e);
                throw new RuntimeException("获取" + path + "配置示例失败");
            }
        }

        return sb.toString();
    }

    /**
     * 监听配置变化，不支持
     *
     * @param consumer 回调
     * @param filePath filePath[0] = 文件路径
     */
    @Override
    public void addListener(Consumer<String> consumer, String... filePath) {
        // not support
    }

    public static void main(String[] args) {
        UnifiedConfigService configService = UnifiedConfigService.getInstance();

        System.out.println(configService.getClass());

//        String c = configService.getConfig("META-INF/spring.factories");
        String c = configService.getConfig("/Users/liyin/Documents/projects/gomcarter/frameworks/pom.xml");
        System.out.println(c);
    }
}
