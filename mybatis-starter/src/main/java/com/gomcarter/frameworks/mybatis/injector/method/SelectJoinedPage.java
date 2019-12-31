package com.gomcarter.frameworks.mybatis.injector.method;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.gomcarter.frameworks.base.common.AssertUtils;
import com.gomcarter.frameworks.base.common.CustomStringUtils;
import com.gomcarter.frameworks.base.common.ReflectionUtils;
import com.gomcarter.frameworks.base.pager.Pageable;
import com.gomcarter.frameworks.mybatis.annotation.Condition;
import com.gomcarter.frameworks.mybatis.annotation.Joinable;
import com.gomcarter.frameworks.mybatis.utils.MapperUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Iterator;


/**
 * 查询满足条件所有数据（并翻页, 并支持多表 join）
 * <p>
 * 前提条件：
 * 1，返回类型必须是Collection；
 * 2，Collection模板中的类标注了Joinable，表示此方法是需要 join
 *
 * @author gomcarter
 * @since 2019-12-26 09:11:22
 */
public class SelectJoinedPage extends AbstractMethod {

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo ignore) {
        // sql 模板
        String sqlTemplate = "<script>\nSELECT %s FROM %s %s %s\n</script>";

        // 条件：
        // 1，方法标注了Joinable
        // 2，返回类型必须是Collection；
        Method[] methods = mapperClass.getDeclaredMethods();
        for (Method method : methods) {
            // 未标注 @Joinable  或 default 的方法跳过
            // 返回类型必须是Collection
            if (!method.isAnnotationPresent(Joinable.class)
                    || MapperUtils.isDefaultMethod(method)
                    || !Collection.class.isAssignableFrom(method.getReturnType())) {
                continue;
            }

            Joinable[] joinables = method.getAnnotationsByType(Joinable.class);

            String mainTable = joinables[0].main();
            AssertUtils.isTrue(StringUtils.isNotBlank(mainTable), "主表不能空");

            String table = MapperUtils.generateTable(mainTable, joinables);

            String pageSql = "";
            StringBuilder whereSql = new StringBuilder();
            Parameter[] parameters = method.getParameters();
            for (Parameter parameter : parameters) {
                String paramName = parameter.getName();
                if (parameter.isAnnotationPresent(Param.class)) {
                    paramName = StringUtils.defaultIfBlank(parameter.getAnnotation(Param.class).value(), paramName);
                }

                if (Pageable.class.isAssignableFrom(parameter.getType())) {
                    pageSql = "order by ${" + paramName + ".orderColumn} ${" + paramName +
                            ".orderType} LIMIT #{" + paramName + ".startNum},#{" + paramName + ".pageCount}";
                } else {
                    if (whereSql.length() == 0) {
                        whereSql.append("\n<where> 1 = 1\n");
                    }

                    MapperUtils.buildWhereSql(whereSql, parameter.getAnnotation(Condition.class), mainTable, paramName, parameter.getType());
                }
            }

            if (whereSql.length() > 0) {
                whereSql.append("</where>\n");
            }

            // 获取返回值，以及对应的 sql
            Class returnType = (Class) ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
            TableInfo tableInfo = ignore;
            if (returnType != modelClass) {
                tableInfo = TableInfoHelper.initTableInfo(builderAssistant, returnType);
            }

            String selectColumns = generateSelectColumns(mainTable, returnType);
            String sql = String.format(sqlTemplate, selectColumns, table, whereSql.toString(), pageSql);

            SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
            this.addSelectMappedStatementForTable(mapperClass, method.getName(), sqlSource, tableInfo);
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
