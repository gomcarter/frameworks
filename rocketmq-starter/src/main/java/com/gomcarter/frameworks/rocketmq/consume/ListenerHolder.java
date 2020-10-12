package com.gomcarter.frameworks.rocketmq.consume;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.MethodParameter;

import java.lang.reflect.Type;

/**
 * @author gaopeng
 * @date 2020/6/23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListenerHolder {

    private MqListener mqListener;

    private Type messageType;

    private MethodParameter methodParameter;
}
