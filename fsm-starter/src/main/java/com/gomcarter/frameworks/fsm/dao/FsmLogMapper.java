package com.gomcarter.frameworks.fsm.dao;

import com.gomcarter.frameworks.fsm.entity.FsmLog;
import com.gomcarter.frameworks.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.io.Serializable;
import java.util.List;

/**
 * @author 李银
 */
public interface FsmLogMapper extends BaseMapper<FsmLog> {

    /**
     * 根据id和type获取状态机日志
     *
     * @param objectId 对象id
     * @param type     业务类型
     * @return 日志
     */
    @Select("select fsm_log where object_id = #{objectId} and type = #{type}")
    List<FsmLog> query(@Param(value = "objectId") Serializable objectId,
                       @Param(value = "type") String type);
}
