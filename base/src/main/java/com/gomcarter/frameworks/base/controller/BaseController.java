package com.gomcarter.frameworks.base.controller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gomcarter.frameworks.base.common.RequestUtils;
import com.gomcarter.frameworks.base.exception.CustomException;
import com.gomcarter.frameworks.base.json.*;
import com.gomcarter.frameworks.config.mapper.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.management.ReflectionException;
import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.Date;

/**
 * @author gomcarter  on 2019-11-11 23:17:48
 */
@ControllerAdvice
@Slf4j
public class BaseController implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> converterType) {
        return methodParameter.getMethodAnnotation(JsonIgnore.class) == null;
    }

    @Override
    public Object beforeBodyWrite(
            Object obj, MethodParameter methodParameter,
            MediaType mediaType, Class<? extends HttpMessageConverter<?>> converterType,
            ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {

        if (obj == null) {
            return new JsonSuccess();
        } else if (obj instanceof JsonObject) {
            return obj;
        } else if (obj instanceof ModelAndView) {
            return obj;
        } else {
            return new JsonData(obj);
        }
    }

    /**
     * @param request   HttpServletRequest
     * @param exception what exception happened in your code
     * @return result for our user as some friendly notice
     */
    @ExceptionHandler(Throwable.class)
    @ResponseBody
    public JsonObject exceptionHandler(HttpServletRequest request, Exception exception) {
        //如果是ClientAbortException, 直接返回null;
        if (StringUtils.contains(exception.getClass().toString(), "ClientAbortException")) {
            return null;
        }

        log.error("{}操作失败, url:{}, method:{}, ip: {}, Referer: {}, UA: {}, params: {}, cookie: {},header: {},",
                this.getClass().getName(),
                request.getRequestURI(),
                request.getMethod(),
                RequestUtils.getIp(request),
                request.getHeader("Referer"),
                request.getHeader("User-Agent"),
                JsonMapper.buildNonNullMapper().toJson(request.getParameterMap()),
                JsonMapper.buildNonNullMapper().toJson(request.getCookies()),
                JsonMapper.buildNonNullMapper().toJson(RequestUtils.headerMap(request)),
                exception);

        if (exception instanceof MissingServletRequestParameterException) {
            return new JsonError(ErrorCode.paramError);
        }

        if (exception instanceof NullPointerException) {
            return new JsonError(ErrorCode.nullPointer);
        }

        if (exception instanceof SQLException) {
            return new JsonError(ErrorCode.sqlError);
        }

        if (exception instanceof ReflectionException) {
            return new JsonError(ErrorCode.sqlError);
        }

        if (exception instanceof CustomException) {
            return new JsonError(exception.getMessage(), ((CustomException) exception).getCode());
        }

        JsonObject jsonError = addMoreExceptionHandler(request, exception);
        if (jsonError != null) {
            return jsonError;
        }

        return new JsonError("请求失败！");
    }

    protected JsonObject addMoreExceptionHandler(HttpServletRequest request, Exception exception) {
        return null;
    }

    /**
     * spring InitBinder
     *
     * @param binder binder
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // binder.registerCustomEditor(String.class, new StringEscapeEditor());
        binder.registerCustomEditor(Date.class, new DateEditor());
    }
}
