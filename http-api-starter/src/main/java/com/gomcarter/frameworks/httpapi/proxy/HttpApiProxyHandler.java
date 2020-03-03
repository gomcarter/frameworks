package com.gomcarter.frameworks.httpapi.proxy;

import com.gomcarter.frameworks.base.config.UnifiedConfigService;
import com.gomcarter.frameworks.base.converter.Convertable;
import com.gomcarter.frameworks.base.json.JsonData;
import com.gomcarter.frameworks.base.mapper.JsonMapper;
import com.gomcarter.frameworks.httpapi.annotation.CheckToken;
import com.gomcarter.frameworks.httpapi.annotation.HttpMethod;
import com.gomcarter.frameworks.httpapi.annotation.HttpParam;
import com.gomcarter.frameworks.httpapi.annotation.ParamType;
import com.gomcarter.frameworks.httpapi.api.BaseApi;
import com.gomcarter.frameworks.httpapi.demo.DemoDto;
import com.gomcarter.frameworks.httpapi.demo.HttpDemoApi;
import com.gomcarter.frameworks.httpapi.utils.ApiTokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * api代理类，执行 http 请求
 *
 * @author gomcarter
 */
@Slf4j
public class HttpApiProxyHandler implements InvocationHandler {

    public static class Holder {
        static BaseApi baseApi;

        static {
            baseApi = new BaseApi() {
                @Override
                protected Map<String, String> getUrlRouter() {
                    return new HashMap<>(16);
                }
            };

            baseApi.afterPropertiesSet();
        }
    }

    public static void addRouter(Properties properties) {
        // 将配置设置到 baseApi 的 router 中。
        for (Object key : properties.keySet()) {
            Holder.baseApi.appendUrlRequestRouter((String) key, properties.getProperty((String) key));
        }
    }

    public <T> T getProxy(Class<T> interfaceClass) {
        return (T) (Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass}, this));
    }

    /**
     * 执行代理对象的所有方法时都会被替换成执行如下的invoke方法
     * <p>
     * target ：代表动态代理对象； method：代表正在执行的方法 ；args :代表执行代理对象方法时传入的实参
     */
    @Override
    public Object invoke(Object target, Method method, Object[] args) throws Throwable {
        // object 自身方法， default 方法不代理
        if (Object.class.equals(method.getDeclaringClass())) {
            return method.invoke(this, args);
        } else if (isDefaultMethod(method)) {
            return invokeDefaultMethod(target, method, args);
        }

        HttpMethod http = method.getAnnotation(HttpMethod.class);
        if (http == null) {
            log.error("未配置 @HttpMethod 信息");
            return null;
        }

        ParamType.Param httpParams = new ParamType.Param();
        Parameter[] parameters = method.getParameters();

        for (int i = 0; i < parameters.length; ++i) {
            Parameter parameter = parameters[i];
            HttpParam param = parameter.getAnnotation(HttpParam.class);
            if (param == null) {
                throw new RuntimeException("the [" + (i + 1) + "] parameter need @HttpParam annotation");
            }
            // 获取默认值
            Object defaultValue = defaultValue(parameter, args[i], param);

            // 加入到 params 中
            param.type().defaultMap(httpParams, parameter, param, defaultValue);
        }

        // 加上认证header
        CheckToken checkToken = method.getAnnotation(CheckToken.class);
        if (checkToken != null) {
            Map<String, String> header = httpParams.getHeader();
            if (header == null) {
                header = new HashMap<>(1, 1);
            }
            header.put(checkToken.tokenName(), new ApiTokenUtils(checkToken.key(), checkToken.tokenName()).getToken());
            httpParams.setHeader(header);
        }

        String stringResult = Holder.baseApi.httpExecute(http.method(), http.key(),
                httpParams.getParams(),
                httpParams.getRestParams(),
                httpParams.getBody() == null ? null : httpParams.getBody().toString(),
                httpParams.getHeader(),
                httpParams.getInputStreamMap());

        Class<?> resultType = method.getReturnType();
        if (resultType == String.class || resultType == void.class) {
            return stringResult;
        }

        if (http.wrap()) {
            // 被 jsonData 包裹的返回值
            JsonData result = JsonMapper.buildNonNullMapper().fromJson(stringResult, JsonData.class);

            if (!result.isSuccess()) {
                log.error("调用接口失败: {}, {}", result.getCode(), result.getMessage());
                throw new RuntimeException("调用接口失败");
            }

            stringResult = JsonMapper.buildNonNullMapper().toJson(result.getExtra());
        }

        Convertable converter = Convertable.getConverter(resultType);
        return converter.convert(stringResult, method.getGenericReturnType());
    }

    /**
     * Backport of java.lang.reflect.Method#isDefault()
     *
     * @param method method
     * @return true -- default methods
     */
    private boolean isDefaultMethod(Method method) {
        return (method.getModifiers()
                & (Modifier.ABSTRACT | Modifier.PUBLIC | Modifier.STATIC)) == Modifier.PUBLIC
                && method.getDeclaringClass().isInterface();
    }

    private Object invokeDefaultMethod(Object proxy, Method method, Object[] args)
            throws Throwable {
        final Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class
                .getDeclaredConstructor(Class.class, int.class);
        if (!constructor.isAccessible()) {
            constructor.setAccessible(true);
        }
        final Class<?> declaringClass = method.getDeclaringClass();
        return constructor
                .newInstance(declaringClass,
                        MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED
                                | MethodHandles.Lookup.PACKAGE | MethodHandles.Lookup.PUBLIC)
                .unreflectSpecial(method, declaringClass).bindTo(proxy).invokeWithArguments(args);
    }

    private Object defaultValue(Parameter parameter, Object value, HttpParam param) {
        if (value == null) {
            Convertable converter = Convertable.getConverter(parameter.getType());
            try {
                return converter.convert(param.defaultValue(), ObjectUtils.defaultIfNull(parameter.getParameterizedType(), parameter.getType()));
            } catch (Exception e) {
                return null;
            }
        }
        return value;
    }

    public static void main(String[] args) {
        // 从Nacos中读取配置
        Properties properties = UnifiedConfigService.getInstance().getConfigAsProperties("MEMBER", "API");
        HttpApiProxyHandler.addRouter(properties);

        HttpDemoApi demoApi = new HttpApiProxyHandler().getProxy(HttpDemoApi.class);
//        System.out.println(demoApi.toString());

        long start = System.currentTimeMillis();

        for (int i = 0; i < 10; ++i) {
            Object demoDto = demoApi.post(new DemoDto().setId(1L).setNickname("11"), 2L,
                    null, new HashMap<String, String>() {{
                        put("header1", "header");
                    }},
                    Arrays.asList("1", "2", "3")
            );

            System.out.println(JsonMapper.buildNonNullMapper().toJson(demoDto));
        }
//
        System.out.println(System.currentTimeMillis() - start);
//        System.out.println(JsonMapper.buildNonNullMapper().toJson(demoDto));


//        Convertable converter = Convertable.getConverter(Integer.class);
//        System.out.println(converter.convert("", Integer.class) + "");
    }
}
