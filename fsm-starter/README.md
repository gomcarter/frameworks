### 一、建表
```
CREATE TABLE `fsm_context` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `type` varchar(32) NOT NULL COMMENT '类型',
  `state_from` varchar(50) DEFAULT NULL COMMENT '初始状态',
  `event` varchar(50) DEFAULT NULL COMMENT '操作事件',
  `state_to` varchar(50) DEFAULT NULL COMMENT '到达状态',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modify_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='状态机上下文';

CREATE TABLE `fsm_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `type` varchar(32) NOT NULL COMMENT '类型',
  `object_id` varchar(50) DEFAULT NULL COMMENT '谁操作的',
  `state_from` varchar(50) DEFAULT NULL COMMENT '初始状态',
  `event` varchar(50) DEFAULT NULL COMMENT '操作事件',
  `state_to` varchar(50) DEFAULT NULL COMMENT '到达状态',
  `owner` varchar(50) DEFAULT NULL COMMENT '谁操作的',
  `mark` varchar(2048) DEFAULT NULL COMMENT '备注',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modify_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `type_object_id` (`type`, `object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='状态机日志';
```

### 一、使用
```
引入jar包
 <dependency>
    <groupId>com.gomcarter.frameworks</groupId>
    <artifactId>fsm-starter</artifactId>
    <version>{version}</version>
</dependency>

主表：
import com.gomcarter.frameworks.fsm.entity.FsmObject;

public class FsmFoo implements FsmObject {
    private String state;
    
    // 表内其他字段
    ...
    
    @Override
    public String getState() {
        return state;
    }
}

service：

public class FsmFooService extends AbstractFsmService<FsmFoo> {
    @Resource
    private FsmFooMapper fsmFooMapper;
    
    @Override
    public String type() {
        return "foo";
    }

    @Override
    public FsmObjectMapper<FsmFoo> mapper() {
        return this.fsmFooMapper;
    }
    
    @Override
    protected void onStateChanged(Serializable objectId, Serializable stateFrom, Serializable event, Serializable stateTo, String operator, String mark) {
        log.info("状态变更通知事件：{}, {}, {}, {}, {}, {}", objectId, stateFrom, event, stateTo, operator, mark);
        // 执行业务代码
    }
}
```



    
