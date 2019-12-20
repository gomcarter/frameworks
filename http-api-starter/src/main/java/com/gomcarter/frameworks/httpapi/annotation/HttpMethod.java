package com.gomcarter.frameworks.httpapi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author gomcarter
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpMethod {

    /**
     * @return 发起请求方式
     */
    Method method();

    /**
     * @return 在 nacos 配置：   key=http://xx.com/yy/zz  中的 key
     */
    String key();

    /**
     * 结果是否被
     * {@link com.gomcarter.frameworks.base.json.JsonObject }
     * 包裹，如果是，则会自动判断接口是否调用成功，成功则将extra 解析到接口返回值中
     *
     * @return 结果是否被  jsonData 包裹
     */
    boolean wrap() default true;

}
