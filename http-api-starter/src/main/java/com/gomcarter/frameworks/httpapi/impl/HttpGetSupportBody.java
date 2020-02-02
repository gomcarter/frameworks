package com.gomcarter.frameworks.httpapi.impl;

import com.gomcarter.frameworks.httpapi.annotation.Method;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import java.net.URI;

/**
 * @author gomcarter
 */
public class HttpGetSupportBody extends HttpEntityEnclosingRequestBase {
    @Override
    public String getMethod() {
        return Method.GET.name();
    }

    public HttpGetSupportBody(final String uri) {
        super();
        setURI(URI.create(uri));
    }

    public HttpGetSupportBody(final URI uri) {
        super();
        setURI(uri);
    }

    public HttpGetSupportBody() {
        super();
    }
}
