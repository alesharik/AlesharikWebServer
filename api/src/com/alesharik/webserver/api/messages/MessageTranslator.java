package com.alesharik.webserver.api.messages;

/**
 * This class used for translate(setup) messages. The code run in same thread
 *
 * @see Messages
 */
@FunctionalInterface
public interface MessageTranslator<T> {
    void translate(T message);
}
