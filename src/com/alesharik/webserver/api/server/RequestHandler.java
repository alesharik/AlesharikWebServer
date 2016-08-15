package com.alesharik.webserver.api.server;

import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

import java.io.IOException;

/**
 * This interface used for provide custom request handler
 */
public interface RequestHandler {

    /**
     * Return true if this handler can handle request
     *
     * @param request request form client
     */
    boolean canHandleRequest(Request request) throws IOException;

    void handleRequest(Request request, Response response) throws Exception;
}
