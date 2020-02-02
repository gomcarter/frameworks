package com.gomcarter.frameworks.httpapi.utils;

import com.gomcarter.frameworks.base.common.CookieUtils;
import com.gomcarter.frameworks.base.common.CustomDateUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.crypto.hash.Md5Hash;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.Date;

/**
 * @author gomcarter on 2019-11-13 10:27:42
 */
public class ApiTokenUtils {
    private String publicKey;
    private String tokenName;

    public ApiTokenUtils(String publicKey, String tokenName) {
        this.publicKey = publicKey;
        this.tokenName = tokenName;
    }

    private String getKey(int offset) {
        String time = CustomDateUtils.toString(getEnCodeTime(offset));
        return getKey(time);
    }

    private String getKey(String time) {
        return publicKey + time + publicKey;
    }

    public String getToken() {
        return new Md5Hash(getKey(0)).toHex();
    }

    private String getToken(String time) {
        return new Md5Hash(getKey(time)).toHex();
    }

    private String getOffSetToken(int offset) {
        return new Md5Hash(getKey(offset)).toHex();
    }

    private Date getEnCodeTime(int offset) {
        Date current = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(current);
        calendar.set(Calendar.HOUR, calendar.get(Calendar.HOUR) + offset);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public boolean validate(HttpServletRequest request) {
        return validate(CookieUtils.getByHeaderOrCookies(request, this.tokenName));
    }

    public boolean validate(String token) {
        return StringUtils.equals(token, getToken()) ||
                StringUtils.equals(token, getOffSetToken(1)) ||
                StringUtils.equals(token, getOffSetToken(-1));
    }

    public static void main(String[] args) {
        System.out.println(new ApiTokenUtils("the key", "backToken").getToken());
    }
}
