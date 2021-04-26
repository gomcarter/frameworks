package com.gomcarter.frameworks.mybatis.service;

import com.gomcarter.frameworks.base.pager.Pageable;
import com.gomcarter.frameworks.mybatis.mapper.BaseMapper;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * @author 李银 on 2021-04-26 20:19:19
 */
@Service
public interface BaseService<T> {

    BaseMapper<T> mapper();

    default T insert(T entity) {
        mapper().insert(entity);
        return entity;
    }

    default T insertNoisy(T entity) {
        mapper().insertNoisy(entity);
        return entity;
    }

    default T update(T entity) {
        mapper().update(entity);
        return entity;
    }

    default T updateCas(T entity) {
        mapper().cas(entity);
        return entity;
    }

    default T updateCasNoisy(T entity) {
        mapper().casNoisy(entity);
        return entity;
    }

    default T getById(Long id) {
        return mapper().getById(id);
    }

    default List<T> getByIdList(Collection<Long> idList) {
        return mapper().getByIdList(idList);
    }

    default <P> List<T> query(P params, Pageable pager) {
        return mapper().query(params, pager);
    }

    default <P> Integer count(P params) {
        return mapper().count(params);
    }
}
