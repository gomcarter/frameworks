package com.gomcarter.frameworks.interfaces.utils;

import com.gomcarter.frameworks.interfaces.dto.ApiBean;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.core.convert.support.GenericConversionService;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author gomcarter 2019-12-02 09:23:09
 */
public class MockUtils {

    public static Object mock(String returns) {
        if (returns == null) {
            return null;
        }
        return mock(JsonMapper.buildNonNullMapper().fromJson(returns, ApiBean.class));
    }

    public static Object mock(ApiBean bean) {
        return generateReturns(bean);
    }

    private static Object generateReturns(ApiBean node) {
        String type = node.getType();
        if (List.class.getSimpleName().equals(type)) {
            // 如果自身打标注了mock，那么以自身为准
            if (StringUtils.isNotBlank(node.getMock()) && StringUtils.isNotBlank(node.getClassName())) {
                Class<?> tKls = getTemplateClass(node.getClassName());
                return JsonMapper.buildNonNullMapper().fromJsonToList(node.getMock(), tKls);
            } else {
                // 自身没有标记mock，那么看实体类的生成规则，长度根据getMockLength定
                int length = node.getMockLength();
                List<Object> l = new ArrayList<>(length);
                for (int i = 0; i < node.getMockLength(); ++i) {
                    l.add(generateReturns(node.getChildren().get(0)));
                }
                return l;
            }
        } else if (Object.class.getSimpleName().equals(type)) {
            // 如果自身打标注了mock，那么以自身为准
            if (StringUtils.isNotBlank(node.getMock()) && StringUtils.isNotBlank(node.getClassName())) {
                Class<?> tKls = getThisClass(node.getClassName());
                return JsonMapper.buildNonNullMapper().fromJson(node.getMock(), tKls);
            } else if (node.getChildren() != null) {
                Map<String, Object> map = new HashMap<>(node.getChildren().size(), 1);
                for (ApiBean s : node.getChildren()) {
                    map.put(s.getKey(), generateReturns(s));
                }
                return map;
            } else {
                return new HashMap<>(0);
            }
        } else if (void.class.getSimpleName().equals(type)
                || File.class.getSimpleName().equals(type)
                || type == null) {
            return null;
        } else {
            try {
                String mock = node.getMock();
                Class<?> kls = getThisClass(node.getClassName());
                if (StringUtils.isBlank(mock) || "*".equals(mock)) {
                    // 随机生成
                    return random(kls);
                } else if (mock.startsWith("{") && mock.endsWith("}")) {
                    // 任意选一个值
                    return select(mock, kls);
                } else if (mock.startsWith("(") && mock.endsWith(")")) {
                    // 在范围内任意生成一个值
                    return rangeRandom(mock, kls);
                } else {
                    // 固定值
                    return convertSimple(mock, kls);
                }
            } catch (Exception e) {
                return null;
            }
        }
    }

    private static Object random(Class<?> kls) {
        if (kls == Boolean.class) {
            return new Random().nextInt(2) == 1;
        } else if (kls == Date.class) {
            // 过去30天到未来30天的任意时间
            return new Date((System.currentTimeMillis() - 2592000000L) + new Random().nextInt() % 5184000000L);
        } else {
            return convertSimple(new Random().nextInt() + "", kls);
        }
    }

    private static Object rangeRandom(String mock, Class<?> kls) {
        String[] values = mock.substring(0, mock.length() - 1).substring(1).split(",");
        if (kls == Boolean.class) {
            return new Random().nextInt(2) == 1;
        } else if (kls == Date.class) {
            Date min = fromDateString(values[0].trim());
            Date max = fromDateString(values[1].trim());
            return new Date(min.getTime() + Math.abs(new Random().nextLong()) % (max.getTime() - min.getTime()));
        } else {
            Long min = Long.valueOf(values[0].trim());
            Long max = Long.valueOf(values[1].trim());
            return convertSimple(min + Math.abs(new Random().nextLong()) % (max - min) + "", kls);
        }
    }

    public static Date fromDateString(String dateStr) {
        dateStr = StringUtils.replaceEach(dateStr, new String[]{
                "-", "\n",
        }, new String[]{
                "/", "",
        });
        if (StringUtils.contains(dateStr, ":")) {
            return fromDateString(dateStr, "yyyy/MM/dd HH:mm:ss");
        } else {
            return fromDateString(dateStr, "yyyy/MM/dd");
        }
    }

