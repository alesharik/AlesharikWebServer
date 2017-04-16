package com.alesharik.webserver.api.control.messaging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use with {@link ControlSocketMessageHandler}. Wire handler for message
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WireControlSocketMessage {
    Class<?> value();
}
