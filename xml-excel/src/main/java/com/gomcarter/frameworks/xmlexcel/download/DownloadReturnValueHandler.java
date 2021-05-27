package com.gomcarter.frameworks.xmlexcel.download;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.gomcarter.frameworks.base.json.JsonData;
import com.gomcarter.frameworks.base.json.JsonObject;
import com.gomcarter.frameworks.base.json.JsonSuccess;
import com.gomcarter.frameworks.config.mapper.JsonMapper;
import com.gomcarter.frameworks.xmlexcel.download.annotation.ResponseFile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.ParameterizedType;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author gaopeng 2021/2/18
 */
@Slf4j
public class DownloadReturnValueHandler implements HandlerMethodReturnValueHandler {

    private static final String DATA_SHEET_NAME = "结果";

    private String appName;

    public DownloadReturnValueHandler(ConfigurableApplicationContext applicationContext) {
        try {
            // this.redisTool = applicationContext.getBean("redisTool", RedisTool.class);
            ConfigurableEnvironment environment = applicationContext.getEnvironment();
            this.appName = environment.getProperty("download.dir",
                    environment.getProperty("app.id", "unknown"));
            // this.downloader = new Downloader();
            // this.downloader.setRedisTool(redisTool);
            // this.downloader.setAppName(appName);
        } catch (Exception e) {
            log.warn("初始化异步下载失败，如果需要使用请按照错误日志正确配置", e);
        }
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return (AnnotatedElementUtils.hasAnnotation(returnType.getContainingClass(), ResponseFile.class) ||
                returnType.hasMethodAnnotation(ResponseFile.class));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
        Assert.state(response != null, "No HttpServletResponse");

        if (returnValue == null) {
            sendTextResult(response, new JsonSuccess());

            return;
        }

        ResponseFile responseFile = getResponseFile(returnType);

        boolean async = responseFile.async();

        if (async) {
            throw new RuntimeException("使用异步下载，但是downloader初始化失败，请检查日志");
        }

        if (async) {
            // 暂时不支持异步
//            String key = webRequest.getParameter("key");
//            if (StringUtils.isNotBlank(key)) {
//                sendTextResult(response, new JsonData(downloader.check(key)));
//                return;
//            }
        }

        DownloadCallback<?> callback;
        Class<?> dataClass;
        if (returnValue instanceof DownloadCallback<?>) {
            dataClass = ResolvableType.forType(
                    ((ParameterizedType) returnType.getGenericParameterType()).getActualTypeArguments()[0])
                    .resolve();

            callback = (DownloadCallback<?>) returnValue;
        } else if (returnValue instanceof Collection) {
            if (async) {
                throw new IllegalStateException("异步下载必须返回DownloadCallback");
            }

            dataClass = ResolvableType.forType(
                    ((ParameterizedType) returnType.getGenericParameterType()).getActualTypeArguments()[0])
                    .resolve();

            callback = context -> {
                context.setFinished();
                return (List<Object>) returnValue;
            };
        } else {
            throw new RuntimeException("不能处理该返回结果" + returnType);
        }

        String filename = responseFile.name();
        String uuid = UUID.randomUUID().toString();
        if (StringUtils.isBlank(filename)) {
            filename = uuid;
        } else {
            filename = filename + "-" + uuid;
        }
        filename = filename + ExcelTypeEnum.XLSX.getValue();

        if (async) {
            // 赞不支持异步
//            sendTextResult(response, new JsonData(downloader.generate(filename, filename, outputStream -> {
//                writeExcel(dataClass, callback, outputStream);
//            })));
        } else {
            response.setContentType(new MediaType("application", "vnd.ms-excel", StandardCharsets.UTF_8).toString());
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.builder("attachment")
                    .filename(filename, StandardCharsets.UTF_8)
                    .build().toString());

            writeExcel(dataClass, callback, response.getOutputStream());
        }
    }

    private void sendTextResult(HttpServletResponse response, JsonObject data) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        StreamUtils.copy(JsonMapper.buildNonNullMapper().toJson(data), StandardCharsets.UTF_8, response.getOutputStream());
    }

    private ResponseFile getResponseFile(MethodParameter returnType) {
        ResponseFile responseFile = returnType.getMethodAnnotation(ResponseFile.class);
        if (responseFile == null) {
            responseFile = AnnotatedElementUtils.getMergedAnnotation(returnType.getContainingClass(), ResponseFile.class);
        }
        if (responseFile == null) {
            throw new IllegalStateException("未找到ResponseFile注解" + returnType);
        }
        return responseFile;
    }

    private void writeExcel(Class<?> dataClass, DownloadCallback<?> callback, OutputStream outputStream) {
        ExcelWriter excelWriter = EasyExcel.write(new BufferedOutputStream(outputStream), dataClass).build();
        WriteSheet writeSheet = EasyExcel.writerSheet(DATA_SHEET_NAME).build();

        try {
            List<?> dataList;
            DownloadContext context = new DownloadContext();

            do {
                dataList = callback.execute(context);

                excelWriter.write(dataList, writeSheet);

                if (CollectionUtils.isEmpty(dataList)) {
                    break;
                }
            } while (!context.isFinished());
        } catch (Exception e) {
            log.error("写excel失败", e);
            // 失败写入空数据，要不然excel格式可能有问题
            excelWriter.write(Collections.emptyList(), writeSheet);
        } finally {
            excelWriter.finish();
        }
    }
}
