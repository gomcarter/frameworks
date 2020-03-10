package com.gomcarter.frameworks.config.converter;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Properties类型转换器
 *
 * @author gomcarter on 2019-11-16 13:58:11
 */
public class PropertiesConverter implements Convertable {

    /**
     * @param sourceValue sourceValue
     * @param ignore      ignore
     * @return Properties result
     */
    @Override
    public Properties convert(Object sourceValue, Type ignore) {
        if (sourceValue == null) {
            return null;
        }

        try {
            Properties properties = new Properties();
            InputStream is = new BufferedInputStream(new ByteArrayInputStream(sourceValue.toString().getBytes()));
            BufferedReader bf = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            properties.load(bf);
            return properties;
        } catch (IOException e) {
            logger.error("转换失败：{} ", sourceValue, e);
        }

        return null;
    }
}
