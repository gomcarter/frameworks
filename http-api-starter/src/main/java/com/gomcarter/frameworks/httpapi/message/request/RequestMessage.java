package com.gomcarter.frameworks.httpapi.message.request;

import com.gomcarter.frameworks.httpapi.utils.RequestTool;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.http.entity.ContentType;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;

/**
 * @author gomcarter
 */
@Data
@Accessors(chain = true)
public class RequestMessage {

    private String apiName;

    private ContentType contentType;

    private Map<String, String> headers = new HashMap<>();

    private Map<String, Set<String>> parameters = new HashMap<>();
    /**
     * 替换url上的 %s
     */
    private List<String> restParameters = new ArrayList<>();

    private Map<String, InputStream> files;

    private String body;

    private String bodyPartName = "body";

    private Charset charset;

    public RequestMessage(String apiName, ContentType contentType, Charset charset) {
        super();
        this.apiName = apiName;
        this.contentType = contentType == null ? ContentType.APPLICATION_JSON : contentType;
        this.charset = charset == null ? Charset.defaultCharset() : charset;
    }

    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }

    public void addAllParameter(Map<String, String> params) {
        params.forEach(this::addParameter);
    }

    public void addParameter(String name, String value) {
        Set<String> values = parameters.computeIfAbsent(name, k -> new HashSet<>());
        values.add(value);
    }
}
