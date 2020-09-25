package com.gomcarter.frameworks.fsm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author gomcarter
 * @date 2020年09月24日11:03:13
 */
@Data
@Accessors(chain = true)
public class FsmLog {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

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
     * 谁操作的
     */
    private String owner;
    /**
     * 操作对象id
     */
    private String objectId;
    /**
     * 操作对象的业务类型
     */
    private String type;
    /**
     * 备注
     */
    private String mark;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 修改时间
     */
    private Date modifyTime;
}
