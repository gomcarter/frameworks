package com.gomcarter.frameworks.interfaces.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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

/**
 * @author gomcarter
 */
public class InterfacesSynchronizer {
    private static final Logger logger = LoggerFactory.getLogger(InterfacesSynchronizer.class);

    public static void sync() {
        String interfaceDomainKey = "interfaces.domain";
        String domain = System.getProperty(interfaceDomainKey);
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

            ObjectMapper mapper = new ObjectMapper()
                    //设置输出时包含属性的风格
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    //禁止使用int代表Enum的order()來反序列化Enum,非常危險
                    .configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, true);

            // build http client
            URIBuilder uriBuilder = new URIBuilder(URI.create(domain + "publics/interfaces?javaId=" + javaId));
            HttpPost httpPost = new HttpPost(uriBuilder.build());
            httpPost.setEntity(new StringEntity(mapper.writeValueAsString(InterfacesRegister.register()), ContentType.APPLICATION_JSON));

            httpClientLocal.execute(httpPost, (response) -> {
                StatusLine statusLine = response.getStatusLine();

                if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
                    throw new RuntimeException("statusCode=" + statusLine.getStatusCode() + ",reason:" + statusLine.getReasonPhrase());
                }

                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    throw new ClientProtocolException("Response no content return.");
                }

                return EntityUtils.toString(entity, Charset.forName("UTF-8"));
            });
        } catch (Exception e) {
            logger.error("some error happened when interfaces sync: ", e);
        }
    }
}
