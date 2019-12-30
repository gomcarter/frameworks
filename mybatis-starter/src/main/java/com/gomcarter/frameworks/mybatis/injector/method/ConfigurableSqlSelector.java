package com.gomcarter.frameworks.mybatis.injector.method;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.gomcarter.frameworks.base.config.UnifiedConfigService;
import com.gomcarter.frameworks.mybatis.annotation.ConfigurableSql;
import com.gomcarter.frameworks.mybatis.utils.MapperUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;

/**
 * 从配置中心拿 sql， 为了安全起见，目前仅支持查询；
 *
 * @author gomcarter
 * @since 2019-12-30 17:35:16
 */
public class ConfigurableSqlSelector extends AbstractMethod {

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo ignore) {
        // 1，扫描 mapperClass 中存在支持 ConfigurableSql 的方法。
        MybatisConfiguration configuration = ignore.getConfiguration();
        Method[] methods = mapperClass.getDeclaredMethods();
        for (Method method : methods) {
            //  如果此方法已经有实现了或者 default 方法，跳过
            if (configuration.hasStatement(method.getName())
                    || MapperUtils.isDefaultMethod(method)
                    || !method.isAnnotationPresent(ConfigurableSql.class)) {
                continue;
            }
            ConfigurableSql configurableSql = method.getAnnotation(ConfigurableSql.class);

            String[] keys = configurableSql.value();
            String sql = UnifiedConfigService.getInstance().getConfig(keys);

            if (StringUtils.isBlank(sql)) {
                continue;
            }

            Class returnType = method.getReturnType();
            if (Collection.class.isAssignableFrom(method.getReturnType())) {
                returnType = (Class) ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
            }

            TableInfo tableInfo = TableInfoHelper.initTableInfo(builderAssistant, returnType);
            SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);

            this.addSelectMappedStatementForTable(mapperClass, method.getName(), sqlSource, tableInfo);
        }

        // 从源码看，这里的返回值是没有意义的。
        return null;
    }
}
