package com.gomcarter.frameworks.fsm.service;

import com.gomcarter.frameworks.fsm.dao.FsmLogMapper;
import com.gomcarter.frameworks.fsm.dao.FsmObjectMapper;
import com.gomcarter.frameworks.fsm.entity.FsmContext;
import com.gomcarter.frameworks.fsm.entity.FsmLog;
import com.gomcarter.frameworks.fsm.entity.FsmObject;
import com.gomcarter.frameworks.fsm.dao.FsmContextMapper;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * @author gomcarter
 */
@Slf4j
public abstract class AbstractFsmService<T extends FsmObject> {
    @Resource
    private FsmLogMapper fsmLogMapper;

    @Resource
    protected FsmContextMapper fsmContextMapper;

    /**
     * @return 状态机的业务类型
     */
    public abstract String type();

    /**
     * @return 业务对象mapper
     */
    public abstract FsmObjectMapper<T> mapper();

    /**
     * 当到达此状态的时候，各个子类需要处理的事
     *
     * @param objectId  业务id
     * @param stateFrom 从什么状态开始
     * @param event     经历什么事件
     * @param stateTo   到达了什么状态
     * @param operator  操作人
     * @param mark      备注
     */
    protected abstract void onStateChanged(Serializable objectId, String stateFrom, String event, String stateTo,
                                           String operator, String mark);

    private String route(String state, String event) {
        FsmContext context = state == null ? fsmContextMapper.get2(this.type(), event)
                : fsmContextMapper.get1(this.type(), state, event);
        if (context == null) {
            String notice = String.format("当前状态[%s]不能进行[%s]操作！", state, event);
            throw new RuntimeException(notice);
        }

        return context.getStateTo();
    }

    /**
     * 执行事件
     *
     * @param objectId 业务id
     * @param event    执行事件
     * @param operator 操作者
     * @param mark     备注
     */
    public void processEvent(Serializable objectId, String event, String operator, String mark) {
        if (event == null) {
            throw new RuntimeException("event 不能空");
        }

        FsmObject object = this.mapper().getById(objectId);

        String stateFrom = object.getState();
        String stateTo = route(stateFrom, event);
        log.info("fsm process event, object_id:{}, {} + {} => {}, operator: {}, mark: {}",
                objectId, stateFrom, event, stateTo, operator, mark);

        // 更新节点状态
        this.mapper().$fsmUpdateState(objectId, stateTo);

        // 插入操作日志
        this.insertLog(type(), objectId,
                stateFrom, event, stateTo, operator, mark);

        onStateChanged(objectId, stateFrom, event, stateTo, operator, mark);
    }

    public void insertLog(Serializable objectId, String event, String user, String mark) {
        FsmObject object = this.mapper().getById(objectId);

        String stateFrom = object.getState();
        //插入操作日志
        this.insertLog(type(), objectId,
                stateFrom, event, stateFrom, user, mark);
    }


    public void insertLog(String type, Serializable objectId, String stateFrom, String event,
                          String stateTo, String operator, String mark) {
        FsmLog log = new FsmLog()
                .setType(type)
                .setObjectId(objectId.toString())
                .setStateFrom(stateFrom)
                .setEvent(event)
                .setStateTo(stateTo)
                .setMark(mark)
                .setOwner(operator);
        this.fsmLogMapper.insert(log);
    }

    public List<FsmLog> queryLogs(Serializable objectId, String type) {
        return this.fsmLogMapper.query(objectId, type);
    }

    public FsmLog getLog(Long id) {
        return fsmLogMapper.getById(id);
    }
}
