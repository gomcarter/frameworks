package com.gomcarter.frameworks.mybatis.utils;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gomcarter.frameworks.base.common.AssertUtils;
import com.gomcarter.frameworks.base.common.CustomStringUtils;
import com.gomcarter.frameworks.base.common.ReflectionUtils;
import com.gomcarter.frameworks.base.pager.Pageable;
import com.gomcarter.frameworks.mybatis.annotation.Condition;
import com.gomcarter.frameworks.mybatis.annotation.JoinType;
import com.gomcarter.frameworks.mybatis.annotation.Joinable;
import com.gomcarter.frameworks.mybatis.annotation.MatchType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
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
     * @param params 参数
     * @param <T>    查询实体类型
     * @param <R>    参数类型
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
            Object value = ReflectionUtils.getFieldValue(params, field);
            // 如果为 null，则跳过此字段
            if (value == null) {
                continue;
            }

            // 根据字段类型获取默认的匹配类型
            Condition condition = field.getAnnotation(Condition.class);
            MatchType type = MatchType.getDefaultType(condition, field.getType());
            String fieldName = getFieldName(condition, field);

            // 开始包装
            type.wrap(wrapper, fieldName, value);
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

    public static String getDatabaseFieldName(Condition condition, String mainTable, String paramName) {
        String alias = StringUtils.isBlank(mainTable) ? "" : (mainTable + ".");

        String databaseFieldName = null;
        if (condition != null) {
            databaseFieldName = condition.field();
            // 不带. 证明不是副表的字段，是主表的字段
            if (databaseFieldName.length() > 0 && !databaseFieldName.contains(".")) {
                databaseFieldName = alias + "`" + databaseFieldName + "`";
            }
        }

        if (databaseFieldName == null || databaseFieldName.length() == 0) {
            databaseFieldName = alias + "`" + CustomStringUtils.camelToUnderline(paramName) + "`";
        }
        return databaseFieldName;
    }

    public static void buildSql(StringBuilder sql, String mainTable, String paramName, Class paramsClass) {

        // 复杂类型取里面的字段
        List<Field> fields = ReflectionUtils.findAllField(paramsClass);
        // fields never be null
        for (Field field : fields) {
            Class fieldClass = field.getType();

            // 根据字段类型获取默认的匹配类型
            Condition condition = field.getAnnotation(Condition.class);
            MatchType type = MatchType.getDefaultType(condition, fieldClass);
            String databaseFieldName = getDatabaseFieldName(condition, mainTable, field.getName());

            // 开始包装
            type.sql(sql, mainTable, databaseFieldName, paramName + "." + field.getName(), fieldClass);
            sql.append(" \n");
        }
    }


    /**
     * 拼接 table join
     *
     * @param mainTable
     * @param joinables joinables
     * @return generateTable string
     */
    public static String generateTable(String mainTable, Joinable[] joinables) {
        StringBuilder sql = new StringBuilder(String.format(" %s %s ", mainTable, mainTable));
        for (Joinable joinable : joinables) {
            String main = joinable.main(),
                    target = joinable.target(),
                    mainKey = joinable.mainKey(),
                    targetKey = joinable.targetKey();
            JoinType type = joinable.type();
            AssertUtils.isTrue(StringUtils.isNotBlank(main), "主表不能空");
            AssertUtils.isTrue(StringUtils.isNotBlank(target), "副表不能空");
            AssertUtils.isTrue(StringUtils.isNotBlank(mainKey), "主表 join 键不能空");
            if (StringUtils.isBlank(targetKey)) {
                targetKey = main + "_id";
            }

            sql.append(String.format("%s JOIN %s %s ON %s.%s = %s.%s ",
                    type.name(), target, target, main, mainKey, target, targetKey));

        }
        return sql.toString();
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

    public static void buildParamSql(StringBuilder whereSql, Parameter parameter, String mainTable, String javaParamName) {
        // 简单类型， 直接 EQ
        if (BeanUtils.isSimpleValueType(parameter.getType())) {
            Condition condition = parameter.getAnnotation(Condition.class);
            MatchType type = MatchType.getDefaultType(condition, parameter.getType());

            type.sql(whereSql, mainTable, MapperUtils.getDatabaseFieldName(condition, mainTable, javaParamName), javaParamName, parameter.getType());
        } else {
            MapperUtils.buildSql(whereSql, mainTable, javaParamName, parameter.getType());
        }
    }
}
