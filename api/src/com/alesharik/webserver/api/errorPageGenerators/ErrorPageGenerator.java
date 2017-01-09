package com.alesharik.webserver.api.errorPageGenerators;

import org.glassfish.grizzly.http.server.Request;

import javax.annotation.concurrent.ThreadSafe;
import java.util.List;
import java.util.Objects;

/**
 * This interface used for generate error pages
 */
@ThreadSafe
public interface ErrorPageGenerator {
    /**
     * Generate error page without description and exception
     *
     * @param request      the request
     * @param status       status
     * @param reasonPhrase reason phrase
     * @return HTML code of error page
     */
    default String generate(Request request, int status, String reasonPhrase) {
        Objects.requireNonNull(request);
        Objects.requireNonNull(reasonPhrase);

        return generate(request, status, reasonPhrase, null, null);
    }

    /**
     * Generate error page with description and without exception
     *
     * @param request      the request
     * @param status       status
     * @param reasonPhrase reason phrase
     * @param description  description
     * @return HTML code of error page
     */
    default String generate(Request request, int status, String reasonPhrase, String description) {
        Objects.requireNonNull(request);
        Objects.requireNonNull(reasonPhrase);

        return generate(request, status, reasonPhrase, description, null);
    }

    /**
     * Generate error page with description and without exception
     *
     * @param request      the request
     * @param status       status
     * @param reasonPhrase reason phrase
     * @param description  description
     * @param throwable    throwable
     * @return HTML code of error page
     */
    String generate(Request request, int status, String reasonPhrase, String description, Throwable throwable);

    /**
     * Add error page constructor
     */
    void addErrorPageConstructor(ErrorPageConstructor constructor);

    /**
     * Remove specified error page constructor
     */
    void removeErrorPageConstructor(ErrorPageConstructor constructor);

    /**
     * Return <code>true</code> if generator contains constructor, overwise <code>false</code>
     */
    boolean containsErrorPageConstructor(ErrorPageConstructor constructor);

    /**
     * Return all error page constructors for status
     *
     * @param status status code
     * @return immutable list
     */
    List<ErrorPageConstructor> getErrorPageConstructorsForStatus(int status);

    /**
     * Default error page constructor used for generate error page
     *
     * @param errorPageConstructor constructor or <code>null</code>. <code>null</code> set last added constructor as default
     * @throws IllegalArgumentException if constructor doesn't support given status
     */
    void setDefaultErrorPageConstructor(ErrorPageConstructor errorPageConstructor, int status);
}
