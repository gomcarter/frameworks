package com.gomcarter.frameworks.interfaces.controller;

import com.gomcarter.frameworks.interfaces.utils.InterfacesRegister;
import com.gomcarter.frameworks.interfaces.utils.MockUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 模拟接口地址
 *
 * @author gomcarter
 */
@RestController
@RequestMapping("_mock")
public class MockController {

    @RequestMapping
    Object mock(HttpServletRequest request, @RequestParam String url) throws Exception {
        RequestMappingHandlerMapping bean = InterfacesRegister.getBean(RequestMappingHandlerMapping.class);

        HandlerMethod handlerMethod = bean.getHandlerMethods().entrySet()
                .stream()
                .filter(s -> s.getKey().getPatternsCondition().getPatterns().contains(url) &&
                        s.getKey().getMethodsCondition().getMethods().contains(RequestMethod.valueOf(request.getMethod())))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(null);

        return handlerMethod == null ? null : MockUtils.mock(new InterfacesRegister().generateReturns(handlerMethod.getMethod(), handlerMethod.getBeanType()));
    }
}
