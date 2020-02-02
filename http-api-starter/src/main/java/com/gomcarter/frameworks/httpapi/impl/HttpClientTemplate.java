package com.gomcarter.frameworks.httpapi.impl;

import com.gomcarter.frameworks.base.common.AssertUtils;
import com.gomcarter.frameworks.httpapi.HttpClientManager;
import com.gomcarter.frameworks.httpapi.annotation.Method;
import com.gomcarter.frameworks.httpapi.config.HttpClientConfig;
import com.gomcarter.frameworks.httpapi.impl.handler.DefaultResponseHandler;
import com.gomcarter.frameworks.httpapi.message.MessageConfig;
import com.gomcarter.frameworks.httpapi.message.request.RequestMessage;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpMessage;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * @author gomcarter
 */
public class HttpClientTemplate {

    private HttpClientManager httpClientManager;

    private HttpClientConfig httpClientConfig;

    private HttpRequestRetryHandler httpRequestRetryHandler;

    private ConnectionKeepAliveStrategy connectionKeepAliveStrategy;

    private CloseableHttpClient httpClient;

    /**
     * Key: Api name
     * Value: MessageConfig
     */
    private Map<String, MessageConfig> requestRouter = new HashMap<>();

    /**
     * Key: Api name
     * Value: host url
     */
    private Map<String, String> urlRequestRouter = new HashMap<>();

    public void init() {
        if (httpClientConfig == null) {
            httpClientConfig = new HttpClientConfig();
        }
        if (httpRequestRetryHandler == null) {
            httpRequestRetryHandler = new DefaultHttpRequestRetryHandler(httpClientConfig.getRetryCount(), true);
        }
        if (connectionKeepAliveStrategy == null) {
            connectionKeepAliveStrategy = new BasicConnectionKeepAliveStrategy(httpClientConfig.getHostToKeepAliveDuration());
        }

        httpClient = httpClientManager.getHttpClient(httpClientConfig, httpRequestRetryHandler, connectionKeepAliveStrategy);
    }

    public void destroy() {
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public String execute(Method method, RequestMessage requestMessage) throws IOException, URISyntaxException {
        HttpRequestBase http = buildHttpRequest(method, requestMessage);
        return httpClient.execute(http, new DefaultResponseHandler());
    }

    public <T> T execute(Method method, RequestMessage requestMessage, ResponseHandler<T> responseHandler) throws IOException, URISyntaxException {
        HttpRequestBase http = buildHttpRequest(method, requestMessage);
        return httpClient.execute(http, responseHandler);
    }

    private void addHeader(RequestMessage requestMessage, HttpMessage http) {
        for (Map.Entry<String, String> entry : requestMessage.getHeaders().entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }

            http.addHeader(entry.getKey(), entry.getValue());
        }
    }

    private HttpEntity buildHttpEntity(RequestMessage requestMessage) {
        if (isNeedMultipart(requestMessage)) {
            return buildMultipartEntity(requestMessage);
        } else {
            return buildSimpleEntity(requestMessage);
        }
    }

    private HttpEntity buildMultipartEntity(RequestMessage requestMessage) {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();

//        if (!MapUtils.isEmpty(requestMessage.getParameters())) {
//            for (Map.Entry<String, Set<String>> entry : requestMessage.getParameters().entrySet()) {
//                for (String value : entry.getValue()) {
//                    builder.addPart(entry.getKey(), new StringBody(value, requestMessage.getContentType()));
//                }
//            }
//        }

        if (!StringUtils.isEmpty(requestMessage.getBody())) {
            builder.addPart(requestMessage.getBodyPartName(), new StringBody(requestMessage.getBody(), requestMessage.getContentType()));
        }

        if (!MapUtils.isEmpty(requestMessage.getFiles())) {
            for (Map.Entry<String, InputStream> entry : requestMessage.getFiles().entrySet()) {
                builder.addPart(entry.getKey(), new InputStreamBody(entry.getValue(), ContentType.APPLICATION_OCTET_STREAM));
            }
        }

        return builder.build();
    }

    private HttpEntity buildSimpleEntity(RequestMessage requestMessage) {
        if (!StringUtils.isEmpty(requestMessage.getBody())) {
            return buildBodyEntity(requestMessage);
        }

        if (!MapUtils.isEmpty(requestMessage.getFiles())) {
            return buildFileEntity(requestMessage);
        }

        return null;
    }

    private HttpEntity buildParametersEntity(RequestMessage requestMessage) {
        List<BasicNameValuePair> params = new ArrayList<>(requestMessage.getParameters().size());

        for (Map.Entry<String, Set<String>> entry : requestMessage.getParameters().entrySet()) {
            if (entry.getKey() == null || CollectionUtils.isEmpty(entry.getValue())) {
                continue;
            }

            for (String value : entry.getValue()) {
                params.add(new BasicNameValuePair(entry.getKey(), value));
            }
        }

        return new UrlEncodedFormEntity(params, requestMessage.getCharset());
    }

