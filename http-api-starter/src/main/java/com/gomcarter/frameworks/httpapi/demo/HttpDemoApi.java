package com.gomcarter.frameworks.httpapi.demo;

import com.gomcarter.frameworks.httpapi.annotation.*;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 本示例是基于 nacos （diamond）作为配置中心的
 *
 * @author gomcarter
 */
@HttpBean({"MEMBER", "API"})
public interface HttpDemoApi {

    /**
     * @param data           body 类型
     * @param id             基本参数类型
     * @param header         放置 header 中
     * @param headerMap      headerMap
     * @param restParams     如果接口为： http://domain.com/%s/%s?id=%s， 那么restParams依次替换接口 %s 占位符
     * @param inputStream    上传文件流
     * @param inputStreamMap 上传文件流 Map&lt;key, inputStream &gt;   也可以是非 Map 参数直接是 InputStream
     * @return 返回结果
     */
    @HttpMethod(method = Method.GET, key = "member.get.by.idList")
    List<DemoDto> post(@HttpParam(type = ParamType.BODY) DemoDto data,
                       @HttpParam("idList") Long id,
                       @HttpParam(value = "headerKey", type = ParamType.HEADER, defaultValue = "123") Object header,
                       @HttpParam(type = ParamType.HEADER) Map<String, String> headerMap,
                       @HttpParam(type = ParamType.REST) List<String> restParams,
                       @HttpParam(value = "inputStreamKey", type = ParamType.INPUT_STREAM, required = false) InputStream inputStream,
                       @HttpParam(type = ParamType.INPUT_STREAM, required = false) Map<String, Object> inputStreamMap);

    default List<DemoDto> post(DemoDto data,
                               Long id,
                               Object header,
                               Map<String, String> headerMap,
                               List<String> restParams) {
        return this.post(data, id, header, headerMap, restParams, null, null);
    }
}
