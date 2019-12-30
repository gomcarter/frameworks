package com.gomcarter.frameworks.dubbo.annotation;

import com.gomcarter.frameworks.dubbo.factory.DubboRegistryImporter;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 配置中心内容存储注册中心的配置，配置项如下：
 * <p>
 * <b style="color:red">注册中心配置：</b>
 * <p>
 * 配置时以dubbo.registry前缀开始：<a href="http://dubbo.apache.org/zh-cn/docs/user/references/xml/dubbo-registry.html">详情这里</a>，举例：
 * <p>
 * 注册中心地址，如不配置默认取nacos作为注册中心： 如：配置中心为http://10.10.1.22:10000 =&gt; nacos://10.10.1.22:10000
 * <p>
 * dubbo.registry.address=nacos://xx.xx.xx.xx:xxx
 * <p>
 * dubbo.registry.username=username
 * <p>
 * dubbo.registry.password=password
 * <p>
 * <b style="color:red">应用配置</b>
 * <p>
 * 配置时以dubbo.application前缀开始: <a href="http://dubbo.apache.org/zh-cn/docs/user/references/xml/dubbo-application.html">详情这里</a>
 * <p>
 * 举例：
 * <p>
 * dubbo.application.name=appName
 * dubbo.application.qosEnable=false
 * <p>
 * <b style="color:red">协议相关配置</b>
 * <p>
 * 配置时以dubbo.protocol前缀开始：<a href="http://dubbo.apache.org/zh-cn/docs/user/references/xml/dubbo-protocol.html">详情这里</a>
 * <p>
 * 举例：
 * <p>
 * dubbo.protocol.name=dubbo
 *
 * @author gomcarter on 2019-11-15 17:34:26
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(DubboRegistryImporter.class)
public @interface EnableDubbo {
    /**
     * 找到配置内容的 keys
     *
     * @return 根据配置中心不同，传入不同的配置 key
     */
    String[] value() default {"REGISTRY", "DUBBO"};

    /**
     * service 提供的端口
     *
     * @return port
     */
    int port();

    /**
     * 服务app 名称
     *
     * @return appName
     */
    String appName() default "";
}
