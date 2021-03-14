package com.gomcarter.frameworks.dubbo;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author gomcarter 2021-01-29 09:40:02
 */
public class DubboApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    static {
        System.setProperty("dubbo.application.logger", "slf4j");
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {

    }
}
