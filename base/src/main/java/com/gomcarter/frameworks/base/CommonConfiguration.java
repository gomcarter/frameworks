package com.gomcarter.frameworks.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gomcarter.frameworks.base.aop.CrossAccessFilter;
import com.gomcarter.frameworks.base.spring.CustomMappingJacksonConverter;
import com.gomcarter.frameworks.base.spring.SpringContextHolder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.unit.DataSize;
import org.springframework.web.filter.FormContentFilter;

import javax.servlet.MultipartConfigElement;
import java.nio.charset.Charset;

/**
 * 过滤器配置
 *
 * @author gomcarter on 2018年3月29日 16:29:34
 */
@Configuration
public class CommonConfiguration {

    /**
     * 设置允许可以跨域访问
     *
     * @return CrossAccessFilter
     */
    @Bean
    public CrossAccessFilter crossAccessFilter() {
        return new CrossAccessFilter();
    }

    /**
     * @return FormContentFilter
     */
    @Bean
    public FormContentFilter httpPutFormContentFilter() {
        return new FormContentFilter();
    }

    /**
     * @return SpringContextHolder
     */
    @Bean
    public SpringContextHolder springContextHolder() {
        return new SpringContextHolder();
    }

    /**
     * @return jacksonObjectMapper
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper objectMapper() {
        return new CustomMappingJacksonConverter().getObjectMapper();
    }

    /**
     * 文件上传配置
     *
     * @return multipartConfigElement
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        // 单个文件最大 --- KB, MB
        factory.setMaxFileSize(DataSize.ofKilobytes(3072000));
        // 设置总上传数据总大小
        factory.setMaxRequestSize(DataSize.ofKilobytes(3072000));
        return factory.createMultipartConfig();
    }

    /**
     * @return responseBodyConverter
     */
    @Bean
    public HttpMessageConverter<String> responseBodyConverter() {
        return new StringHttpMessageConverter(Charset.defaultCharset());
    }
}
