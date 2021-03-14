package com.gomcarter.frameworks.rocketmq;

import com.gomcarter.frameworks.rocketmq.util.MsgUtil;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.messaging.Message;

import javax.annotation.PostConstruct;

/**
 * @author gomcarter 2021-01-29
 */
public interface MqCheckListener<T> {

    @PostConstruct
    default void register() {
        CheckListenerRegister.registerCheckListener(topic(), tags(), this);
    }

    default String destination() {
        return MsgUtil.genDestination(topic(), tags());
    }

    String topic();

    default String tags() {
        return null;
    }

    RocketMQLocalTransactionState check(Message<T> msg);
}
