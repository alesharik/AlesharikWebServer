package com.alesharik.webserver.api.messages;

/**
 * This class used for generate new specific {@link Message}s
 *
 * @param <T> the specific message
 */
public abstract class AbstractMessageFactory<T extends Message> {
    /**
     * Name of a message
     */
    public abstract String getName();

    public abstract T newMessage();
}
