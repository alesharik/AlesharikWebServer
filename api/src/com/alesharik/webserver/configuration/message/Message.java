package com.alesharik.webserver.configuration.message;

import javax.annotation.concurrent.Immutable;

/**
 * Message been used as container for hold data sending between two {@link MessageStream}s.
 * This class is immutable.
 * Use <code>newMessage</code> instead of constructor
 *
 * @param <T>
 */
@Immutable
public interface Message<T> {
    /**
     * Create new empty message. Used instead of constructor.
     */
    T newMessage();
}
