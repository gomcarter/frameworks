package com.gomcarter.frameworks.interfaces.utils;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gomcarter.frameworks.interfaces.annotation.Ignore;
import com.gomcarter.frameworks.interfaces.annotation.Notes;
import com.gomcarter.frameworks.interfaces.dto.ApiBean;
import com.gomcarter.frameworks.interfaces.dto.ApiInterface;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;
import sun.reflect.generics.reflectiveObjects.TypeVariableImpl;

import javax.validation.constraints.NotNull;
import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author gomcarter on 2019-12-02 09:23:09
 */
@Order
public class InterfacesRegister implements ApplicationContextAware {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public InterfacesRegister() {
        // 判断是否使用 swagger
        try {
            Class.forName("io.swagger.annotations.Api");
        } catch (Exception e) {
            swagger = false;
        }

        try {
            Class.forName("javax.validation.constraints.NotNull");
        } catch (Exception e) {
            javax = false;
        }
    }

    /**
     * the cache for fields default value
     */
    private WeakHashMap<Class, Object> instanceMap = new WeakHashMap<>();
    /**
     * the class counter for recursion depth
     */
    private WeakHashMap<String, Integer> keyClassMap = new WeakHashMap<>();

    private final static int RECURSION_DEPTH = 0;

    /**
     * 是否使用了 swagger
     */
    private boolean swagger = true;
    private boolean javax = true;

    /**
     * <b style="color:green">comment：</b>
     * <p>
     * 1、{@link org.springframework.web.bind.annotation.RequestMapping}，such as：
     * <blockquote>
     * {@code @GetMapping(value = "list",name = "name of this interface")}
     * </blockquote>
     * <blockquote>
     * {@code @RequestMapping(value = "list",name = "name of this interface")}
     * </blockquote>
     * <blockquote style="color:red">
     * {@code mark： if the name of RequestMapping is blank，then skip it —— which means the interface has no name}
     * </blockquote>
     * <p>
     * 2、some comment for this interface{@link Notes}，如：
     * </p>
     * <blockquote><pre>
     * {@code @Notes("this interface for user login")} <br>
     * {@code public void login() {...} }
     * </pre></blockquote>
     * 或
     * <blockquote><pre>
     *     public class Params {
     *         {@code @Notes("property comment")}
     *          private long id;
     *     }
     * </pre></blockquote>
     * 或
     * <blockquote><pre>
     *     {@code public void login(@Notes("parameter comment") String cellphone) {...} }
     * </pre></blockquote>
     * <p>
     * 3、mar this parameter whether required or not，mark on it as {@link org.springframework.web.bind.annotation.RequestParam}, such as：
     * <blockquote>
     * {@code public void login(@RequestParam String cellphone) {...} }
     * </blockquote>
     * mark on the value{@link Notes}，such as：
     * <blockquote><pre>
     *     public class Params {
     *         {@code @Notes(value="the id", notNull=true)}
     *          private long id;
     *     }
     * </pre></blockquote>
     * <blockquote>
     * {@code public class Params }
     * </blockquote>
     *
     * <blockquote>
     * {@code }
     * </blockquote>
     *
     * @return list of ApiInterface
     */
    @Notes("interface of register the interfaces to the interfaces center")
    public static List<ApiInterface> register() {
        return new InterfacesRegister().register0();
    }

