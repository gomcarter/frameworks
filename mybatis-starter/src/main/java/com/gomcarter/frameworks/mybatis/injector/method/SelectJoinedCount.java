package com.gomcarter.frameworks.mybatis.injector.method;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.gomcarter.frameworks.base.common.AssertUtils;
import com.gomcarter.frameworks.base.common.CustomStringUtils;
import com.gomcarter.frameworks.mybatis.annotation.Condition;
import com.gomcarter.frameworks.mybatis.annotation.Joinable;
import com.gomcarter.frameworks.mybatis.annotation.MatchType;
import com.gomcarter.frameworks.mybatis.utils.MapperUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * 查询满足条件所有数据（并翻页, 并支持多表 join）
 * 1，共用此方法，如何动态 join？（存在多个 join 的时候如何隔离？）
 * 2，如何不与源 Entity 混合在一起？
 * <p>
 * 使用注解形式，自定义实体的 entity
 *
 * @author gomcarter
 * @since 2019-12-26 09:11:22
 */
public class SelectJoinedCount extends AbstractMethod {

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo ignore) {
        // sql 模板
        String sqlTemplate = "<script>\nSELECT count(1) FROM %s %s\n</script>";

        // 1，扫描 mapperClass 中存在支持 join 的方法。
        Method[] methods = mapperClass.getDeclaredMethods();
        for (Method method : methods) {
            // 未标注 @Joinable  或 default 的方法跳过
            // 返回结果类型必须是 Integer
            if (!method.isAnnotationPresent(Joinable.class)
                    || MapperUtils.isDefaultMethod(method)
                    || method.getReturnType() != Integer.class) {
                continue;
            }

            Joinable[] joinables = method.getAnnotationsByType(Joinable.class);
            String mainTable = joinables[0].main();
            AssertUtils.isTrue(StringUtils.isNotBlank(mainTable), "主表不能空");

            String table = MapperUtils.generateTable(mainTable, joinables);

            StringBuilder whereSql = new StringBuilder();
            Parameter[] parameters = method.getParameters();
            for (Parameter parameter : parameters) {
                String paramName = parameter.getName();
                if (parameter.isAnnotationPresent(Param.class)) {
                    paramName = StringUtils.defaultIfBlank(parameter.getAnnotation(Param.class).value(), paramName);
                }

                if (whereSql.length() == 0) {
                    whereSql.append("\n<where> 1 = 1\n");
                }

                MapperUtils.buildParamSql(whereSql, parameter, mainTable, paramName);
            }

            if (whereSql.length() > 0) {
                whereSql.append("</where>\n");
            }

            String sql = String.format(sqlTemplate, table, whereSql.toString());

            SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
            this.addSelectMappedStatementForOther(mapperClass, method.getName(), sqlSource, Integer.class);
        }

        // 从源码看，这里的返回值是没有意义的。
        return null;
    }
}
