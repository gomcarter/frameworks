package com.gomcarter.frameworks.mybatis.utils;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gomcarter.frameworks.base.common.CustomStringUtils;
import com.gomcarter.frameworks.base.pager.Pageable;
import com.gomcarter.frameworks.config.converter.Convertable;
import com.gomcarter.frameworks.config.utils.ReflectionUtils;
import com.gomcarter.frameworks.mybatis.annotation.Condition;
import com.gomcarter.frameworks.mybatis.annotation.MatchStrategy;
import com.gomcarter.frameworks.mybatis.annotation.MatchType;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

/**
 * @author gomcarter
 */
public class MapperUtils {

    /**
     * 构建分页器
     *
     * @param pageable 外部传来的参数
     * @param <T>      要查询的对象
     * @return page
     */
    public static <T> Page<T> buildPage(Pageable pageable) {
        String split = ",";
        // 构造分页器
        Page<T> page = new Page<>(pageable.getStartNum() / pageable.getPageCount() + 1, pageable.getPageCount(), false);
        String orderColumn = pageable.getOrderColumn();
        if (orderColumn != null && (orderColumn = orderColumn.trim()).length() > 0) {
            String[] sortby = orderColumn.split(split);
            String[] orderTypes = (pageable.getOrderType() == null ? "" : pageable.getOrderType())
                    .trim().split(split);

            List<OrderItem> orderItemList = new ArrayList<>();
            for (int i = 0; i < sortby.length; ++i) {
                String sort = sortby[i];
                boolean asc = orderTypes.length >= i + 1 && "asc".equalsIgnoreCase(orderTypes[i]);

                OrderItem orderItem = new OrderItem();
                orderItem.setColumn(sort);
                orderItem.setAsc(asc);

                orderItemList.add(orderItem);
            }

            page.setOrders(orderItemList);
        }

        return page;
    }

    private static WeakHashMap<Class, List<Field>> FIELD_CACHE_MAP = new WeakHashMap<>();

    public static <T, R> Wrapper<T> buildQueryWrapper(R params, String... columns) {
        boolean hasColumn = columns != null && columns.length > 0;
        boolean hasCondition = params != null;
        QueryWrapper<T> wrapper = null;
        if (hasColumn || hasCondition) {
            wrapper = new QueryWrapper<>();

            if (hasColumn) {
                wrapper.select(columns);
            }

            if (hasCondition) {
                return MapperUtils.buildWrapper(wrapper, params);
            }
        }

        return wrapper;
    }

    public static <T, R> Wrapper<T> buildUpdateWrapper(R params, String... columnsToNull) {
        boolean hasColumn = columnsToNull != null && columnsToNull.length > 0;
        boolean hasCondition = params != null;
        UpdateWrapper<T> wrapper = null;
        if (hasColumn || hasCondition) {
            wrapper = new UpdateWrapper<>();

            if (hasColumn) {
                for (String s : columnsToNull) {
                    wrapper.set(true, s, null);
                }
            }

            if (hasCondition) {
                return MapperUtils.buildWrapper(wrapper, params);
            }
        }

        return wrapper;
    }

    /**
     * 根据params 构建一个 wrapper，具体规则见：{@link com.gomcarter.frameworks.mybatis.annotation.Condition}
     *
     * @param wrapper queryWrapper or updateWrapper
     * @param params  参数
     * @param <T>     查询实体类型
     * @param <R>     参数类型
     * @return wrapper
     */
    public static <T, R> Wrapper<T> buildWrapper(Wrapper<T> wrapper, R params) {
        // cache
        List<Field> fields = FIELD_CACHE_MAP.get(params.getClass());
        if (fields == null) {
            fields = ReflectionUtils.findAllField(params.getClass());
            FIELD_CACHE_MAP.put(params.getClass(), fields);
        }

        // fields never be null
        for (Field field : fields) {
            Object value = null;
            Condition condition = null;
            MatchStrategy strategy = null;
            if (field.isAnnotationPresent(Condition.class)) {
                condition = field.getAnnotation(Condition.class);
                if (StringUtils.isNotBlank(condition.fixedValue())) {
                    // 获取默固定认值
                    Convertable converter = Convertable.getConverter(field.getType());
                    value = converter.convert(condition.fixedValue(), ObjectUtils.defaultIfNull(field.getGenericType(), field.getType()));
                }

                strategy = condition.strategy();
            }

            if (value == null) {
                value = ReflectionUtils.getFieldValue(params, field);
            }

            String fieldName = getFieldName(condition, field);
            // 如果为 null，则跳过
            if (value == null) {
                if (strategy == MatchStrategy.IGNORED) {
                    MatchType.NULL.wrap(wrapper, fieldName, null);
                }
            } else {
                // 根据字段类型获取默认的匹配类型
                MatchType type = MatchType.getDefaultType(condition, field.getType());
                // 开始包装
                type.wrap(wrapper, fieldName, value);
            }
        }

        return wrapper;
    }

