package com.gomcarter.frameworks.http.proxy;

import com.gomcarter.frameworks.base.exception.CustomException;
import com.gomcarter.frameworks.config.annotation.ConfigurableValue;
import com.gomcarter.frameworks.config.annotation.ConfigurableValues;
import com.gomcarter.frameworks.config.converter.Convertable;
import com.gomcarter.frameworks.config.mapper.JsonMapper;
import com.gomcarter.frameworks.config.utils.ReflectionUtils;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.core.io.InputStreamSource;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.net.URLEncoder;
import java.util.*;

public class HttpApiProxyHandler implements MethodInterceptor {
    private HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
    private OkHttpClient client;
    private String host;
    private Properties config;

    public static <T> T getProxy(Class<T> interfaceClass, String host, Properties config) {
        Enhancer enhancer = new Enhancer();
        // 设置enhancer对象的父类
        enhancer.setSuperclass(interfaceClass);
        // 设置enhancer的回调对象
        enhancer.setCallback(new HttpApiProxyHandler(host, config));
        // 创建代理对象
        return (T) enhancer.create();
        // return (T) (Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass}, this));
    }

    public HttpApiProxyHandler(String host, Properties config) {
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder()
                .addInterceptor(loggingInterceptor);
        client = builder.build();
        this.host = host;
        this.config = config;
    }

    public HttpApiProxyHandler setHost(String host) {
        this.host = host;
        return this;
    }

    private Request buildRequest(Method method, Object[] args) {
        Parameter[] parameters = method.getParameters();
        Request.Builder builder = new Request.Builder();

        Object body = null;
        Map<Object, Object> params = new HashMap<>();
        for (int i = 0; i < parameters.length; ++i) {
            Parameter parameter = parameters[i];
            RequestHeader headerAnnotation;
            if ((headerAnnotation = parameter.getAnnotation(RequestHeader.class)) != null) {
                String paramKey = StringUtils.defaultIfBlank(headerAnnotation.value(), parameter.getName());
                putHeaders(builder, paramKey, args[i]);
            } else if (parameter.getAnnotation(RequestBody.class) != null) {
                body = args[i];
            } else {
                // 当参数来处理
                String paramKey = parameter.getName();
                RequestParam param = parameter.getAnnotation(RequestParam.class);
                if (param != null && StringUtils.isNotBlank(param.value())) {
                    paramKey = param.value();
                }
                putParams(params, paramKey, args[i]);
            }
        }

        ConfigurableValues cvsAnnotation = method.getAnnotation(ConfigurableValues.class);
        if (cvsAnnotation != null) {
            for (String cvs : cvsAnnotation.value()) {
                if (StringUtils.isNotBlank(cvs)) {
                    params.putIfAbsent(cvs, config.getProperty(cvs));
                }
            }
        }
        ConfigurableValue cvAnnotation = method.getAnnotation(ConfigurableValue.class);
        if (cvAnnotation != null && StringUtils.isNotBlank(cvAnnotation.value())) {
            params.putIfAbsent(cvAnnotation.value(), config.getProperty(cvAnnotation.value()));
        }

        return buildRequest(method, builder, params, body).build();
    }

    private Field[] getFields(Class kls) {
        Field[] fields = kls.getDeclaredFields();
        Class superKls = kls;
        while ((superKls = superKls.getSuperclass()) != null) {
            Field[] superFields = superKls.getDeclaredFields();
            int newLength = superFields.length;
            if (newLength > 0) {
                Field[] original = fields;
                fields = new Field[fields.length + newLength];

                System.arraycopy(superFields, 0, fields, 0, newLength);
                System.arraycopy(original, 0, fields, newLength, original.length);
            }
        }
        return fields;
    }

    private void putParams(Map<Object, Object> params, String paramKey, Object value) {
        if (value == null) {
            return;
        }
        Class kls = value.getClass();
        if (value instanceof Map) {
            params.putAll((Map) value);
        } else if (BeanUtils.isSimpleValueType(kls) || kls == Object.class
                || kls.isArray() || Iterable.class.isAssignableFrom(kls)) {
            params.put(paramKey, value);
        } else if (InputStreamSource.class.isAssignableFrom(kls) || File.class.isAssignableFrom(kls)) {
            // 是文件的时候
        } else {
            Arrays.stream(this.getFields(kls))
                    .filter(field -> !field.getName().contains("this$"))
                    .filter(field -> (field.getModifiers() & Modifier.STATIC) != Modifier.STATIC)
                    .forEach(field -> putParams(params, field.getName(), ReflectionUtils.getFieldValue(value, field)));
        }
    }

