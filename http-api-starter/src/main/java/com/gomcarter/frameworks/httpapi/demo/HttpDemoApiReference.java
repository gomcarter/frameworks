package com.gomcarter.frameworks.httpapi.demo;

import com.gomcarter.frameworks.httpapi.annotation.HttpResource;

import java.util.Arrays;
import java.util.HashMap;

/**
 * @author gomcarter
 */
public class HttpDemoApiReference {

    @HttpResource
    HttpDemoApi demoApi;

    public void method() {

        demoApi.post(new DemoDto().setId(1L).setNickname("name"),
                2L,
                "headerValue",
                new HashMap<String, String>() {{
                    put("header1", "header1");
                }},
                Arrays.asList("1", "2", "3"),
                null,
                null
        );
    }
}
