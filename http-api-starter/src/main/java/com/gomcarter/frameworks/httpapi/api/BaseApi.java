package com.gomcarter.frameworks.httpapi.api;

import com.fasterxml.jackson.databind.type.TypeFactory;
import com.gomcarter.frameworks.base.json.JsonTData;
import com.gomcarter.frameworks.base.mapper.JsonMapper;
import com.gomcarter.frameworks.httpapi.annotation.Method;
import com.gomcarter.frameworks.httpapi.auth.ApiAuthStrategy;
import com.gomcarter.frameworks.httpapi.auth.AuthStrategy;
import com.gomcarter.frameworks.httpapi.impl.DefaultHttpClientManager;
import com.gomcarter.frameworks.httpapi.impl.HttpClientTemplate;
import com.gomcarter.frameworks.httpapi.impl.handler.DefaultResponseHandler;
import com.gomcarter.frameworks.httpapi.message.request.PostRequestMessage;
import com.gomcarter.frameworks.httpapi.message.request.RequestMessage;
import com.gomcarter.frameworks.httpapi.request.GetRequest;
import com.gomcarter.frameworks.httpapi.request.PostRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 仅只能处理系统内部接口调用，因为这里会统一处理返回结果为JsonObject的对象；
 * 如果需要用到外部调用，请另行封装
 * <p>
 * 如果需要扩展： 找  gomcarter
 */
public abstract class BaseApi implements DisposableBean, InitializingBean {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected TypeFactory typeFactory = TypeFactory.defaultInstance();

    protected HttpClientTemplate httpClientTemplate;

    protected AuthStrategy authStrategy = new ApiAuthStrategy();

    public void init() {
        if (httpClientTemplate == null) {
            httpClientTemplate = new HttpClientTemplate();
        }

        if (httpClientTemplate.getHttpClientManager() == null) {
            DefaultHttpClientManager httpClientManager = new DefaultHttpClientManager();
            httpClientManager.init();

            this.httpClientTemplate.setHttpClientManager(httpClientManager);
        }

        httpClientTemplate.setUrlRequestRouter(getUrlRouter());

        httpClientTemplate.init();
    }

    protected abstract Map<String, String> getUrlRouter();


    /* *******************
     * 以下POST方法
     ******************* */
    public String postWithFile(String urlKey, Map<String, Object> params, Map<String, String> headers, Map<String, InputStream> files) {
        return this.httpExecute(Method.POST, urlKey, params, null, null, headers, files);
    }

    protected String postWithBody(String urlKey, String body, Map<String, String> headers) {
        return this.httpExecute(Method.POST, urlKey, null, null, body, headers, null);
    }

    protected <T> T postWithBody(String urlKey, Class<T> kls, String body, Map<String, String> headers) {
        return this.post(urlKey, kls, null, null, headers, body);
    }

    protected String postWithBody(String urlKey, List<String> restParams, String body, Map<String, String> headers) {
        return this.httpExecute(Method.POST, urlKey, null, restParams, body, headers, null);
    }

    protected <T> List<T> postList(String urlKey, Class<T> kls) {
        return this.postList(urlKey, kls, null, null);
    }

    protected <T> List<T> postList(String urlKey, Class<T> kls, Map<String, Object> params) {
        return this.postList(urlKey, kls, params, null);
    }

    protected <T> List<T> postList(String urlKey, Class<T> kls, Map<String, Object> params, Map<String, String> headers) {
        String r = this.httpExecute(Method.POST, urlKey, params, headers);

        JsonTData<List<T>> data = JsonMapper.buildNonNullMapper().fromJson(r, typeFactory.constructParametricType(JsonTData.class,
                typeFactory.constructParametricType(List.class, kls)));

        if (!data.isSuccess()) {
            throw new RuntimeException("post list接口调用失败：" + data.getMessage());
        }
        return data.getExtra();
    }

    protected <T> T post(String urlKey, Class<T> kls) {
        return this.post(urlKey, kls, null, null, null, null);
    }

    protected <T> T post(String urlKey, Class<T> kls, Map<String, Object> params) {
        return this.post(urlKey, kls, params, null, null, null);
    }

    protected <T> T post(String urlKey, Class<T> kls, List<String> restParams) {
        return this.post(urlKey, kls, null, restParams, null, null);
    }

    protected <T> T post(String urlKey, Class<T> kls, Map<String, Object> params, Map<String, String> header) {
        return this.post(urlKey, kls, params, null, header, null);
    }

    protected <T> T post(String urlKey, Class<T> kls, Map<String, Object> params, List<String> restParams, Map<String, String> headers, String body) {
        String r = this.httpExecute(Method.POST, urlKey, params, restParams, body, headers, null);

        JsonTData<T> data = JsonMapper.buildNonNullMapper().fromJson(r, typeFactory.constructParametricType(JsonTData.class, typeFactory.constructType(kls)));

        if (!data.isSuccess()) {
            throw new RuntimeException("post接口调用失败：" + data.getMessage());
        }

        return data.getExtra();
    }

