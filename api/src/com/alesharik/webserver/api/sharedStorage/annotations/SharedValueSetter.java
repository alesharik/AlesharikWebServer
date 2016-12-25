package com.alesharik.webserver.api.sharedStorage.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation works only in class, annotated {@link UseSharedStorage}!
 * In runtime, annotated method transforms(using asm) into shared field setter. All code in this method will removed!
 * If method has no parameters, throw {@link com.alesharik.webserver.exceptions.IllegalMethodException}. If method has
 * more parameters than 1, the code use first parameter. The parameter casts to {@link Object} and store.
 * The value is name of shared field
 *
 * @see UseSharedStorage
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SharedValueSetter {
    String value();
}
