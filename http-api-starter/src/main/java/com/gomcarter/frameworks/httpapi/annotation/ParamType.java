package com.gomcarter.frameworks.httpapi.annotation;

import com.gomcarter.frameworks.base.mapper.JsonMapper;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author gomcarter
 */
public enum ParamType {
    /**
     * DEFAULT
     */
    DEFAULT {
        @Override
        void map(Param params, Parameter parameter, HttpParam param, Object value) {
            if (Map.class.isAssignableFrom(parameter.getType())) {
                if (params.params == null) {
                    params.params = (Map<String, Object>) value;
                } else {
                    params.params.putAll((Map<? extends String, ? extends String>) value);
                }
            } else {
                if (params.params == null) {
                    params.params = new HashMap<>();
                }

                String key = param.value();
                if (StringUtils.isBlank(key)) {
                    throw new RuntimeException("the [" + parameter + "] parameter need key");
                }

                params.params.put(key, value);
            }
        }
    },
    /**
     * HEADER
     */
    HEADER {
        @Override
        void map(Param params, Parameter parameter, HttpParam param, Object value) {
            if (Map.class.isAssignableFrom(parameter.getType())) {
                if (params.header == null) {
                    params.header = (Map<String, String>) value;
                } else {
                    params.header.putAll((Map<? extends String, ? extends String>) value);
                }
            } else {
                if (StringUtils.isBlank(param.value())) {
                    throw new RuntimeException("the [" + parameter + "] parameter need key");
                }
                if (params.header == null) {
                    params.header = new HashMap<>();
                } else {
                    params.header.put(param.value(), value.toString());
                }
            }
        }
    },
    /**
     * INPUT_STREAM
     */
    INPUT_STREAM {
        @Override
        void map(Param params, Parameter parameter, HttpParam param, Object value) {
            if (Map.class.isAssignableFrom(parameter.getType())) {
                if (params.inputStreamMap == null) {
                    params.inputStreamMap = (Map<String, InputStream>) value;
                } else {
                    params.inputStreamMap.putAll((Map<? extends String, ? extends InputStream>) value);
                }
            } else {
                if (StringUtils.isBlank(param.value())) {
                    throw new RuntimeException("the [" + parameter + "] parameter need key");
                }

                if (params.inputStreamMap == null) {
                    params.inputStreamMap = new HashMap<>();
                } else {
                    params.inputStreamMap.put(param.value(), (InputStream) value);
                }
            }
        }
    },
    /**
     * REST
     */
    REST {
        @Override
        void map(Param params, Parameter parameter, HttpParam param, Object value) {
            if (params.restParams == null) {
                params.restParams = new ArrayList<>();
            }
            params.restParams.add(value.toString());
        }
    },
    /**
     * BODY
     */
    BODY {
        @Override
        void map(Param params, Parameter parameter, HttpParam param, Object value) {
            if (params.body == null) {
                params.body = new StringBuilder(JsonMapper.buildNonNullMapper().toJson(value));
            } else {
                params.body.append(JsonMapper.buildNonNullMapper().toJson(value));
            }
        }
    };

    abstract void map(Param params, Parameter parameter, HttpParam param, Object value);

    public void defaultMap(Param params, Parameter parameter, HttpParam param, Object value) {
        if (value != null) {
            this.map(params, parameter, param, value);
        } else if (param.required()) {
            throw new RuntimeException("the [" + parameter + "] parameter is required");
        }
    }

    @Data
    @Accessors(chain = true)
    public static class Param {
        StringBuilder body = null;
        Map<String, String> header = null;
        Map<String, Object> params = new HashMap<>();
        List<String> restParams = null;
        Map<String, InputStream> inputStreamMap = null;
    }
}
