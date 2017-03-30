package com.alesharik.webserver.server.api;

import com.alesharik.webserver.configuration.Module;

public abstract class HttpHandler extends org.glassfish.grizzly.http.server.HttpHandler implements Module {
    public HttpHandler() {

    }

    public abstract String getHandlerName();
}
