package com.alesharik.webserver.api.agent.transformer;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * DO NOT TRY TRANSFORM CLASS TRANSFORMERS! Class, annotated by this annotation, can have {@link Transform} or/and {@link TransformAll}
 * annotations
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ClassTransformer {
}
