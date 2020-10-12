package com.gomcarter.frameworks.rocketmq.util;

import com.gomcarter.frameworks.rocketmq.consume.MqListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.core.MethodParameter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.converter.SmartMessageConverter;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * @author gaopeng
 * @date 2020/6/23
 */
@Slf4j
public class MsgUtil {

    public static String genDestination(String topic) {
        return genDestination(topic, null);
    }

    public static String genDestination(String topic, String tags) {
        if (StringUtils.isBlank(topic)) {
            throw new IllegalArgumentException("topic不能为空");
        }

        String key;
        if (StringUtils.isBlank(tags)) {
            key = topic;
        } else {
            key = topic + ":" + tags;
        }

        return key;
    }

    public static Type getMessageType(MqListener listener) {
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(listener);

        Type matchedGenericInterface = null;
        while (Objects.nonNull(targetClass)) {
            Type[] interfaces = targetClass.getGenericInterfaces();
            if (Objects.nonNull(interfaces)) {
                for (Type type : interfaces) {
                    if (type instanceof ParameterizedType &&
                            (Objects.equals(((ParameterizedType) type).getRawType(), MqListener.class))) {
                        matchedGenericInterface = type;
                        break;
                    }
                }
            }
            targetClass = targetClass.getSuperclass();
        }
        if (Objects.isNull(matchedGenericInterface)) {
            return Object.class;
        }

        Type[] actualTypeArguments = ((ParameterizedType) matchedGenericInterface).getActualTypeArguments();
        if (Objects.nonNull(actualTypeArguments) && actualTypeArguments.length > 0) {
            return actualTypeArguments[0];
        }

        return Object.class;
    }

    public static MethodParameter getMethodParameter(MqListener listener, Type messageType, MessageConverter messageConverter) {
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(listener);

        Class clazz = null;
        if (messageType instanceof ParameterizedType && messageConverter instanceof SmartMessageConverter) {
            clazz = (Class) ((ParameterizedType) messageType).getRawType();
        } else if (messageType instanceof Class) {
            clazz = (Class) messageType;
        } else {
            throw new RuntimeException("parameterType:" + messageType + " of onMessage method is not supported");
        }
        try {
            final Method method = targetClass.getMethod("onMessage", clazz);
            return new MethodParameter(method, 0);
        } catch (NoSuchMethodException e) {
            log.info("", e);
            throw new RuntimeException("parameterType:" + messageType + " of onMessage method is not supported");
        }
    }
}
