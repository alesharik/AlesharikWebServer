package com.alesharik.webserver.server.api;

import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

import javax.annotation.Nonnull;
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
    boolean canHandleRequest(@Nonnull Request request) throws IOException;

    /**
     * Handle request
     *
     * @param request  http server request
     * @param response http server response
     * @throws Exception if anything happens
     */
    void handleRequest(@Nonnull Request request, @Nonnull Response response) throws Exception;
}
