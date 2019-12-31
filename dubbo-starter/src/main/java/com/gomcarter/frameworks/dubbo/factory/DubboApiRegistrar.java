package com.gomcarter.frameworks.dubbo.factory;

import com.gomcarter.frameworks.base.common.AopUtils;
import com.gomcarter.frameworks.base.common.ReflectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.*;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author gomcarter on 2019-11-09 23:31:48
 */
@Order
@Slf4j
public class DubboApiRegistrar implements BeanPostProcessor {

    private RegistryConfig rc;
    private ApplicationConfig ac;
    private ProtocolConfig pc;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // register service
        this.registerService(bean);

        // registry reference
        this.registerReference(bean);

        return bean;
    }

    private void registerService(Object bean) {
        Service service = bean.getClass().getAnnotation(Service.class);
        // 如果打了service标签，则注册到注册中心去
        if (service != null) {
            // 手动指定了 service
            Class<?> interfacesClass = service.interfaceClass();

            // 没有手动指定，则自动获取
            if (interfacesClass == void.class) {
                // 去除代理类，获取原始类的实现接口
                Class userClass = AopUtils.getTargetClass(bean.getClass());
                Class[] superInterfaces = userClass.getInterfaces();
                if (superInterfaces == null || superInterfaces.length > 1) {
                    throw new RuntimeException(userClass.getName() + "注册到 dubbo 的接口不明确！");
                }

                interfacesClass = superInterfaces[0];
            }


            ServiceConfig<Object> sc = new ServiceConfig<>();
            sc.setApplication(ac);
            sc.setRegistry(rc);
            sc.setProtocol(pc);
            sc.setInterface(interfacesClass);
            sc.setRef(bean);
            sc.setTimeout(ObjectUtils.defaultIfNull(service.timeout(), 2000));

            sc.setLoadbalance(service.loadbalance());
            sc.setCluster(service.cluster());
            sc.setGroup(service.group());
            sc.setVersion(service.version());

            if (service.export()) {
                sc.export();
            }

            log.info("dubbo service {} - {} started ", interfacesClass.getName(), bean.getClass().getName());
        }
    }

    private Map<Class, Object> ioc = new HashMap<>();

    private void registerReference(Object bean) {
        // 获取被代理的对象
        Object target = AopUtils.getTargetObject(bean);
        for (Field field : ReflectionUtils.findAllField(target.getClass())) {
            Reference reference = field.getAnnotation(Reference.class);
            if (reference == null) {
                continue;
            }

            Class<?> apiClass = field.getType();
            Object api = ioc.get(apiClass);
            if (api == null) {
                ReferenceConfig<?> referenceConfig = new ReferenceConfig<>();

                referenceConfig.setApplication(ac);
                referenceConfig.setRegistry(rc);
                if (rc.isCheck() == null) {
                    referenceConfig.setCheck(false);
                } else {
                    referenceConfig.setCheck(rc.isCheck() && reference.check());
                }
                referenceConfig.setFilter(StringUtils.join(reference.filter(), ","));
                referenceConfig.setVersion(reference.version());
                referenceConfig.setGroup(reference.group());
                referenceConfig.setCluster(reference.cluster());
                referenceConfig.setLoadbalance(reference.loadbalance());
                referenceConfig.setRetries(reference.retries());
                referenceConfig.setInterface(field.getType());
                referenceConfig.setUrl(reference.url());
                referenceConfig.setClient(reference.client());
                referenceConfig.setActives(reference.actives());
                referenceConfig.setAsync(reference.async());
                referenceConfig.setCache(reference.cache());
                referenceConfig.setCallbacks(reference.callbacks());
                referenceConfig.setConnections(reference.connections());
                referenceConfig.setInit(reference.init());
                referenceConfig.setLayer(reference.layer());
                referenceConfig.setId(reference.id());
                referenceConfig.setProxy(reference.proxy());
                referenceConfig.setTimeout(ObjectUtils.defaultIfNull(reference.timeout(), 2000));
                referenceConfig.setLazy(reference.lazy());
                referenceConfig.setListener(StringUtils.join(reference.listener(), ","));
                referenceConfig.setMock(reference.mock());
                referenceConfig.setMonitor(reference.monitor());

                api = referenceConfig.get();
                ioc.put(apiClass, api);
            }

            ReflectionUtils.setField(target, field, api);
        }
    }

    public RegistryConfig getRc() {
        return rc;
    }

    public DubboApiRegistrar setRc(RegistryConfig rc) {
        this.rc = rc;
        return this;
    }

    public ApplicationConfig getAc() {
        return ac;
    }

    public DubboApiRegistrar setAc(ApplicationConfig ac) {
        this.ac = ac;
        return this;
    }

    public ProtocolConfig getPc() {
        return pc;
    }

    public DubboApiRegistrar setPc(ProtocolConfig pc) {
        this.pc = pc;
        return this;
    }
}
