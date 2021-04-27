package com.gomcarter.frameworks.mybatis.service;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gomcarter.frameworks.base.pager.Pageable;
import com.gomcarter.frameworks.mybatis.mapper.BaseMapper;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * @author 李银 on 2021-04-26 20:19:19
 */
public class BaseService<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> {

    /**
     * 插入一条记录
     *
     * @param entity 实体对象
     */
    public T insert(T entity) {
        getBaseMapper().insert(entity);
        return entity;
    }

    /**
     * 删除一条记录
     *
     * @param id 实体对象id
     * @return 删除条数
     */
    public int deleteById(Serializable id) {
        return getBaseMapper().deleteById(id);
    }

    /**
     * 批量删除对象
     *
     * @param idList 实体对象id集合
     * @return 删除条数
     */
    public int deleteBatchIds(Collection<? extends Serializable> idList) {
        return getBaseMapper().deleteBatchIds(idList);
    }

    /**
     * delete entity where condition
     * vn
     *
     * @param condition 查询条件
     * @param <R>       参数类型
     * @return 被删除总数
     */
    public <R> int deleteBy(R condition) {
        return ((BaseMapper<T>) getBaseMapper()).deleteBy(condition);
    }


    /**
     * we have to insert success, throw exception if failed
     *
     * @param entity entity
     * @return the entity
     */
    public int insertNoisy(T entity) {
        return ((BaseMapper<T>) getBaseMapper()).insertNoisy(entity);
    }

    /**
     * 根据 ID 修改
     *
     * @param entity 实体对象
     * @return affect rows
     */
    public int update(T entity) {
        return ((BaseMapper<T>) getBaseMapper()).update(entity);
    }

    /**
     * entity must have the field 'version' and must be a Integer
     * compare version and set new entity
     *
     * @param entity entity
     * @return affect rows
     */
    public int updateCas(T entity) {
        return ((BaseMapper<T>) getBaseMapper()).cas(entity);
    }

    /**
     * compare version and set new entity， throw exception if failed
     *
     * @param entity entity
     */
    public void updateCasNoisy(T entity) {
        ((BaseMapper<T>) getBaseMapper()).casNoisy(entity);
    }

    /**
     * 根据 ID 查询
     *
     * @param id 主键ID
     * @return the entity
     */
    public T getById(Long id) {
        return ((BaseMapper<T>) getBaseMapper()).getById(id);
    }

    /**
     * 通过 idList 查询
     *
     * @param idList 主键ID
     * @return the list of entity
     */
    public List<T> getByIdList(Collection<Long> idList) {
        return ((BaseMapper<T>) getBaseMapper()).getByIdList(idList);
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
    public <P> List<T> query(P params, Pageable pageable) {
        return ((BaseMapper<T>) getBaseMapper()).query(params, pageable);
    }

    /**
     * 查询总数
     * 根据params 构建一个 queryWrapper，具体规则见：{@link com.gomcarter.frameworks.mybatis.annotation.Condition}
     *
     * @param params 查询参数
     * @param <P>    参数类型
     * @return 总数
     */
    public <P> Integer count(P params) {
        return ((BaseMapper<T>) getBaseMapper()).count(params);
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
    public int update(Serializable id, String column, Object value) {
        return ((BaseMapper<T>) getBaseMapper()).update(id, column, value);
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
    public int update(Serializable id, SFunction<T, ?> column, Object value) {
        return ((BaseMapper<T>) getBaseMapper()).update(id, column, value);
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
    public int update(Serializable id, T entity, String... columnsToNull) {
        return ((BaseMapper<T>) getBaseMapper()).update(id, entity, columnsToNull);
    }

    /**
     * 根据 ID 修改，数据库主键
     *
     * @param entity        实体对象： 将字段设置成 null，将不会更新该字段。如果你的需求本身就是要将该字段设置成 null，则使用 columnsToNull
     * @param columnsToNull 指定更新为 null 的列名
     * @return affect rows
     */
    public int update(T entity, String... columnsToNull) {
        return ((BaseMapper<T>) getBaseMapper()).update(entity, columnsToNull);
    }


    /**
     * 复杂查询，只返回第一条数据
     *
     * @param params 查询参数
     * @param <R>    参数类型
     * @return the list of entity
     */
    public <R> T getUnique(R params) {
        return ((BaseMapper<T>) getBaseMapper()).getUnique(params);
    }

    /**
     * 复杂查询，只返回第一条数据
     *
     * @param params  查询参数
     * @param <R>     参数类型
     * @param columns 返回哪些具体数据
     * @return the list of entity
     */
    public <R> T getUnique(R params, String... columns) {
        return ((BaseMapper<T>) getBaseMapper()).getUnique(params, columns);
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
    public <R> List<T> query(R params, Pageable pageable, String... columns) {
        return ((BaseMapper<T>) getBaseMapper()).query(params, pageable, columns);
    }

    /**
     * update entity where condition
     *
     * @param entity    实体对象 (set 条件值，可以为 null): 将字段设置为 null，则改字段不会被更新
     * @param condition 查询条件
     * @param <R>       参数类型
     * @return 被更新总数
     */
    public <R> int updateBy(T entity, R condition) {
        return ((BaseMapper<T>) getBaseMapper()).updateBy(entity, condition);
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
    public <R> int updateBy(T entity, R condition, String... columnsToNull) {
        return ((BaseMapper<T>) getBaseMapper()).updateBy(entity, condition, columnsToNull);
    }
}
