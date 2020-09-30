package com.gomcarter.frameworks.fsm.dao;

import com.gomcarter.frameworks.fsm.entity.FsmContext;
import com.gomcarter.frameworks.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author 李银
 */
public interface FsmContextMapper extends BaseMapper<FsmContext> {

    /**
     * type获取状态机上下文
     *
     * @param type      类型
     * @param stateFrom 初始状态
     * @param event     结束状态
     * @return 上下文
     */
    @Select("select" +
            " * " +
            " from fsm_context" +
            " where" +
            " `type` = #{type} and state_from = #{stateFrom} and event = #{event}")
    FsmContext get1(@Param(value = "type") String type,
                    @Param(value = "stateFrom") String stateFrom,
                    @Param(value = "event") String event);

    /**
     * type获取状态机上下文
     *
     * @param type  类型
     * @param event 结束状态
     * @return 上下文
     */
    @Select("select" +
            " * " +
            " from fsm_context" +
            " where" +
            " `type` = #{type} and state_from is null and event = #{event}")
    FsmContext get2(@Param(value = "type") String type,
                    @Param(value = "event") String event);
}
