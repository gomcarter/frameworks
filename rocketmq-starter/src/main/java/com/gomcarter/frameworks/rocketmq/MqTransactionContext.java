package com.gomcarter.frameworks.rocketmq;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author gomcarter 2021-01-29
 */
@Data
@RequiredArgsConstructor
@Accessors(chain = true)
public class MqTransactionContext {

    @NonNull
    private final Runnable callback;
}