    public static String getFieldName(Condition condition, Field field) {
        String fieldName = null;
        if (condition != null) {
            fieldName = condition.field();
        }

        if (fieldName == null || fieldName.length() == 0) {
            fieldName = CustomStringUtils.camelToUnderline(field.getName());
        }

        return fieldName;
    }

    public static String getDatabaseFieldName(Condition condition, String paramName) {
        String fieldName = null;
        if (condition != null) {
            fieldName = condition.field();
        }

        if (fieldName == null || fieldName.length() == 0) {
            fieldName = CustomStringUtils.camelToUnderline(paramName);
        }

        return "`" + fieldName + "`";
    }

    public static void buildWhereSql(StringBuilder whereSql, Condition condition, String paramName, Class paramsClass) {
        // 简单类型
        if (BeanUtils.isSimpleValueType(paramsClass)) {
            MatchType type = MatchType.getDefaultType(condition, paramsClass);
            String databaseFieldName = MapperUtils.getDatabaseFieldName(condition, paramName);

            type.sqlTemplate(whereSql, condition, databaseFieldName, paramName, paramsClass);
        } else {
            // 复杂类型取里面的字段
            List<Field> fields = ReflectionUtils.findAllField(paramsClass);
            // fields never be null
            for (Field field : fields) {
                Class fieldClass = field.getType();

                // 根据字段类型获取默认的匹配类型
                Condition subCondition = field.getAnnotation(Condition.class);
                MatchType type = MatchType.getDefaultType(subCondition, fieldClass);
                String databaseFieldName = getDatabaseFieldName(subCondition, field.getName());

                // 开始包装
                type.sqlTemplate(whereSql, subCondition, databaseFieldName, paramName + "." + field.getName(), fieldClass);
                whereSql.append(" \n");
            }
        }
    }

    /**
     * Backport of java.lang.reflect.Method#isDefault()
     *
     * @param method method
     * @return true -- default methods
     */
    public static boolean isDefaultMethod(Method method) {
        return (method.getModifiers()
                & (Modifier.ABSTRACT | Modifier.PUBLIC | Modifier.STATIC)) == Modifier.PUBLIC
                && method.getDeclaringClass().isInterface();
    }

    public static void buildExistsSql(StringBuilder whereSql, Object params) {
        // cache
        List<Field> fields = FIELD_CACHE_MAP.get(params.getClass());
        if (fields == null) {
            fields = ReflectionUtils.findAllField(params.getClass());
            FIELD_CACHE_MAP.put(params.getClass(), fields);
        }

        // fields never be null
        for (Field field : fields) {
            Object value = null;
            Condition condition = null;
            boolean fixed = false;
            if (field.isAnnotationPresent(Condition.class)) {
                condition = field.getAnnotation(Condition.class);
                if (fixed = StringUtils.isNotBlank(condition.fixedValue())) {
                    // 获取默固定认值
                    Convertable converter = Convertable.getConverter(field.getType());
                    value = converter.convert(condition.fixedValue(), ObjectUtils.defaultIfNull(field.getGenericType(), field.getType()));
                }
            }

            if (value == null) {
                value = ReflectionUtils.getFieldValue(params, field);
            }

            if (value != null) {
                // 根据字段类型获取默认的匹配类型
                MatchType type = MatchType.getDefaultType(condition, field.getType());
                // 开始包装
                type.whereSqlForExists(whereSql, "a." + getDatabaseFieldName(condition, field.getName()), value, fixed);
            }
        }
    }


    public static void main(String[] args) {
        StringBuilder whereSql = new StringBuilder();

        String sql = "select 1 from demo a ";
        MapperUtils.buildExistsSql(whereSql, new Demo());
        if (whereSql.length() > 0) {
            sql += " where 1 = 1 " + whereSql.toString();
        }

        System.out.println(sql);
    }
}
