package com.gomcarter.frameworks.fsm.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author gomcarter
 * @date 2020年09月24日11:03:13
 */
@Data
@Accessors(chain = true)
public class FsmContext {

    /**
     * 主键
     */
    private Long id;

    /**
     * 类型
     */
    private String type;

    /**
     * 初始状态
     */
    private String stateFrom;
    /**
     * 操作事件
     */
    private String event;
    /**
     * 到达状态
     */
    private String stateTo;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 修改时间
     */
    private Date modifyTime;
}
