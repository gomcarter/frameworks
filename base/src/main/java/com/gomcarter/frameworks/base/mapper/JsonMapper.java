package com.gomcarter.frameworks.base.mapper;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 简单封装Jackson实现JSON  &lt;=&gt; Java Object的Mapper.
 * <p>
 * 封装不同的输出风格, 使用不同的builder函数创建实例.
 *
 * @author gomcarter
 */
@Slf4j
public class JsonMapper {

    private static class Holder {
        private static final JsonMapper DEFAULT_MAPPER = new JsonMapper(Include.NON_NULL);
    }

    private static class TimeFormatHolder {
        private static final JsonMapper TIME_FORMAT_MAPPER = new JsonMapper(Include.NON_NULL, "yyyy-MM-dd HH:mm:ss");
    }

    private ObjectMapper mapper;

    public JsonMapper(Include inclusion) {
        this(inclusion, null);
    }

    public JsonMapper(Include inclusion, String timeFormat) {
        mapper = new ObjectMapper();
        //设置输出时包含属性的风格
        mapper.setSerializationInclusion(inclusion);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //禁止使用int代表Enum的order()來反序列化Enum,非常危險
        mapper.configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, true);
        mapper.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);

        if (timeFormat != null) {
            mapper.setDateFormat(new SimpleDateFormat(timeFormat));
        }
    }

    /**
     * 创建只输出非空属性到Json字符串的Mapper.
     * <p>
     * 此方法可废弃，请使用新方法  buildNotNullMapper
     *
     * @return JsonMapper
     */
    public static JsonMapper buildNonNullMapper() {
        return Holder.DEFAULT_MAPPER;
    }

    public static JsonMapper buildNonNullTimeFormatMapper() {
        return TimeFormatHolder.TIME_FORMAT_MAPPER;
    }

    /**
     * 如果JSON字符串为Null或"null"字符串, 返回Null.
     * 如果JSON字符串为"[]", 返回空集合.
     * <p>
     * 如需读取集合如List/Map, 且不是List&lt;String&gt;这种简单类型时使用如下语句,使用後面的函數.
     *
     * @param jsonString jsonString
     * @param clazz      clazz
     * @param <T>        clazz
     * @return instance of clazz
     */
    public <T> T fromJson(String jsonString, Class<T> clazz) {
        if (StringUtils.isEmpty(jsonString)) {
            return null;
        }

        try {
            return mapper.readValue(jsonString, clazz);
        } catch (IOException e) {
            log.error("Json转换出错", e);
            return null;
        }
    }


    /**
     * 構造泛型的Type如List&lt;MyBean&gt;, Map&lt;String,MyBean&gt;
     *
     * @param parametrized     parametrized class
     * @param parameterClasses parameterClasses
     * @return JavaType
     */
    public JavaType constructParametricType(Class<?> parametrized, Class<?>... parameterClasses) {
        return mapper.getTypeFactory().constructParametricType(parametrized, parameterClasses);
    }

    /**
     * 如果对象为Null, 返回"null".
     * 如果集合为空集合, 返回"[]".
     *
     * @param object object
     * @return json string
     */
    public String toJson(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (IOException e) {
            log.error("Json转换出错", e);
            return null;
        }
    }

    /**
     * 如果JSON字符串为Null或"null"字符串, 返回Null.
     * 如果JSON字符串为"[]", 返回空集合.
     * <p>
     * 如需读取集合如List/Map, 且不是List&lt;String&gt;時,
     * 先用constructParametricType(List.class,MyBean.class)構造出JavaTeype,再調用本函數.
     *
     * @param jsonString jsonString
     * @param javaType   javaType
     * @param <T>        class
     * @return instance of class
     */
    public <T> T fromJson(String jsonString, JavaType javaType) {
        if (StringUtils.isEmpty(jsonString)) {
            return null;
        }
        try {
            return mapper.readValue(jsonString, javaType);
        } catch (IOException e) {
            log.error("Json转换出错", e);
            return null;
        }
    }

    /**
     * @param jsonString jsonString
     * @param javaType   javaType
     * @param <T>        class
     * @return instance of class
     */
    public <T> T fromJson(String jsonString, TypeReference<T> javaType) {
        if (StringUtils.isEmpty(jsonString)) {
            return null;
        }
        try {
            return mapper.readValue(jsonString, javaType);
        } catch (IOException e) {
            log.error("Json转换出错", e);
            return null;
        }
    }

    /**
     * @param jsonString      jsonString
     * @param collectionClass collectionClass
     * @param clazz           clazz
     * @param <T>             clazz
     * @return Iterable instance of clazz
     */
    public <T> Iterable<T> fromJsonToCollection(String jsonString, Class<? extends Iterable> collectionClass, Class<T> clazz) {
        if (jsonString.startsWith("[")) {
            return fromJson(jsonString, constructParametricType(collectionClass, clazz));
        } else {
            //Single
            return fromJsonToCollection("[" + jsonString + "]", collectionClass, clazz);
        }
    }

    /**
     * @param jsonString jsonString
     * @param clazz      clazz
     * @param <T>        clazz
     * @return List instance of clazz
     */
    public <T> List<T> fromJsonToList(String jsonString, Class<T> clazz) {
        return (List<T>) this.fromJsonToCollection(jsonString, List.class, clazz);
    }
}
