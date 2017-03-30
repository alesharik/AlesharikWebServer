package com.alesharik.webserver.server.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Register new WebSocketApplication. Your class must extend {@link org.glassfish.grizzly.websockets.WebSocketApplication}.
 * WSApplication registered by default
 *
 * @see org.glassfish.grizzly.websockets.WebSocketEngine#register(String, String, org.glassfish.grizzly.websockets.WebSocketApplication)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WSApplication {
    String contextPath() default "";

    /**
     * Url pattern
     */
    String value();

    /**
     * {@link WSChecker} class
     */
    Class<?> checker() default WSChecker.class;
}
