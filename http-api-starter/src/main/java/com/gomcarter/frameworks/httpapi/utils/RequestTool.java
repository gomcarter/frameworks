package com.gomcarter.frameworks.httpapi.utils;

import com.gomcarter.frameworks.httpapi.message.request.RequestMessage;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

public class RequestTool {

    public static void addAllParams(RequestMessage message, Map<String, Object> params) {
        for (String key : params.keySet()) {
            addParameter(message, key, params.get(key));
        }
    }

    public static void addParameter(RequestMessage message, String key, Object value) {
        if (value == null) {
            //没有就不添加参数
            return;
        }

        if (value instanceof Collection) {
            //如果是集合,只支持一遍
            for (Object tmp : (Collection<?>) value) {
                if (tmp != null) {
                    message.addParameter(key, objectToStr(tmp));
                }
            }
        } else {
            message.addParameter(key, objectToStr(value));
        }

    }

    private static String objectToStr(Object value) {
        if (value == null) {
            return StringUtils.EMPTY;
        }
        if (value instanceof Date) {
            //如果是日期 转变一次
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return (sdf.format(value));
        }
        return value.toString();
    }
}
