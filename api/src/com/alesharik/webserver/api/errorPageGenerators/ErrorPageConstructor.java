package com.alesharik.webserver.api.errorPageGenerators;


import org.glassfish.grizzly.http.server.Request;

import javax.annotation.concurrent.ThreadSafe;

/**
 * This interface used for generate error pages for one status
 */
@ThreadSafe
public interface ErrorPageConstructor {
    /**
     * Generate error page with description and without exception
     *
     * @param request      the request
     * @param status       status code
     * @param reasonPhrase reason phrase
     * @param description  description
     * @param throwable    throwable
     * @return HTML code of error page
     */
    String generate(Request request, int status, String reasonPhrase, String description, Throwable throwable);

    /**
     * Return true if support this code
     */
    boolean support(int status);

    /**
     * Return name of constructor
     */
    String getName();
}
