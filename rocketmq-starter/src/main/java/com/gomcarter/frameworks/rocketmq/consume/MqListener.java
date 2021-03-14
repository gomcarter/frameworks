package com.gomcarter.frameworks.rocketmq.consume;

import com.gomcarter.frameworks.rocketmq.util.MsgUtil;

/**
 * @author gomcarter 2021-01-29
 */
public interface MqListener<T> {

    void onMessage(T message);

    default String destination() {
        return MsgUtil.genDestination(topic(), tags());
    }

    String topic();

    default String tags() {
        return null;
    }
}
