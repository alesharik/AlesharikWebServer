package com.alesharik.webserver.api.sharedStorage.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation works only in class, annotated {@link UseSharedStorage}!
 * In runtime, annotated method transforms(using asm) into shared field getter. If storage has no field - return <code>null</code>.
 * The return object will tried to cast into return type. If it can't, throw {@link ClassCastException}
 * The value is name of shared field
 *
 * @see UseSharedStorage
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SharedValueGetter {
    String value();
}
