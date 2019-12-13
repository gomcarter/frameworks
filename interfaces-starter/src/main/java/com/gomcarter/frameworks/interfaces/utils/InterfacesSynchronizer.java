package com.gomcarter.frameworks.interfaces.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
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

import java.net.URI;
import java.nio.charset.Charset;

import static com.gomcarter.frameworks.interfaces.utils.InterfacesRegister.register;

/**
 * @author gomcarter
 */
public class InterfacesSynchronizer {

    private static String INTERFACE_DOMAIN_KEY = "interfaces.domain";
    private static String domain = System.getProperty(INTERFACE_DOMAIN_KEY);
    private static String javaId = System.getProperty("interfaces.javaId");
    public static boolean NEED_PUSH = domain != null && domain.startsWith("http") && javaId != null;

    public InterfacesSynchronizer() {

    }


    void sync() throws Exception {
        if (!domain.endsWith("/")) {
            domain += "/";
        }
        URIBuilder uriBuilder = new URIBuilder(URI.create(domain + "publics/interfaces?javaId=" + javaId));

        HttpPost httpPost = new HttpPost(uriBuilder.build());

        ObjectMapper mapper = new ObjectMapper();
        //设置输出时包含属性的风格
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //禁止使用int代表Enum的order()來反序列化Enum,非常危險
        mapper.configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, true);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);

        httpPost.setEntity(new StringEntity(mapper.writeValueAsString(register()), ContentType.APPLICATION_JSON));

        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                .build();

        CloseableHttpClient httpClientLocal = HttpClients.custom()
                .setDefaultRequestConfig(defaultRequestConfig)
                .build();

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

        httpClientLocal.close();
    }
}
