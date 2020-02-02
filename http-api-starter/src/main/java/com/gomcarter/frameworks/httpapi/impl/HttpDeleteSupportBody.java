package com.gomcarter.frameworks.httpapi.impl;

import com.gomcarter.frameworks.httpapi.annotation.Method;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import java.net.URI;

/**
 * @author gomcarter
 */
public class HttpDeleteSupportBody extends HttpEntityEnclosingRequestBase {
    @Override
    public String getMethod() {
        return Method.DELETE.name();
    }

    public HttpDeleteSupportBody(final String uri) {
        super();
        setURI(URI.create(uri));
    }

    public HttpDeleteSupportBody(final URI uri) {
        super();
        setURI(uri);
    }

    public HttpDeleteSupportBody() {
        super();
    }
}
