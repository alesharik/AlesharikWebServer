package com.alesharik.webserver.api.agent.transformer;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotated method will be executed on every class the agent tries to load.
 * Annotated method must be static!
 * Executes after {@link Transform}
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TransformAll {
}
