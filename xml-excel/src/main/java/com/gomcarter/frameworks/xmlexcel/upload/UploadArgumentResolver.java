package com.gomcarter.frameworks.xmlexcel.upload;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.event.SyncReadListener;
import com.alibaba.excel.exception.ExcelAnalysisException;
import com.gomcarter.frameworks.base.exception.CustomException;
import com.gomcarter.frameworks.xmlexcel.upload.annotation.RequestFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author gaopeng 2021/2/18
 */
@Slf4j
public class UploadArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(RequestFile.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        Assert.state(servletRequest != null, "No HttpServletRequest");

        RequestFile requestFile = parameter.getParameterAnnotation(RequestFile.class);
        Assert.state(requestFile != null, "No RequestFile");

        MultipartHttpServletRequest multipartRequest =
                WebUtils.getNativeRequest(servletRequest, MultipartHttpServletRequest.class);
        boolean isMultipart = (multipartRequest != null || isMultipartContent(servletRequest));

        if (!isMultipart) {
            throw new RuntimeException("该请求不是multipart请求，不能使用RequestFile");
        }

        if (multipartRequest == null) {
            multipartRequest = new StandardMultipartHttpServletRequest(servletRequest);
        }

        String name = getPartName(parameter, requestFile);

        MultipartFile file = multipartRequest.getFile(name);
        if (file == null && requestFile.required()) {
            throw new MissingServletRequestPartException(name);
        }

        if (file == null) {
            return null;
        }

        Class<?> dataClass = requestFile.dataClass();
        if (dataClass.equals(Object.class)) {
            Type dataType = parameter.getGenericParameterType();
            if (!(dataType instanceof ParameterizedType)) {
                throw new IllegalArgumentException("@RequestFile注解标注的类型不正确");
            }

            dataClass = (Class<?>) ((ParameterizedType) dataType).getActualTypeArguments()[0];
        }

        return readData(file, dataClass);
    }

    private List<?> readData(MultipartFile file, Class<?> dataClass) {
        try {
            SyncReadListener readListener = new CustomSyncReadListener();
            EasyExcel.read(file.getInputStream(), dataClass, readListener).sheet().doRead();

            return readListener.getList();
        } catch (ExcelAnalysisException eae) {
            throw new CustomException(eae.getMessage());
        } catch (Exception e) {
            log.warn("上传数据失败", e);

            throw new RuntimeException("上传数据失败!");
        }
    }

    private static boolean isMultipartContent(HttpServletRequest request) {
        String contentType = request.getContentType();
        return (contentType != null && contentType.toLowerCase().startsWith("multipart/"));
    }

    private String getPartName(MethodParameter methodParam, RequestFile requestFile) {
        String fileName = requestFile.name();
        if (fileName.isEmpty()) {
            fileName = methodParam.getParameterName();
            if (fileName == null) {
                throw new IllegalArgumentException("Request File name for argument type [" +
                        methodParam.getNestedParameterType().getName() +
                        "] not specified, and parameter name information not found in class file either.");
            }
        }
        return fileName;
    }
}