    private void putHeaders(Request.Builder builder, String paramKey, Object value) {
        if (value != null) {
            if (value instanceof Map) {
                for (Map.Entry<?, ?> pair : ((Map<?, ?>) value).entrySet()) {
                    Object k = pair.getKey();
                    Object v = pair.getValue();
                    if (k != null && v != null) {
                        builder.addHeader(k.toString(), value.toString());
                    }
                }
            } else {
                builder.addHeader(paramKey, value.toString());
            }
        }
    }

    private Request.Builder buildRequest(Method method, Request.Builder builder, Map<?, ?> params, Object body) {
        RequestMethod m = null;
        String path = null;
        GetMapping get = method.getAnnotation(GetMapping.class);
        if (get != null) {
            m = RequestMethod.GET;
            path = get.value()[0];
        }

        PostMapping post = method.getAnnotation(PostMapping.class);
        if (post != null) {
            m = RequestMethod.POST;
            path = post.value()[0];
        }
        if (m == null) {
            throw new CustomException(method.getName() + "仅支持POST和GET");
        }

        okhttp3.RequestBody requestBody = null;
        if (body != null && m != RequestMethod.GET) {
            String stringBody = JsonMapper.buildNonNullMapper().toJson(body);
            requestBody = okhttp3.RequestBody.create(StringUtils.defaultIfBlank(stringBody, ""), MediaType.parse("application/json"));
        }

        String url = this.host + path;
        if (url.contains("?")) {
            url = url + "&" + this.toQueryString(params);
        } else {
            url = url + "?" + this.toQueryString(params);
        }

        return builder.url(url)
                .method(m.name(), requestBody);
    }

    public String toQueryString(Map<?, ?> data) {
        if (null == data) {
            return StringUtils.EMPTY;
        }
        try {
            StringBuilder queryString = new StringBuilder();
            for (Map.Entry<?, ?> pair : data.entrySet()) {
                Object key = pair.getKey();
                Object value = pair.getValue();
                if (value instanceof Collection) {
                    for (Object v : (Collection) value) {
                        queryString.append(key).append("=");
                        queryString.append(URLEncoder.encode(v + "", "UTF-8")).append("&");
                    }
                } else {
                    queryString.append(key).append("=");
                    queryString.append(URLEncoder.encode(value + "", "UTF-8")).append("&");
                }
            }
            if (queryString.length() > 0) {
                queryString.deleteCharAt(queryString.length() - 1);
            }
            return queryString.toString();
        } catch (Exception e) {
            throw new CustomException(e);
        }
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

    /**
     * 执行代理对象的所有方法时都会被替换成执行如下的invoke方法
     * <p>
     * target ：代表动态代理对象； method：代表正在执行的方法 ；args :代表执行代理对象方法时传入的实参
     */
    @Override
    public Object intercept(Object target, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        // object 自身方法， default 方法不代理
        if (!method.getDeclaringClass().isInterface()) {
            return method.invoke(this, args);
        } else if (isDefaultMethod(method)) {
            return invokeDefaultMethod(target, method, args);
        }

        Request request = this.buildRequest(method, args);

        ResponseBody response = client.newCall(request).execute().body();
        String res = response == null ? null : response.string();

        Class<?> resultType = method.getReturnType();
        if (resultType == String.class || resultType == void.class) {
            return res;
        }
        Convertable converter = Convertable.getConverter(resultType);
        return converter.convert(res, method.getGenericReturnType());
    }
//
//    private Object defaultValue(Parameter parameter, Object value, HttpParam param) {
//        if (value == null) {
//            Convertable converter = Convertable.getConverter(parameter.getType());
//            try {
//                return converter.convert(param.defaultValue(), ObjectUtils.defaultIfNull(parameter.getParameterizedType(), parameter.getType()));
//            } catch (Exception e) {
//                return null;
//            }
//        }
//        return value;
//    }
}
