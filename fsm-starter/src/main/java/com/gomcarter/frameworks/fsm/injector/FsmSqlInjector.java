package com.gomcarter.frameworks.fsm.injector;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.gomcarter.frameworks.fsm.injector.method.FsmUpdateStateUpdater;
import com.gomcarter.frameworks.mybatis.injector.CustomSqlInjector;

import java.util.List;

/**
 * fsm sqlTemplate 定制注入器
 *
 * @author 李银 2020年03月17日17:17:33
 */
public class FsmSqlInjector extends CustomSqlInjector {

    @Override
    public List<AbstractMethod> getMethodList(Class<?> mapperClass) {
        List<AbstractMethod> methods = super.getMethodList(mapperClass);

        methods.add(new FsmUpdateStateUpdater());

        return methods;
    }
}
