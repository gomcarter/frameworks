package com.gomcarter.frameworks.base.common;

import com.gomcarter.frameworks.base.streaming.Streamable;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;


/**
 * @author gomcarter
 */
public class RequestUtils {

    /**
     * 获取 cookie to map
     *
     * @param request HttpServletRequest
     * @return map
     */
    public static Map<String, String> getCookieMap(HttpServletRequest request) {
        if (request == null || request.getCookies() == null) {
            return new HashMap<>(0);
        }

        return Streamable.valueOf(request.getCookies())
                .uniqueGroupbySafely(Cookie::getName, Cookie::getValue)
                .collect();
    }

    /**
     * 通过 key 获取 cookie
     *
     * @param request HttpServletRequest
     * @param key     cookie key
     * @return Cookie
     */
    public static Cookie getCookie(HttpServletRequest request, String key) {
        Cookie[] cookies = request.getCookies();
        Cookie c = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (StringUtils.equals(key, cookie.getName())) {
                    c = cookie;
                    break;
                }
            }
        }
        return c;
    }

    /**
     * 通过 key 获取 cookie value
     *
     * @param cookies Cookie array
     * @param key     cookie key
     * @return Cookie value
     */
    public static String getCookieValue(Cookie[] cookies, String key) {
        if (cookies == null) {
            return null;
        }
        String result = null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(key)) {
                result = cookie.getValue();
                break;
            }
        }
        return result;
    }

    /**
     * 通过 key 获取 cookie value
     *
     * @param request HttpServletRequest
     * @param key     cookie key
     * @return Cookie value
     */
    public static String getCookieValue(HttpServletRequest request, String key) {
        return getCookieValue(request.getCookies(), key);
    }

    /**
     * set cookie to HttpServletResponse
     *
     * @param response HttpServletResponse
     * @param key      cookie key
     * @param value    key
     * @param time     expire time (unit: ms)
     */
    public static void addCookies(HttpServletResponse response, String key, String value, Integer time) {
        addCookies(response, key, value, null, time);
    }

    /**
     * set cookie to HttpServletResponse
     *
     * @param response HttpServletResponse
     * @param key      cookie key
     * @param value    key
     * @param domain   domain
     * @param time     expire time (unit: ms)
     */
    public static void addCookies(HttpServletResponse response, String key, String value, String domain, Integer time) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(time);
        cookie.setPath("/");
        if (domain != null) {
            cookie.setDomain(domain);
        }
        response.addCookie(cookie);
    }

    /**
     * remove a cookie from HttpServletResponse
     *
     * @param response HttpServletResponse
     * @param key      cookie key
     */
    public static void removeCookies(HttpServletResponse response, String key) {
        Cookie cookie = new Cookie(key, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    /**
     * get a value from HttpServletRequest, it first should be a simple header,
     * otherwise  is a cookie
     *
     * @param request HttpServletRequest
     * @param key     cookie key
     * @return the cookie or header value
     */
    public static String getByHeaderOrCookies(HttpServletRequest request, String key) {
        // 优先从http头中获取取得
        String value = request.getHeader(key);
        // 参数没有时从cookie取得
        if (StringUtils.isBlank(value)) {
            Cookie[] cookies = request.getCookies();
            if (!ArrayUtils.isEmpty(cookies)) {
                value = getCookieValue(cookies, key);
            }
        }
        return value;
    }

    public static Map<String, String> headerMap(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        Map<String, String> headerMap = new HashMap<>();
        while (headerNames.hasMoreElements()) {
            String header = headerNames.nextElement();
            headerMap.put(header, request.getHeader(header));
        }
        return headerMap;
    }

    /**
     * get visitor's ip
     *
     * @param request HttpServletRequest
     * @return ip such as "119.22.11.2"
     */
    public static String getIp(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        if (request.getHeader("X-Forwarded-For") != null) {
            ip = request.getHeader("X-Forwarded-For");
        } else if (request.getHeader("X-Real-IP") != null) {
            ip = request.getHeader("X-Real-IP");
        }
        return ip;
    }

    /**
     * read request body
     *
     * @param request HttpServletRequest
     * @return body content
     * @throws IOException for read failed
     */
    public static String body(HttpServletRequest request) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder buffer = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            buffer.append(line);
        }
        return buffer.toString();
    }
}
