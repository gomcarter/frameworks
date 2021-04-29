package com.gomcarter.frameworks.base.aop;

import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author gomcarter
 */
public class CrossAccessFilter extends OncePerRequestFilter {

    private String accessControlAllowHeaders = "Origin, X-Requested-With, Content-Type, Accept";

    public CrossAccessFilter() {
    }

    protected void crossAllows(HttpServletRequest request, HttpServletResponse response) {
        String o = request.getHeader("Origin");
        response.setHeader("Access-Control-Allow-Origin", o);
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.addHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE");
        response.addHeader("Access-Control-Allow-Headers", this.accessControlAllowHeaders);
        response.addHeader("Access-Control-Max-Age", "172800");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        this.crossAllows(request, response);
        chain.doFilter(request, response);
    }

    public CrossAccessFilter setAccessControlAllowHeaders(String accessControlAllowHeaders) {
        this.accessControlAllowHeaders = "Origin, X-Requested-With, Content-Type, Accept," + accessControlAllowHeaders;
        return this;
    }
}
