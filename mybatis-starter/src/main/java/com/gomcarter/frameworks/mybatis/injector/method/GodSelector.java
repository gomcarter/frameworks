package com.gomcarter.frameworks.mybatis.injector.method;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.gomcarter.frameworks.base.common.CustomStringUtils;
import com.gomcarter.frameworks.base.pager.Pageable;
import com.gomcarter.frameworks.config.utils.ReflectionUtils;
import com.gomcarter.frameworks.mybatis.annotation.Condition;
import com.gomcarter.frameworks.mybatis.utils.MapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Iterator;

/**
 * 1、万能查询 sqlTemplate 自动生成器
 * 2、支持分页查询
 * 3、计算总数（返回值设置为Integer,Long类型即可）
 *
 * <p>
 * 寻找没有注入 sqlTemplate 的接口，自动给填充 select 语句和查询条件
 *
 * @author gomcarter
 * @since 2019-12-26 09:11:22
 */
@Slf4j
public class GodSelector extends AbstractMethod {

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        // sqlTemplate 模板
        String sqlTemplate = "<script>\nSELECT %s FROM %s %s %s\n</script>";

        MybatisConfiguration configuration = tableInfo.getConfiguration();
        Method[] methods = mapperClass.getDeclaredMethods();
        // 1，扫描 mapperClass 中存在支持自动加 sqlTemplate 的方法。
        for (Method method : methods) {
            //  如果此方法已经有实现了或者 default 方法，跳过
            String id = mapperClass.getName() + "." + method.getName();
            if (configuration.hasStatement(id)
                    || MapperUtils.isDefaultMethod(method)) {
                continue;
            }

            Class returnType = method.getReturnType();
            String pageSql = "";
            boolean count = false;
            // 简单类型返回值仅支持Integer或Long
            if (BeanUtils.isSimpleValueType(returnType)) {
                if (Long.class != returnType && Integer.class != returnType) {
                    log.error("自动注入{}的sql失败, 简单类型返回值仅支持Integer或Long", id);
                    throw new RuntimeException("初始化" + id + "失败");
                }
                // 单类返回一条数据
                count = true;
            }

            String table, selectColumns;

            table = tableInfo.getTableName();
            selectColumns = sqlSelectColumns(tableInfo, false);

            StringBuilder whereSql = new StringBuilder();
            Parameter[] parameters = method.getParameters();
            for (Parameter parameter : parameters) {
                String paramName = parameter.getName();
                if (parameter.isAnnotationPresent(Param.class)) {
                    paramName = StringUtils.defaultIfBlank(parameter.getAnnotation(Param.class).value(), paramName);
                }

                if (Pageable.class.isAssignableFrom(parameter.getType())) {
                    if (!count) {
                        // count 表示只是计算条数，就没有必要分页了
                        pageSql = "order by ${" + paramName + ".orderColumn} ${" + paramName +
                                ".orderType} LIMIT #{" + paramName + ".startNum},#{" + paramName + ".pageCount}";
                    }
                } else {
                    if (whereSql.length() == 0) {
                        whereSql.append("\n<where> 1 = 1\n");
                    }

                    MapperUtils.buildWhereSql(whereSql, parameter.getAnnotation(Condition.class), paramName, parameter.getType());
                }
            }

            if (whereSql.length() > 0) {
                whereSql.append("</where>\n");
            }

            String sql = String.format(sqlTemplate, selectColumns, table, whereSql.toString(), pageSql);
            SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);

            if (count) {
                this.addSelectMappedStatementForOther(mapperClass, id, sqlSource, returnType);
            } else {
                this.addSelectMappedStatementForTable(mapperClass, id, sqlSource, tableInfo);
            }
        }

        // 从源码看，这里的返回值是没有意义的。
        return null;
    }

    private String generateSelectColumns(String mainTable, Class returnType) {
        // cache
        Iterator<Field> fields = ReflectionUtils.findAllField(returnType).iterator();

        StringBuilder sb = new StringBuilder();
        if (!fields.hasNext()) {
            return "1";
        }

        while (true) {
            Field field = fields.next();
            String fieldName = null;
            if (field.isAnnotationPresent(TableField.class)) {
                TableField tableField = field.getAnnotation(TableField.class);
                if (tableField.exist()) {
                    fieldName = tableField.value();
                }
            }

            if (fieldName == null || fieldName.length() == 0) {
                fieldName = mainTable + "." + CustomStringUtils.camelToUnderline(field.getName());
            }

            fieldName = fieldName + " AS `" + field.getName() + '`';

            sb.append(fieldName);

            if (!fields.hasNext()) {
                break;
            }

            sb.append(",");
        }
        return sb.toString();
    }
}
