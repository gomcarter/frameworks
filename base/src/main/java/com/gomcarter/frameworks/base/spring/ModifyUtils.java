package com.gomcarter.frameworks.base.spring;

import com.gomcarter.frameworks.config.mapper.JsonMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;

import java.io.IOException;
import java.util.Collection;

/**
 * @author gomcarter
 */
public abstract class ModifyUtils {

    public static Object calcValue(MethodParameter methodParameter, String[] values) {
        if (values == null) {
            return null;
        }
        String value;
        Class<?> clz = methodParameter.getParameterType();
        if (Collection.class.isAssignableFrom(clz) || clz.isArray()) {
            if (values.length == 1) {
                value = values[0];
                if (!value.startsWith("[")) {
                    value = ModifyUtils.mark2List(value);
                }
            } else {
                value = ModifyUtils.mark2List(StringUtils.join(values, ','));
            }
        } else {
            value = values.length > 0 ? values[0] : null;
        }
        try {
            return JsonMapper.buildNonNullTimeFormatMapper().fromJson(value, methodParameter.getGenericParameterType());
        } catch (IOException e1) {
            return values.length == 1 ? values[0] : values;
        }
    }

    public static String mark2List(String value) {
        return '[' + value + ']';
    }
}
