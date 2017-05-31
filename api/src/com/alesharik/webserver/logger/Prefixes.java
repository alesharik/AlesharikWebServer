package com.alesharik.webserver.logger;

import javax.annotation.Nonnull;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation tell logger what class has default prefixes. Logger will write default class prefixes on every log message
 * from annotated class
 */
@Documented
@Target({ElementType.TYPE, ElementType.PACKAGE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Prefixes {
    /**
     * Class prefixes
     */
    @Nonnull
    String[] value();

    /**
     * If true, logger will add <code>[Class:line]</code> prefix, where Class is class name and line is which line call logger
     */
    boolean requireDebugPrefix() default false;
}
