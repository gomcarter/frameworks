package com.gomcarter.frameworks.dubbo;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author gaopeng
 * @date 2020/5/20
 */
public class DubboApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    static {
        System.setProperty("dubbo.application.logger", "slf4j");
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {

    }
}
