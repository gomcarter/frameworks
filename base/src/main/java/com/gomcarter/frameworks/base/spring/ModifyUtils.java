package com.gomcarter.frameworks.base.spring;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Collection;

/**
 * @author gomcarter
 */
public abstract class ModifyUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final TypeFactory TYPE_FACTORY = TypeFactory.defaultInstance();

    static {
        MAPPER.setSerializationInclusion(Include.NON_NULL);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, true);
        MAPPER.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        MAPPER.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }

    public static <T> T fromJson(String jsonString, Type type) throws IOException {
        return MAPPER.readValue(jsonString, TYPE_FACTORY.constructType(type));
    }

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
            return fromJson(value, methodParameter.getGenericParameterType());
        } catch (IOException e1) {
            return values.length == 1 ? values[0] : values;
        }
    }

    public static String mark2List(String value) {
        return '[' + value + ']';
    }
}