    private List<ApiInterface> register0() {
        RequestMappingHandlerMapping bean = getBean(RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = bean.getHandlerMethods();

        List<ApiInterface> interfacesList = new ArrayList<>(handlerMethods.size());
        for (Map.Entry<RequestMappingInfo, HandlerMethod> kvEntry : handlerMethods.entrySet()) {
            RequestMappingInfo rmi = kvEntry.getKey();
            HandlerMethod handlerMethod = kvEntry.getValue();

            try {
                // get the base info
                ApiInterface interfaces = generateBase(handlerMethod.getMethod(), handlerMethod.getBeanType(), rmi);
                if (interfaces == null) {
                    continue;
                }

                // get the returns
                keyClassMap.clear();
                ApiBean returns = generateReturns(handlerMethod.getMethod(), handlerMethod.getBeanType());
                interfaces.setReturns(returns);

                // get the parameters
                List<ApiBean> parameters = generateParameters(handlerMethod.getMethod(), handlerMethod.getMethodParameters());
                interfaces.setParameters(parameters);

                // add to api list
                interfacesList.add(interfaces);
            } catch (Exception e) {
                // if failed， skip it
                logger.error("generate failed and skipped : {}",
                        handlerMethod.getMethod().getDeclaringClass().getName() + "." + handlerMethod.getMethod().getName(), e);
            }
        }

        // finally clear the cache
        instanceMap.clear();
        keyClassMap.clear();

        return interfacesList;
    }

    private ApiInterface generateBase(Method method, Class<?> controllerClass, RequestMappingInfo rmi) throws Exception {
        String url = StringUtils.join(rmi.getPatternsCondition().getPatterns(), ",");
        String httpMethod = StringUtils.join(rmi.getMethodsCondition().getMethods(), ",");

        return generateBase1(method, controllerClass, rmi.getName(), url, httpMethod);
    }

    private ApiInterface generateBase1(Method method, Class<?> controllerClass, String interfaceName, String url, String httpMethod) {
        String mark = Optional.ofNullable(method.getAnnotation(Notes.class)).map(Notes::value).orElse(null);

        if (swagger) {
            ApiOperation api = method.getAnnotation(ApiOperation.class);
            if (api != null) {
                interfaceName = StringUtils.isBlank(interfaceName) ? api.value() : interfaceName;
                mark = StringUtils.isBlank(mark) ? api.notes() : mark;
            }
        }

        // if the name of RequestMapping is blank，then skip it —— which means the interface has no name.
        if (StringUtils.isBlank(interfaceName)) {
            return null;
        }

        // 接口是否已废弃
        boolean deprecated = controllerClass.getAnnotation(Deprecated.class) != null || method.getAnnotation(Deprecated.class) != null;

        return new ApiInterface()
                .setMethod(httpMethod)
                .setUrl(url)
                .setController(method.getDeclaringClass().getSimpleName())
                .setDeprecated(deprecated)
                .setName(interfaceName)
                .setMark(mark);
    }

    public List<ApiBean> generateParameters(Method method, MethodParameter[] methodParameters) {
        List<ApiBean> parameters = new ArrayList<>();

        String[] parameterNames = Optional.ofNullable(new DefaultParameterNameDiscoverer().getParameterNames(method)).orElse(new String[methodParameters.length]);

        for (int i = 0; i < methodParameters.length; ++i) {
            // 如果是request或者response，或者标记@Ignore就忽略此参数
            MethodParameter mp = methodParameters[i];
            DataType t = DataType.get(mp.getParameterType());
            if (t == DataType.request || t == DataType.response || mp.hasParameterAnnotation(Ignore.class)) {
                continue;
            }

            Notes notes = mp.getParameterAnnotation(Notes.class);
            RequestParam requestParam = mp.getParameterAnnotation(RequestParam.class);
            RequestBody requestBody = mp.getParameterAnnotation(RequestBody.class);
            // the logic of parameter is required or not
            boolean notNull = mp.getParameterAnnotation(PathVariable.class) != null
                    || Optional.ofNullable(requestParam).map(RequestParam::required).orElse(false)
                    || Optional.ofNullable(requestBody).map(RequestBody::required).orElse(false)
                    || Optional.ofNullable(notes).map(Notes::notNull).orElse(false);
            if (javax) {
                notNull = notNull || mp.hasParameterAnnotation(NotNull.class);
            }

            String comment = Optional.ofNullable(notes).map(Notes::value).orElse(null);

            String defaultValue = Optional.ofNullable(requestParam)
                    .map(RequestParam::defaultValue)
                    .filter(s -> !"\n\t\t\n\t\t\n\ue000\ue001\ue002\n\t\t\t\t\n".equals(s))
                    .orElse(null);

            String parameterName = Optional.ofNullable(requestParam).map(RequestParam::value).orElse(null);
            parameterName = StringUtils.isBlank(parameterName) ? parameterNames[i] : parameterName;

            if (swagger) {
                if (mp.hasParameterAnnotation(ApiParam.class)) {
                    ApiParam apiParam = mp.getParameterAnnotation(ApiParam.class);
                    notNull = notNull || apiParam.required();
                    defaultValue = ObjectUtils.defaultIfNull(defaultValue, apiParam.defaultValue());
                    comment = StringUtils.isBlank(comment) ? apiParam.value() : comment;
                } else if (mp.hasParameterAnnotation(ApiModelProperty.class)) {
                    ApiModelProperty model = mp.getParameterAnnotation(ApiModelProperty.class);
                    notNull = notNull || model.required();
                    comment = StringUtils.isBlank(comment) ? model.value() : comment;
                }
            }

            ApiBean parameter = new ApiBean()
                    .setComment(comment)
                    .setNotNull(notNull)
                    .setKey(parameterName)
                    .setBody(requestBody != null)
                    .setDefaults(defaultValue);

            generateChildrenBean(parameter, parameterName, mp.getGenericParameterType(), null);

            parameters.add(parameter);

            keyClassMap.clear();
        }

        return parameters;
    }

    public ApiBean generateReturns(Method method, Class<?> controllerClass) throws NoSuchMethodException {
        ApiBean returns = new ApiBean();
        Type genericReturnType = controllerClass.getDeclaredMethod(method.getName(), method.getParameterTypes()).getGenericReturnType();
        this.generateChildrenBean(returns, null, genericReturnType, null);
        return returns;
    }

    private void generateChildrenBean(ApiBean parent, String key, Type parentType, Type actualType) {
        if (terminal(key, parentType)) {
            return;
        }

        if (parentType instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) parentType).getActualTypeArguments();
            if (actualTypeArguments.length > 1) {
                throw new RuntimeException("simple POJO or Iterable only for parameters and returns");
            }

            Class thisClass = ((ParameterizedTypeImpl) parentType).getRawType();
            DataType dataType = DataType.get(thisClass);

            // Collection类单独处理
            if (dataType == DataType.collection) {
                ApiBean child = new ApiBean();
                parent.setType(List.class.getSimpleName())
                        .addChild(child);

                Type cache = actualTypeArguments[0];
                if (cache.getTypeName().contains(".")) {
                    // 包含点，证明是一个类
                    actualType = actualTypeArguments[0];
                } else {
                    // 是个模板
                    actualType = actualType == null ? actualTypeArguments[0] : actualType;
                }
                generateChildrenBean(child, key, actualType, null);
            } else {
                generateChildrenBean(parent, key, thisClass, actualTypeArguments[0]);
            }

        } else if (parentType instanceof TypeVariableImpl) {
            parent.setType(Object.class.getSimpleName());
        } else {
            Class parentKls = (Class) parentType;

            if (parentKls.isArray()) {
                throw new RuntimeException("simple POJO or Iterable only for parameters and returns, you have to make it as: List，Set or Collection");
            }

            DataType dataType = DataType.get(parentKls);
            if (dataType == DataType.file) {
                parent.setType(File.class.getSimpleName());
            } else if (dataType == DataType.object) {
                generatePOJO(parent, key, parentKls, actualType);
            } else if (dataType == DataType.simple) {
                parent.setType(parentKls.getSimpleName());
            }
        }
    }

    private void generatePOJO(ApiBean parent, String key, Class parentKls, Type actualType) {
        Object instance = this.newInstance(parentKls);

        List<ApiBean> children = Arrays.stream(this.getFields(parentKls))
                .filter(field -> !field.getName().contains("this$"))
                .filter(field -> (field.getModifiers() & Modifier.STATIC) != Modifier.STATIC)
                .map(field -> {
                    // 标记ignore或者JsonIgnore将不会读取到接口中心
                    if (field.getAnnotation(Ignore.class) != null || field.getAnnotation(JsonIgnore.class) != null) {
                        return null;
                    }

                    Notes notes = field.getAnnotation(Notes.class);
                    Object defaults = getFieldValue(instance, field);
                    boolean notNull = Optional.ofNullable(notes).map(Notes::notNull).orElse(false);
                    String comment = Optional.ofNullable(notes).map(Notes::value).orElse(null);

                    String mock = Optional.ofNullable(notes).map(Notes::mock).orElse(null);
                    String fieldName = field.getName();

                    if (swagger) {
                        if (field.isAnnotationPresent(ApiModelProperty.class)) {
                            ApiModelProperty model = field.getAnnotation(ApiModelProperty.class);
                            notNull = notNull || model.required();
                            comment = StringUtils.isBlank(comment) ? model.value() : comment;
                            mock = StringUtils.isBlank(mock) ? model.example() : mock;
                        } else if (field.isAnnotationPresent(ApiParam.class)) {
                            ApiParam apiParam = field.getAnnotation(ApiParam.class);
                            notNull = notNull || apiParam.required();
                            defaults = ObjectUtils.defaultIfNull(defaults, convert(apiParam.defaultValue(), field));
                            comment = StringUtils.isBlank(comment) ? apiParam.value() : comment;
                            mock = StringUtils.isBlank(mock) ? apiParam.example() : mock;
                        }
                    }

                    ApiBean child = new ApiBean()
                            .setKey(fieldName)
                            .setNotNull(notNull)
                            .setComment(comment)
                            .setDefaults(defaults)
                            .setMock(StringUtils.isNotBlank(mock) ? convert(mock, field) : defaults);

                    generateChildrenBean(child, StringUtils.defaultString(key, field.getName()), field.getGenericType(), actualType);
                    return child;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        parent.setChildren(children)
                .setType(Object.class.getSimpleName());
    }

    /**
     * 将string转出field对应的值
     *
     * @param sourceValue string value
     * @param field       字段
     * @return 具体的值
     */
    private Object convert(String sourceValue, Field field) {
        try {
            Class<?> kls = field.getType();
            if (BeanUtils.isSimpleProperty(kls) || kls == Object.class) {
                SimpleTypeConverter typeConverter = new SimpleTypeConverter();
                typeConverter.setConversionService(new GenericConversionService());
                typeConverter.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"), false));
                return typeConverter.convertIfNecessary(sourceValue, kls);
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

    private boolean terminal(String key, Type type) {
        if (type instanceof ParameterizedType
                || type instanceof TypeVariableImpl
                || DataType.get((Class) type) != DataType.object) {
            return false;
        }

        String k = key + "_" + type.getTypeName();
        Integer times = keyClassMap.getOrDefault(k, 0);
        if (times > RECURSION_DEPTH) {
            return true;
        }
        keyClassMap.put(k, ++times);
        return false;
    }

    private Field[] getFields(Class kls) {
        Field[] fields = kls.getDeclaredFields();
        Class superKls = kls;
        while ((superKls = superKls.getSuperclass()) != null) {
            Field[] superFields = superKls.getDeclaredFields();
            int newLength = superFields.length;
            if (newLength > 0) {
                Field[] original = fields;
                fields = new Field[fields.length + newLength];

                System.arraycopy(superFields, 0, fields, 0, newLength);
                System.arraycopy(original, 0, fields, newLength, original.length);
            }
        }
        return fields;
    }

    private Object newInstance(Class kls) {
        try {
            Object instance = instanceMap.get(kls);
            if (instance == null) {
                instanceMap.put(kls, kls.newInstance());
            }
            return instanceMap.get(kls);
        } catch (Exception e) {
            return null;
        }
    }

    private static Object getFieldValue(Object instance, Field field) {
        if (instance != null) {
            try {
                field.setAccessible(true);
                return field.get(instance);
            } catch (Exception ignore) {
            }
        }
        return null;
    }

    private static ApplicationContext applicationContext;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        applicationContext = ac;

        List<ApiInterface> interfaces = InterfacesRegister.register();
        // sync interfaces
        InterfacesSynchronizer.sync(interfaces);
    }

    public static <T> T getBean(Class<T> kls) {
        return applicationContext.getBean(kls);
    }

    public static void registerFrom(Class controllerClass) {
        Method[] methods = controllerClass.getDeclaredMethods();
        for (Method m : methods) {
            registerFrom(m);
        }
    }

    public static void registerFrom(Method method) {
        try {
            InterfacesRegister register = new InterfacesRegister();
            RequestMapping rm = method.getAnnotation(RequestMapping.class);
            GetMapping gm = method.getAnnotation(GetMapping.class);
            PostMapping pm = method.getAnnotation(PostMapping.class);
            PutMapping ppm = method.getAnnotation(PutMapping.class);
            PatchMapping ptm = method.getAnnotation(PatchMapping.class);
            DeleteMapping dm = method.getAnnotation(DeleteMapping.class);

            if (rm == null && gm == null && pm == null && ppm == null && ptm == null && dm == null) {
                return;
            }

            RequestMethod[] httpMethod;
            String[] murls;
            String interfaceName;
            if (rm != null) {
                httpMethod = rm.method();
                murls = rm.value();
                interfaceName = rm.name();
            } else if (gm != null) {
                httpMethod = new RequestMethod[]{RequestMethod.GET};
                murls = gm.value();
                interfaceName = gm.name();
            } else if (pm != null) {
                httpMethod = new RequestMethod[]{RequestMethod.POST};
                murls = pm.value();
                interfaceName = pm.name();
            } else if (ppm != null) {
                httpMethod = new RequestMethod[]{RequestMethod.PUT};
                murls = ppm.value();
                interfaceName = ppm.name();
            } else if (ptm != null) {
                httpMethod = new RequestMethod[]{RequestMethod.PATCH};
                murls = ptm.value();
                interfaceName = ptm.name();
            } else {
                httpMethod = new RequestMethod[]{RequestMethod.DELETE};
                murls = dm.value();
                interfaceName = dm.name();
            }


            Class controllerClass = method.getDeclaringClass();
            RequestMapping crm = (RequestMapping) controllerClass.getDeclaredAnnotation(RequestMapping.class);
            String[] burls = crm.value();
            if (burls.length == 0) {
                burls = new String[]{""};
            }

            if (murls.length == 0) {
                murls = new String[]{""};
            }

            String[] urls = new String[burls.length * murls.length];
            int n = 0;
            for (int k = 0; k < burls.length; k++) {
                for (int j = 0; j < murls.length; ++j) {
                    urls[n] = (burls[k] + "/" + murls[j]).replaceAll("//", "/");
                }
            }

            // get the base info
            ApiInterface interfaces = register.generateBase1(method, controllerClass, interfaceName,
                    StringUtils.join(urls, ","),
                    StringUtils.join(httpMethod, ","));

            if (interfaces == null) {
                System.out.println(method.getName() + "未填写接口名称");
                return;
            }

            // get the returns
            register.keyClassMap.clear();

            ApiBean returns = register.generateReturns(method, method.getDeclaringClass());
            interfaces.setReturns(returns);

            Parameter[] mp = method.getParameters();
            MethodParameter[] mpr = new MethodParameter[mp.length];
            for (int i = 0; i < mp.length; ++i) {
                mpr[i] = new MethodParameter(method, i);
            }

            // get the parameters
            List<ApiBean> parameters = register.generateParameters(method, mpr);
            interfaces.setParameters(parameters);

            // add to api list
            InterfacesSynchronizer.sync(Collections.singletonList(interfaces));
        } catch (Exception e) {
            // if failed， skip it
            e.printStackTrace();
        }
    }

}
