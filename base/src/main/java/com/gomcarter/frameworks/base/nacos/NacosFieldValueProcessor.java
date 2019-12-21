package com.gomcarter.frameworks.base.nacos;

import com.gomcarter.frameworks.base.annotation.QiangDaNacosValue;
import com.gomcarter.frameworks.base.common.NacosClientUtils;
import com.gomcarter.frameworks.base.common.ReflectionUtils;
import com.gomcarter.frameworks.base.converter.Convertable;
import com.gomcarter.frameworks.base.spring.BeanFieldAndMethodProcessor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author gomcarter on 2019-11-15 17:43:37
 */
public class NacosFieldValueProcessor extends BeanFieldAndMethodProcessor implements ApplicationListener<ContextRefreshedEvent> {

    private WeakHashMap<String, String> configMap = new WeakHashMap<>();

    private Map<String, List<Consumer<String>>> comsumerMap = new HashMap<>();

    /**
     * @param bean     bean
     * @param beanName beanName
     * @param field    some one field of bean
     */
    @Override
    protected final void processField(Object bean, String beanName, Field field) {

        QiangDaNacosValue qiangDaNacosValue = field.getAnnotation(QiangDaNacosValue.class);
        if (qiangDaNacosValue == null) {
            return;
        }

        // 获取表达式
        String expression = qiangDaNacosValue.value();
        String[] expressionArray = expression.split("\\.");
        // 校验
        if (expressionArray.length < 2) {
            throw new RuntimeException(bean.getClass().getName() + "." + field.getName() + "配置的表达式不正确");
        }

        // 获取dataId， group， 还有配置内容里面的key（如果没有key，那么所有配置内容都归这个变量）
        String[] keyArray = new String[expressionArray.length - 2];
        System.arraycopy(expressionArray, 2, keyArray, 0, expressionArray.length - 2);
        String group = expressionArray[0],
                dataId = expressionArray[1],
                key = StringUtils.join(keyArray, "."),
                cacheKey = dataId + "_" + group;

        // 每个 dataid，group 缓存起来
        String config = configMap.get(cacheKey);
        if (config == null) {
            config = NacosClientUtils.getConfigAsString(dataId, group);
            configMap.put(cacheKey, config);

            NacosClientUtils.addListener(dataId, group, (c) -> {
                List<Consumer<String>> consumerList = comsumerMap.get(cacheKey);
                if (consumerList != null) {
                    consumerList.forEach(d -> d.accept(c));
                }
            });
        }

        // 需要自动刷新
        if (qiangDaNacosValue.autoRefreshed()) {
            List<Consumer<String>> consumerList = comsumerMap.computeIfAbsent(cacheKey, k -> new ArrayList<>());

            consumerList.add(c -> fillField(bean, c, key, field));
        }

        fillField(bean, config, key, field);
    }

    private void fillField(Object bean, String config, String key, Field field) {
        String value;
        if (StringUtils.isNotBlank(key)) {
            Properties properties = Convertable.PROPERTIES_CONVERTER.convert(config, null);
            // properties配置中的某一项配置属于这个变量
            value = properties.getProperty(key);
        } else {
            // 整个dataId + group的数据都属于这个变量
            value = config;
        }

        ReflectionUtils.setFieldIfNotMatchConvertIt(bean, field, value);
    }

    /**
     * @param bean     bean
     * @param beanName beanName
     * @param method   some one method bean
     */
    @Override
    protected final void processMethod(Object bean, String beanName, Method method) {
        // do nothing
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        //root application context 没有parent
        if (event.getApplicationContext().getParent() == null) {
            configMap = null;
        }
    }
}
