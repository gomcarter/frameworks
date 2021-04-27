package com.gomcarter.frameworks.mybatis.service;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.gomcarter.frameworks.base.pager.Pageable;
import com.gomcarter.frameworks.mybatis.mapper.BaseMapper;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * @author 李银 on 2021-04-26 20:19:19
 */
@Service
public interface BaseService<T> {

    BaseMapper<T> mapper();

    /**
     * 插入一条记录
     *
     * @param entity 实体对象
     */
    default T insert(T entity) {
        mapper().insert(entity);
        return entity;
    }

    /**
     * 删除一条记录
     *
     * @param id 实体对象id
     * @return 删除条数
     */
    default int deleteById(Serializable id) {
        return mapper().deleteById(id);
    }

    /**
     * 批量删除对象
     *
     * @param idList 实体对象id集合
     * @return 删除条数
     */
    default int deleteBatchIds(Collection<? extends Serializable> idList) {
        return mapper().deleteBatchIds(idList);
    }

    /**
     * delete entity where condition
     *
     * @param condition 查询条件
     * @param <R>       参数类型
     * @return 被删除总数
     */
    default <R> int deleteBy(R condition) {
        return mapper().deleteBy(condition);
    }


    /**
     * we have to insert success, throw exception if failed
     *
     * @param entity entity
     * @return the entity
     */
    default T insertNoisy(T entity) {
        return mapper().insertNoisy(entity);
    }

    /**
     * 根据 ID 修改
     *
     * @param entity 实体对象
     * @return affect rows
     */
    default int update(T entity) {
        return mapper().update(entity);
    }

    /**
     * entity must have the field 'version' and must be a Integer
     * compare version and set new entity
     *
     * @param entity entity
     * @return affect rows
     */
    default int updateCas(T entity) {
        return mapper().cas(entity);
    }

    /**
     * compare version and set new entity， throw exception if failed
     *
     * @param entity entity
     */
    default void updateCasNoisy(T entity) {
        mapper().casNoisy(entity);
    }

    /**
     * 根据 ID 查询
     *
     * @param id 主键ID
     * @return the entity
     */
    default T getById(Long id) {
        return mapper().getById(id);
    }

    /**
     * 通过 idList 查询
     *
     * @param idList 主键ID
     * @return the list of entity
     */
    default List<T> getByIdList(Collection<Long> idList) {
        return mapper().getByIdList(idList);
    }

    /**
     * 分页查询
     * <p>
     * 根据params 构建一个 queryWrapper，具体规则见：{@link com.gomcarter.frameworks.mybatis.annotation.Condition}
     *
     * @param params   查询参数，必须是自行封装的 java 类，不能是简单类型或者包装类或者模板类
     * @param pageable 分页器
     * @param <P>      参数类型
     * @return the list of entity
     */
    default <P> List<T> query(P params, Pageable pageable) {
        return mapper().query(params, pageable);
    }

    /**
     * 查询总数
     * 根据params 构建一个 queryWrapper，具体规则见：{@link com.gomcarter.frameworks.mybatis.annotation.Condition}
     *
     * @param params 查询参数
     * @param <P>    参数类型
     * @return 总数
     */
    default <P> Integer count(P params) {
        return mapper().count(params);
    }

    /**
     * 根据 ID 修改，数据库主键
     * 限定 ： 主键字段必须是 id
     *
     * @param id     指定主键
     * @param column 需要更新的字段
     * @param value  更新成为什么值
     * @return affect rows
     */
    default int update(Serializable id, String column, Object value) {
        return mapper().update(id, column, value);
    }

    /**
     * 根据 ID 修改，数据库主键
     * 限定 ： 主键字段必须是 id
     *
     * @param id     指定主键
     * @param column 需要更新的字段
     * @param value  更新成为什么值
     * @return affect rows
     */
    default int update(Serializable id, SFunction<T, ?> column, Object value) {
        return mapper().update(id, column, value);
    }

    /**
     * 根据 ID 修改，数据库主键
     * 限定 ： 主键字段必须是 id
     *
     * @param id            主键
     * @param entity        实体对象： 将字段设置成 null，将不会更新该字段。如果你的需求本身就是要将该字段设置成 null，则使用 columnsToNull
     * @param columnsToNull 指定更新为 null 的列名
     * @return affect rows
     */
    default int update(Serializable id, T entity, String... columnsToNull) {
        return mapper().update(id, entity, columnsToNull);
    }

    /**
     * 根据 ID 修改，数据库主键
     *
     * @param entity        实体对象： 将字段设置成 null，将不会更新该字段。如果你的需求本身就是要将该字段设置成 null，则使用 columnsToNull
     * @param columnsToNull 指定更新为 null 的列名
     * @return affect rows
     */
    default int update(T entity, String... columnsToNull) {
        return mapper().update(entity, columnsToNull);
    }


    /**
     * 复杂查询，只返回第一条数据
     *
     * @param params 查询参数
     * @param <R>    参数类型
     * @return the list of entity
     */
    default <R> T getUnique(R params) {
        return mapper().getUnique(params);
    }

    /**
     * 复杂查询，只返回第一条数据
     *
     * @param params  查询参数
     * @param <R>     参数类型
     * @param columns 返回哪些具体数据
     * @return the list of entity
     */
    default <R> T getUnique(R params, String... columns) {
        return mapper().getUnique(params, columns);
    }

    /**
     * 分页查询
     * <p>
     * 根据params 构建一个 queryWrapper，具体规则见：{@link com.gomcarter.frameworks.mybatis.annotation.Condition}
     *
     * @param params   查询参数，必须是自行封装的 java 类，不能是简单类型或者包装类或者模板类
     * @param pageable 分页器
     * @param columns  需要查询的列
     * @param <R>      参数类型
     * @return the list of entity
     */
    default <R> List<T> query(R params, Pageable pageable, String... columns) {
        return mapper().query(params, pageable, columns);
    }

    /**
     * update entity where condition
     *
     * @param entity    实体对象 (set 条件值，可以为 null): 将字段设置为 null，则改字段不会被更新
     * @param condition 查询条件
     * @param <R>       参数类型
     * @return 被更新总数
     */
    default <R> int updateBy(T entity, R condition) {
        return mapper().updateBy(entity, condition);
    }

    /**
     * update entity where condition
     *
     * @param entity        实体对象 (set 条件值，可以为 null): 将字段设置为 null，则改字段不会被更新； 如果设置字段为 null，请设置 columnsToNull
     * @param condition     查询条件
     * @param columnsToNull 把什么列更新为 null
     * @param <R>           参数类型
     * @return 被更新总数
     */
    default <R> int updateBy(T entity, R condition, String... columnsToNull) {
        return mapper().updateBy(entity, condition, columnsToNull);
    }
}
