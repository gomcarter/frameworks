package com.gomcarter.frameworks.rocketmq;

import com.gomcarter.frameworks.rocketmq.util.MsgUtil;
import com.google.common.collect.Maps;

import java.util.concurrent.ConcurrentMap;

/**
 * @author gomcarter 2021-01-29
 */
public class CheckListenerRegister {

    private static ConcurrentMap<String, MqCheckListener> checkListeners = Maps.newConcurrentMap();

    public static void registerCheckListener(String topic, MqCheckListener checkListener) {
        registerCheckListener(topic, null, checkListener);
    }

    public static void registerCheckListener(String topic, String tags, MqCheckListener checkListener) {
        String key = MsgUtil.genDestination(topic, tags);

        MqCheckListener old = checkListeners.putIfAbsent(key, checkListener);
        if (old != null) {
            throw new RuntimeException("checkListener重复注册 topic=" + topic + " tags=" + tags);
        }
    }

    public static MqCheckListener getCheckListener(String topic, String tags) {
        return checkListeners.get(MsgUtil.genDestination(topic, tags));
    }


}
