package com.gomcarter.frameworks.rocketmq;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author gaopeng
 * @date 2020/6/17
 */
@Data
@RequiredArgsConstructor
@Accessors(chain = true)
public class MqTransactionContext {

    @NonNull
    private final Runnable callback;
}
