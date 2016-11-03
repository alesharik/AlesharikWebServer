package com.alesharik.webserver.api.sharedStorage.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This interface used for set sharedStorage of class. If class has this annotation, it will process in agent, and all
 * {@link SharedValueGetter}s and {@link SharedValueSetter} are replaced by asm code.
 *
 * @see SharedValueSetter
 * @see SharedValueGetter
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UseSharedStorage {
    String value();
}
