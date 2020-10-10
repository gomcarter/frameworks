package com.gomcarter.frameworks.dubbo;

import org.apache.dubbo.common.serialize.hessian2.Hessian2SerializerFactory;
import org.springframework.context.annotation.Configuration;

/**
 * @author gaopeng
 */
@Configuration
public class DubboAutoConfiguration {

    static {
        // 设置该配置，序列化对象不需要继承java.io.Serializable
        Hessian2SerializerFactory.SERIALIZER_FACTORY.setAllowNonSerializable(true);
    }
}