    public static Date fromDateString(String dateStr, String formatStr) {
        SimpleDateFormat sdf = new SimpleDateFormat(formatStr);
        Date date = null;
        try {
            date = sdf.parse(dateStr);
        } catch (ParseException ignore) {
        }
        return date;
    }

    private static Object select(String mock, Class<?> kls) {
        String[] values = mock.substring(0, mock.length() - 1).substring(1).split(",");
        String value = values[new Random().nextInt(values.length)];
        return convertSimple(value, kls);
    }

    private static Class<?> getThisClass(String className) {
        try {
            if (className.contains("<")) {
                return Class.forName(className.split("<")[0]);
            } else {
                return Class.forName(className);
            }
        } catch (ClassNotFoundException e) {
            return HashMap.class;
        }
    }

    private static Class getTemplateClass(String className) {
        try {
            if (className.contains("<")) {
                return Class.forName(className.split("<")[1].split(">")[0]);
            } else {
                return Class.forName(className);
            }
        } catch (ClassNotFoundException e) {
            return HashMap.class;
        }
    }

    /**
     * 将string转出field对应的值
     *
     * @param sourceValue string value
     * @param field       字段
     * @return 具体的值
     */
    public static Object convert(String sourceValue, Field field) {
        try {
            Class<?> kls = field.getType();
            if (BeanUtils.isSimpleProperty(kls) || kls == Object.class) {
                return convertSimple(sourceValue, kls);
            } else if (kls.isArray() || Collection.class.isAssignableFrom(kls)) {
                ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
                kls = (Class) parameterizedType.getActualTypeArguments()[0];
                Class<? extends Collection> collectionClass = (Class<? extends Collection>) parameterizedType.getRawType();

                return JsonMapper.buildNonNullMapper().fromJsonToCollection(sourceValue, collectionClass, kls);
            } else if (Properties.class.isAssignableFrom(kls) || File.class.isAssignableFrom(kls)) {
                Properties properties = new Properties();
                InputStream is = new BufferedInputStream(new ByteArrayInputStream(sourceValue.getBytes()));
                BufferedReader bf = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                properties.load(bf);
                return properties;
            } else {
                return JsonMapper.buildNonNullMapper().fromJson(sourceValue, kls);
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static Object convertSimple(String sourceValue, Class<?> kls) {
        SimpleTypeConverter typeConverter = new SimpleTypeConverter();
        typeConverter.setConversionService(new GenericConversionService());
        typeConverter.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"), false));
        return typeConverter.convertIfNecessary(sourceValue.trim(), kls);
    }

    public static void main(String[] args) {
//        int n = -10;
//        int m = 5;
//        for (int i = 0; i < 100; i++) {
//            System.out.println(new Random().nextInt(m - n) + n);
//        }

        System.out.println(1000L * 3600L * 24L * 30L * 2);
//        new Random().nextInt() % System.currentTimeMillis();
        System.out.println(new Date((System.currentTimeMillis() - 2592000000L) + new Random().nextInt() % 5184000000L));
        System.out.println(convertSimple("1.2", Double.class));
        System.out.println(select("{1,2,3,4,5}", Long.class));
        System.out.println(select("{2020-10-10 11:11:11,2020-01-01 12:12:12}", Date.class));
        System.out.println(select("{11.1,22.2,33.4}", Double.class));
        System.out.println(select("{aa,bb,cc}", String.class));
//        System.out.println(JsonMapper.buildNonNullMapper().toJson(mock("{\"notNull\":false,\"body\":false,\"type\":\"List\",\"children\":[{\"notNull\":false,\"body\":false,\"type\":\"Object\",\"children\":[{\"key\":\"id\",\"notNull\":false,\"body\":false,\"defaults\":10,\"comment\":\"规格键id\",\"type\":\"Long\"},{\"key\":\"name\",\"notNull\":false,\"body\":false,\"defaults\":\"规格键名称\",\"comment\":\"规格键名称\",\"type\":\"String\"}]}]}")));
//        System.out.println(JsonMapper.buildNonNullMapper().toJson(mock("[[1],2,3]")));
//        System.out.println(JsonMapper.buildNonNullMapper().toJson(mock("[[1],[2,3]]")));
//        System.out.println(JsonMapper.buildNonNullMapper().toJson(mock("[{\"a\":1}, [2,3]]")));
//        System.out.println(JsonMapper.buildNonNullMapper().toJson(mock("{\"a\":1,\"b\":[2],\"c\":3}")));
    }
}