    /* *******************
     * 以下GET方法
     ******************* */
    protected <T> List<T> getList(String urlKey, Class<T> kls) {
        return this.getList(urlKey, kls, null, null);
    }

    protected <T> List<T> getList(String urlKey, Class<T> kls, Map<String, Object> params) {
        return this.getList(urlKey, kls, params, null);
    }

    protected <T> List<T> getList(String urlKey, Class<T> kls, Map<String, Object> params, Map<String, String> headers) {
        String r = this.httpExecute(Method.GET, urlKey, params, headers);

        JsonTData<List<T>> data = JsonMapper.buildNonNullMapper().fromJson(r, typeFactory.constructParametricType(JsonTData.class,
                typeFactory.constructParametricType(List.class, kls)));

        if (!data.isSuccess()) {
            throw new RuntimeException("get list接口调用失败：" + data.getMessage());
        }
        return data.getExtra();
    }

    protected <T> T get(String urlKey, Class<T> kls) {
        return this.get(urlKey, kls, null, null);
    }

    protected <T> T get(String urlKey, Class<T> kls, Map<String, Object> params) {
        return this.get(urlKey, kls, params, null);
    }

    protected <T> T get(String urlKey, Class<T> kls, Map<String, Object> params, Map<String, String> headers) {
        String r = this.httpExecute(Method.GET, urlKey, params, headers);


        JsonTData<T> data = JsonMapper.buildNonNullMapper().fromJson(r, typeFactory.constructParametricType(JsonTData.class, typeFactory.constructType(kls)));

        if (!data.isSuccess()) {
            throw new RuntimeException("接口调用失败：" + data.getMessage());
        }

        return data.getExtra();
    }

    protected <T> T httpExecute(Method method, String urlKey, Class<T> kls, Map<String, Object> params, Map<String, String> headers) {
        return JsonMapper.buildNonNullMapper().fromJson(this.httpExecute(method, urlKey, params, headers), kls);
    }

    protected String httpExecute(Method method, String urlKey) {
        return this.httpExecute(method, urlKey, null, null);
    }

    protected String httpExecute(Method method, String urlKey, Map<String, Object> params) {
        return this.httpExecute(method, urlKey, params, null);
    }

    protected String httpExecute(Method method, String urlKey, Map<String, Object> params, Map<String, String> headers) {
        return this.httpExecute(method, urlKey, params, null, null, headers, null);
    }

    /**
     * @param method     method
     * @param urlKey     urlKey
     * @param params     params
     * @param restParams restParams
     * @param body       body
     * @param headers    headers
     * @param files      files
     * @return result
     */
    public String httpExecute(Method method, String urlKey, Map<String, Object> params, List<String> restParams,
                                 String body, Map<String, String> headers, Map<String, InputStream> files) {
        params = ObjectUtils.defaultIfNull(params, new HashMap<>());
        logger.info("调用接口：{}，参数 ：{}, {}，{}",
                httpClientTemplate.getUrlRequestRouter().get(urlKey),
                JsonMapper.buildNonNullMapper().toJson(params),
                body,
                headers);
        long start = System.currentTimeMillis();

        String result;
        try {
            if (method == Method.GET) {

                /*初始化参数，url，api公有认证headers*/
                RequestMessage message = authStrategy.init(new GetRequest(urlKey).addAllParams(params).getRequestMessage());

                /*额外headers*/
                addHeaders(message, headers);

                result = httpClientTemplate.executeGet(message, new DefaultResponseHandler());
            } else if (method == Method.POST) {
                PostRequestMessage message = new PostRequest<>(urlKey).addAllParams(params).getPostRequestMessage();

                if (StringUtils.isNotBlank(body)) {
                    message.setBody(body);
                }

                if (null != restParams) {
                    message.setRestParameters(restParams);
                }

                if (files != null && !files.isEmpty()) {
                    message.setFiles(files);
                }

                authStrategy.init(message);

                addHeaders(message, headers);

                result = httpClientTemplate.executePost(message, new DefaultResponseHandler());
            } else {
                throw new RuntimeException("method " + method + " not support yet");
            }
        } catch (Exception e) {
            logger.error("接口调用失败，用时：{}ms", System.currentTimeMillis() - start, e);

            throw new RuntimeException("接口调用失败！");
        }

        logger.info("用时：{}ms, 调用结果：{}", System.currentTimeMillis() - start, result);
        return result;
    }

    private void addHeaders(RequestMessage message, Map<String, String> headers) {
        if (headers != null) {
            for (String key : headers.keySet()) {
                message.addHeader(key, headers.get(key));
            }
        }
    }

    /**
     * 可以自定义 HttpClientTemplate 注入
     */
    public void setHttpClientTemplate(HttpClientTemplate httpClientTemplate) {
        this.httpClientTemplate = httpClientTemplate;
    }

    public void appendUrlRequestRouter(String urlKey, String url) {
        this.httpClientTemplate.appendUrlRequestRouter(urlKey, url);
    }

    @Override
    public void destroy() throws Exception {
        this.httpClientTemplate.destroy();
        logger.info("api destroyed");
    }

    @Override
    public void afterPropertiesSet() {
        this.init();
    }
}
