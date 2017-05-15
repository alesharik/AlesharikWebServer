package com.alesharik.webserver.logger;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation used by logger for retrieve class prefix
 */
@Deprecated
@Target({ElementType.TYPE, ElementType.PACKAGE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Prefix {
    String value();

    /**
     * If true, logger add [className:line] prefix
     */
    boolean requireDebugPrefix() default false;
}
