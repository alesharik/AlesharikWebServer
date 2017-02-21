package com.alesharik.webserver.api.agent.classPath;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class, annotated by this annotation, can have {@link ListenAnnotation}, {@link ListenClass}, {@link ListenInterface}
 * annotated methods.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ClassPathScanner {
}
