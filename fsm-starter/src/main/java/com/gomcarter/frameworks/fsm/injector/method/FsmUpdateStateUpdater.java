package com.gomcarter.frameworks.fsm.injector.method;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

import java.lang.reflect.Method;

/**
 * @author 李银
 * @since 2020年09月25日13:30:49
 */
@Slf4j
public class FsmUpdateStateUpdater extends AbstractMethod {

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo ignore) {
        // sql 模板
        String sqlTemplate = "<script>\nUPDATE %s SET state = #{state} where id = #{id}\n</script>";


        MybatisConfiguration configuration = ignore.getConfiguration();
        Method[] methods = mapperClass.getMethods();
        // 1，扫描 mapperClass 中存在支持自动加 sql 的方法。
        for (Method method : methods) {
            if ("$fsmUpdateState".equals(method.getName())) {
                String sql = String.format(sqlTemplate, ignore.getTableName());
                SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);

                return this.addUpdateMappedStatement(mapperClass, modelClass, method.getName(), sqlSource);
            }
        }

        // 从源码看，这里的返回值是没有意义的。
        return null;
    }
}
