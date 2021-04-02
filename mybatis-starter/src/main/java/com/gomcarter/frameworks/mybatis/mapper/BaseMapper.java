package com.gomcarter.frameworks.mybatis.mapper;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gomcarter.frameworks.base.common.CollectionUtils;
import com.gomcarter.frameworks.base.pager.DefaultPager;
import com.gomcarter.frameworks.base.pager.Pageable;
import com.gomcarter.frameworks.config.utils.ReflectionUtils;
import com.gomcarter.frameworks.mybatis.utils.MapperUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper 继承该接口后，无需编写 mapper.xml 文件，即可获得CRUD功能
 * 也支持 写 mapper.xml 文件
 *
 * @author gomcarter on 2019-12-05 11:51:27
 */
public interface BaseMapper<T> extends com.baomidou.mybatisplus.core.mapper.BaseMapper<T> {

    String ID = "id";

    /**
     * we have to insert success, throw exception if failed
     *
     * @param entity entity
     * @return the entity
     */
    default T insertNoisy(T entity) {
        int inserted = insert(entity);
        if (inserted <= 0) {
            throw new RuntimeException("insert failed.");
        }
        return entity;
    }

    /**
     * compare version and set new entity， throw exception if failed
     *
     * @param entity entity
     */
    default void casNoisy(T entity) {

        if (cas(entity) <= 0) {
            throw new RuntimeException("update failed, the data is expired.");
        }
    }

    /**
     * entity must have the field 'version' and must be a Integer
     * compare version and set new entity
     *
     * @param entity entity
     * @return affect rows
     */
    default int cas(T entity) {
        Field version;
        try {
            version = entity.getClass().getDeclaredField("version");
        } catch (NoSuchFieldException e) {
            return 0;
        }

        Integer oldValue = (Integer) ReflectionUtils.getFieldValue(entity, version);
        Object id = ReflectionUtils.getFieldValue(entity, "id");
        if (id == null) {
            return 0;
        }

        ReflectionUtils.setFieldIfNotMatchConvertIt(entity, version, oldValue == null ? 1 : oldValue + 1);

        int affectRow = update(entity, new QueryWrapper<T>().eq("version", oldValue).eq("id", id));

        if (affectRow <= 0) {
            // 更新失败，把 version 还原
            ReflectionUtils.setField(entity, version, oldValue);
        }

        return affectRow;
    }

    /**
     * 根据 ID 修改
     *
     * @param entity 实体对象 ： 将字段设置成 null，将不会更新该字段。如果你的需求本身就是要将该字段设置成 null
     * @return affect rows
     */
    default int update(T entity) {
        return updateById(entity);
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
        return update(null, new UpdateWrapper<T>().eq(ID, id).set(column, value));
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
        return update(null, new UpdateWrapper<T>().eq(ID, id).lambda().set(column, value));
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
        UpdateWrapper<T> wrapper = new UpdateWrapper<>();
        wrapper.eq(ID, id);

        if (columnsToNull != null && columnsToNull.length > 0) {
            for (String column : columnsToNull) {
                wrapper.set(column, null);
            }
        }

        return update(entity, wrapper);
    }

    /**
     * 根据 ID 修改，数据库主键
     *
     * @param entity        实体对象： 将字段设置成 null，将不会更新该字段。如果你的需求本身就是要将该字段设置成 null，则使用 columnsToNull
     * @param columnsToNull 指定更新为 null 的列名
     * @return affect rows
     */
    default int update(T entity, String... columnsToNull) {
        if (columnsToNull != null && columnsToNull.length > 0) {
            List<Field> idFieldList = ReflectionUtils.findAllField(entity.getClass())
                    .stream()
                    .filter(s -> s.isAnnotationPresent(TableId.class))
                    .collect(Collectors.toList());

            if (CollectionUtils.isEmpty(idFieldList)) {
                throw new RuntimeException("can not find id for entity");
            }

            UpdateWrapper<T> wrapper = new UpdateWrapper<>();
            for (Field field : idFieldList) {
                TableId tableId = field.getAnnotation(TableId.class);
                wrapper.eq(StringUtils.defaultIfBlank(tableId.value(), field.getName()), ReflectionUtils.getFieldValue(entity, field));
            }

            for (String column : columnsToNull) {
                wrapper.set(column, null);
            }

            return update(entity, wrapper);
        }
        return update(entity);
    }

    /**
     * 根据 ID 查询
     *
     * @param id 主键ID
     * @return the entity
     */
    default T getById(Serializable id) {
        if (id == null) {
            return null;
        }
        return selectById(id);
    }

    /**
     * 通过 idList 查询
     *
     * @param idList 主键ID
     * @return the list of entity
     */
    default List<T> getByIdList(Collection<? extends Serializable> idList) {
        if (idList == null || idList.isEmpty()) {
            return new ArrayList<>(0);
        }
        return selectBatchIds(idList);
    }

    /**
     * 复杂查询，只返回第一条数据
     *
     * @param params 查询参数
     * @param <R>    参数类型
     * @return the list of entity
     */
    default <R> T getUnique(R params) {
        return getUnique(params, (String[]) null);
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
        List<T> result = this.query(params, new DefaultPager(1, 1), columns);
        if (result == null || result.size() <= 0) {
            return null;
        }

        return result.get(0);
    }

    /**
     * 分页查询
     * <p>
     * 根据params 构建一个 queryWrapper，具体规则见：{@link com.gomcarter.frameworks.mybatis.annotation.Condition}
     *
     * @param params   查询参数，必须是自行封装的 java 类，不能是简单类型或者包装类或者模板类
     * @param pageable 分页器
     * @param <R>      参数类型
     * @return the list of entity
     */
    default <R> List<T> query(R params, Pageable pageable) {
        return this.query(params, pageable, (String[]) null);
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
        Page<T> page = MapperUtils.buildPage(pageable);
        Wrapper<T> wrapper = MapperUtils.buildQueryWrapper(params, columns);

        IPage<T> result = this.selectPage(page, wrapper);

        return result.getRecords();
    }

    /**
     * 查询总数
     * 根据params 构建一个 queryWrapper，具体规则见：{@link com.gomcarter.frameworks.mybatis.annotation.Condition}
     *
     * @param params 查询参数
     * @param <R>    参数类型
     * @return 总数
     */
    default <R> Integer count(R params) {
        return this.selectCount(MapperUtils.buildQueryWrapper(params));
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
        return this.updateBy(entity, condition, (String[]) null);
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
        return this.update(entity, MapperUtils.buildUpdateWrapper(condition, columnsToNull));
    }
}
