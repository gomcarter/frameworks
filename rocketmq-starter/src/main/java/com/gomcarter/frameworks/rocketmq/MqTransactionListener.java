package com.gomcarter.frameworks.rocketmq;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.apache.rocketmq.spring.support.RocketMQUtil;
import org.springframework.messaging.Message;

/**
 * @author gomcarter 2021-01-29
 */
@Slf4j
public abstract class MqTransactionListener implements RocketMQLocalTransactionListener {

    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object ctx) {
        if (!(ctx instanceof MqTransactionContext)) {
            log.warn("事务状态不合法 {}", msg);
            return RocketMQLocalTransactionState.ROLLBACK;
        }

        MqTransactionContext context = (MqTransactionContext)ctx;

        try {
            context.getCallback().run();

            log.info("execute local msg {}", msg);

            return RocketMQLocalTransactionState.COMMIT;
        } catch (Exception e) {
            log.warn("{}", msg, e);

            return RocketMQLocalTransactionState.UNKNOWN;
        }
    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        String topic = msg.getHeaders().get(RocketMQUtil.toRocketHeaderKey(RocketMQHeaders.TOPIC), String.class);
        String tags = msg.getHeaders().get(RocketMQUtil.toRocketHeaderKey(RocketMQHeaders.TAGS), String.class);

        MqCheckListener checkListener = CheckListenerRegister.getCheckListener(topic, tags);
        if (checkListener == null) {
            log.error("未找到checkListener {}", msg);
            return RocketMQLocalTransactionState.UNKNOWN;
        }

        return checkListener.check(msg);
    }
}
