package com.alesharik.webserver.api.messages;

/**
 * This interface used for listen messages. The code will run in ANOTHER THREAD
 *
 * @see Messages
 */
@FunctionalInterface
public interface MessageListener<T extends Message> {
    void listen(T message);
}
