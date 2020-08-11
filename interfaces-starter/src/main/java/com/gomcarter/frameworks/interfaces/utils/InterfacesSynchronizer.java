package com.gomcarter.frameworks.interfaces.utils;

import com.gomcarter.frameworks.interfaces.dto.ApiInterface;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author gomcarter 2019-12-02 09:23:09
 */
public class InterfacesSynchronizer {
    private static final Logger logger = LoggerFactory.getLogger(InterfacesSynchronizer.class);

    public static void sync(List<ApiInterface> interfaces) {
        String interfaceDomainKey = "interfaces.domain";
        String domain = System.getProperty(interfaceDomainKey, "http://developer.dev.66buy.com.cn/");
        String javaId = System.getProperty("interfaces.javaId");

        if (domain == null || !domain.startsWith("http") || javaId == null) {
            logger.info("there is no interface center configured, then ignored.");
            return;
        }

        if (!domain.endsWith("/")) {
            domain += "/";
        }

        RequestConfig defaultRequestConfig = RequestConfig
                .custom()
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                .build();

        try (CloseableHttpClient httpClientLocal = HttpClients.custom()
                .setDefaultRequestConfig(defaultRequestConfig)
                .build()) {

            // build http client
            URIBuilder uriBuilder = new URIBuilder(URI.create(domain + "publics/interfaces?javaId=" + javaId));
            HttpPost httpPost = new HttpPost(uriBuilder.build());
            httpPost.setEntity(new StringEntity(JsonMapper.buildNonNullMapper().toJson(interfaces), ContentType.APPLICATION_JSON));

            httpClientLocal.execute(httpPost, (response) -> {
                StatusLine statusLine = response.getStatusLine();

                if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
                    throw new RuntimeException("statusCode=" + statusLine.getStatusCode() + ",reason:" + statusLine.getReasonPhrase());
                }

                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    throw new ClientProtocolException("Response no content return.");
                }

                String result = EntityUtils.toString(entity, Charset.forName("UTF-8"));
                logger.info("调用接口中心结果：{}", result);
                return result;
            });
        } catch (Exception e) {
            logger.error("some error happened when interfaces sync: ", e);
        }
    }
}
