package com.gomcarter.frameworks.rocketmq.consume;

import com.gomcarter.frameworks.rocketmq.util.MsgUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQPushConsumerLifecycleListener;
import org.apache.rocketmq.spring.support.RocketMQMessageConverter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.messaging.converter.SmartMessageConverter;
import org.springframework.messaging.support.MessageBuilder;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author gomcarter 2021-01-29
 */
@Slf4j
public abstract class AbstractRocketMQListener implements RocketMQListener<MessageExt>, RocketMQPushConsumerLifecycleListener, ApplicationContextAware {

    protected Map<String, ListenerHolder> mqListenerMap = Maps.newHashMap();

    protected Map<String, List<String>> topicTags = Maps.newHashMap();

    protected ConfigurableApplicationContext applicationContext;

    @Autowired
    protected RocketMQMessageConverter rocketMQMessageConverter;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }

    @Override
    public void prepareStart(DefaultMQPushConsumer consumer) {
        Map<String, MqListener> listeners = applicationContext.getBeansOfType(MqListener.class);

        listeners.forEach((k, v) -> {
            Type messageType = MsgUtil.getMessageType(v);
            MethodParameter methodParameter = MsgUtil.getMethodParameter(v, messageType, getMessageConverter());
            ListenerHolder old = mqListenerMap.put(v.destination(), new ListenerHolder(v, messageType, methodParameter));
            if (old != null) {
                log.warn("存在两个不同的listener有相同的订阅关系topic={},tags={}", v.topic(), v.tags());
                throw new RuntimeException("不能重复订阅mq消息");
            }

            List<String> tags = topicTags.computeIfAbsent(v.topic(), t -> Lists.newArrayList());
            if (StringUtils.isNotBlank(v.tags())) {
                tags.add(v.tags());
            }
        });

        topicTags.forEach((k, v) -> {
            if (CollectionUtils.isEmpty(v)) {
                subscribe(consumer, k, "*");
            } else {
                subscribe(consumer, k, StringUtils.join(v, "||"));
            }
        });
    }

    private void subscribe(DefaultMQPushConsumer consumer, String topic, String tags) {
        try {
            consumer.subscribe(topic, tags);
        } catch (MQClientException e) {
            throw new RuntimeException("订阅mq失败", e);
        }
    }

    @Override
    public void onMessage(MessageExt message) {
        ListenerHolder listenerHolder = mqListenerMap.get(MsgUtil.genDestination(message.getTopic(), message.getTags()));
        if (listenerHolder == null) {
            listenerHolder = mqListenerMap.get(message.getTopic());
        }

        if (listenerHolder == null) {
            throw new RuntimeException(String.format("未找到listener topic=%s tags=%s msgId=%s", message.getTopic(), message.getTags(), message.getMsgId()));
        }

        log.info("接收到消息msgId={},topic={},tags={}", message.getMsgId(), message.getTopic(), message.getTags());

        listenerHolder.getMqListener().onMessage(doConvertMessage(message, listenerHolder.getMessageType(), listenerHolder.getMethodParameter()));
    }

    protected Object doConvertMessage(MessageExt messageExt, Type messageType, MethodParameter methodParameter) {
        if (Objects.equals(messageType, MessageExt.class)) {
            return messageExt;
        } else {
            String str = new String(messageExt.getBody(), StandardCharsets.UTF_8);
            if (Objects.equals(messageType, String.class)) {
                return str;
            } else {
                // If msgType not string, use objectMapper change it.
                try {
                    if (messageType instanceof Class) {
                        //if the messageType has not Generic Parameter
                        return this.getMessageConverter().fromMessage(MessageBuilder.withPayload(str).build(), (Class<?>) messageType);
                    } else {
                        //if the messageType has Generic Parameter, then use SmartMessageConverter#fromMessage with third parameter "conversionHint".
                        //we have validate the MessageConverter is SmartMessageConverter in this#getMethodParameter.
                        return ((SmartMessageConverter) this.getMessageConverter()).fromMessage(MessageBuilder.withPayload(str).build(), (Class<?>) ((ParameterizedType) messageType).getRawType(), methodParameter);
                    }
                } catch (Exception e) {
                    log.info("convert failed. str:{}, msgType:{}", str, messageType);
                    throw new RuntimeException("cannot convert message to " + messageType, e);
                }
            }
        }
    }

    protected SmartMessageConverter getMessageConverter() {
        return (SmartMessageConverter) this.rocketMQMessageConverter.getMessageConverter();
    }
}
