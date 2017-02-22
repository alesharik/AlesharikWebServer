package com.alesharik.webserver.api.agent.classPath;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Method, annotated by this annotation, will be called for every class, annotated by given annotation(<code>value()</code>).
 * Annotated method must be static and have only 1 argument: <code>{@link Class}</code>. Return result will be ignored
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ListenAnnotation {
    /**
     * Annotation to listen
     */
    Class<?> value();
}