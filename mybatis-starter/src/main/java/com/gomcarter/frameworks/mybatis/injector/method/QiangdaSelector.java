package com.gomcarter.frameworks.mybatis.injector.method;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.gomcarter.frameworks.base.pager.Pageable;
import com.gomcarter.frameworks.mybatis.utils.MapperUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;

/**
 * 寻找没有注入 sql 的接口，自动给填充 select 语句和查询条件
 *
 * @author gomcarter
 * @since 2019-12-26 09:11:22
 */
public class QiangdaSelector extends AbstractMethod {

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo ignore) {
        // sql 模板
        String sqlTemplate = "<script>\nSELECT %s FROM %s %s %s\n</script>";

        // 1，扫描 mapperClass 中存在支持 join 的方法。
        Method[] methods = mapperClass.getDeclaredMethods();

        MybatisConfiguration configuration = ignore.getConfiguration();
        for (Method method : methods) {
            //  如果此方法已经有实现了或者 default 方法，跳过
            if (configuration.hasStatement(method.getName())
                    || MapperUtils.isDefaultMethod(method)) {
                continue;
            }

            Class returnType = method.getReturnType();
            // 自动注入需满足两个条件中的一个
            // 1，返回值必须是 java.util.Collection
            // 2，否则如果必须是一个 java 类（必须包含属性，不能是简单类型和包装类）
            if (!Collection.class.isAssignableFrom(method.getReturnType())) {
                // 不是Collection
                if (BeanUtils.isSimpleValueType(returnType) || returnType.getDeclaredFields().length == 0
                        || returnType == void.class) {
                    continue;
                }
            } else {
                returnType = (Class) ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
            }

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

                    MapperUtils.buildParamSql(whereSql, parameter, null, paramName);
                }
            }

            if (whereSql.length() > 0) {
                whereSql.append("</where>\n");
            }

            TableInfo tableInfo = TableInfoHelper.initTableInfo(builderAssistant, returnType);
            String selectColumns = sqlSelectColumns(tableInfo, false);

            String sql = String.format(sqlTemplate, selectColumns,
                    ignore.getTableName(), whereSql.toString(), pageSql);
            SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
            this.addSelectMappedStatementForTable(mapperClass, method.getName(), sqlSource, tableInfo);
        }

        // 从源码看，这里的返回值是没有意义的。
        return null;
    }
}
