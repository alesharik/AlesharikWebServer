package com.alesharik.webserver.api.agent;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The method in @{@link ClassTransformer} class executes on every class the anent tries to load.
 * The method must be static!
 * Executes after {@link Transform}
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TransformAll {
}
