package com.gomcarter.frameworks.xmlexcel;

import com.gomcarter.frameworks.xmlexcel.download.DownloadReturnValueHandler;
import com.gomcarter.frameworks.xmlexcel.upload.UploadArgumentResolver;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * @author gaopeng 2020/8/26
 */
@Configuration
public class UpdownAutoConfiguration implements WebMvcConfigurer, ApplicationContextAware {

    private ConfigurableApplicationContext applicationContext;

    @Override
    public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> handlers) {
        handlers.add(new DownloadReturnValueHandler(applicationContext));
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new UploadArgumentResolver());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }
}