    private HttpEntity buildBodyEntity(RequestMessage requestMessage) {
        return new StringEntity(requestMessage.getBody(), requestMessage.getContentType());
    }

    private HttpEntity buildFileEntity(RequestMessage requestMessage) {
        return new InputStreamEntity(requestMessage.getFiles().values().iterator().next());
    }

    private boolean isNeedMultipart(RequestMessage requestMessage) {
        return MapUtils.isNotEmpty(requestMessage.getFiles()) && StringUtils.isNotBlank(requestMessage.getBody());
    }

    private String replacePlaceHolders(RequestMessage requestMessage, String apiPath) {
        if (!CollectionUtils.isEmpty(requestMessage.getRestParameters())) {
            apiPath = String.format(apiPath, requestMessage.getRestParameters().toArray());
        }

        return apiPath;
    }

    private HttpEntityEnclosingRequestBase buildHttpRequest(Method method, RequestMessage requestMessage) throws URISyntaxException {
        MessageConfig messageConfig = requestRouter.get(requestMessage.getApiName());

        URIBuilder uriBuilder = null;
        if (messageConfig != null) {
            String path = replacePlaceHolders(requestMessage, messageConfig.getApiPath());

            uriBuilder = new URIBuilder().setScheme(messageConfig.getUrlSchema())
                    .setHost(messageConfig.getHost())
                    .setPort(messageConfig.getPort())
                    .setPath(path);
        } else {
            String url = urlRequestRouter.get(requestMessage.getApiName());
            if (url != null) {
                url = replacePlaceHolders(requestMessage, url);
                uriBuilder = new URIBuilder(URI.create(url));
            }
        }

        if (uriBuilder == null) {
            throw new IllegalArgumentException("No message config for api " + requestMessage.getApiName());
        }

        HttpEntityEnclosingRequestBase http = buildMethod(method, uriBuilder, requestMessage);
        // 加header
        addHeader(requestMessage, http);

        // 加body和file
        HttpEntity httpEntity = buildHttpEntity(requestMessage);
        if (httpEntity != null) {
            http.setEntity(httpEntity);
            http.setHeader(httpEntity.getContentType());
        }
        return http;
    }

    private HttpEntityEnclosingRequestBase buildMethod(Method method, URIBuilder uriBuilder, RequestMessage requestMessage) throws URISyntaxException {
        AssertUtils.notNull(method);

        requestMessage.getParameters().forEach((key, valueSet) -> {
            if (StringUtils.isNotBlank(key) && CollectionUtils.isNotEmpty(valueSet)) {
                valueSet.forEach(v -> uriBuilder.addParameter(key, v));
            }
        });

        switch (method) {
            case GET:
                return new HttpGetSupportBody(uriBuilder.build());
            case POST:
                return new HttpPost(uriBuilder.build());
            case PUT:
                return new HttpPut(uriBuilder.build());
            case PATCH:
                return new HttpPatch(uriBuilder.build());
            case DELETE:
                return new HttpDeleteSupportBody(uriBuilder.build());
            default:
                throw new IllegalArgumentException("Not support method " + method);
        }
    }

    public HttpClientManager getHttpClientManager() {
        return httpClientManager;
    }

    public void setHttpClientManager(HttpClientManager httpClientManager) {
        this.httpClientManager = httpClientManager;
    }

    public Map<String, MessageConfig> getRequestRouter() {
        return requestRouter;
    }

    public void setRequestRouter(Map<String, MessageConfig> requestRouter) {
        this.requestRouter = requestRouter;
    }

    public Map<String, String> getUrlRequestRouter() {
        return urlRequestRouter;
    }

    public void setUrlRequestRouter(Map<String, String> urlRequestRouter) {
        this.urlRequestRouter = urlRequestRouter;
    }

    public HttpClientConfig getHttpClientConfig() {
        return httpClientConfig;
    }

    public void setHttpClientConfig(HttpClientConfig httpClientConfig) {
        this.httpClientConfig = httpClientConfig;
    }

    public HttpRequestRetryHandler getHttpRequestRetryHandler() {
        return httpRequestRetryHandler;
    }

    public void setHttpRequestRetryHandler(HttpRequestRetryHandler httpRequestRetryHandler) {
        this.httpRequestRetryHandler = httpRequestRetryHandler;
    }

    public ConnectionKeepAliveStrategy getConnectionKeepAliveStrategy() {
        return connectionKeepAliveStrategy;
    }

    public void setConnectionKeepAliveStrategy(ConnectionKeepAliveStrategy connectionKeepAliveStrategy) {
        this.connectionKeepAliveStrategy = connectionKeepAliveStrategy;
    }

    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    public void appendUrlRequestRouter(String urlKey, String url) {
        this.urlRequestRouter.put(urlKey, url);
    }
}
