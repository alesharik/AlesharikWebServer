package com.alesharik.webserver.api.control.messaging;

import net.jcip.annotations.NotThreadSafe;

import javax.annotation.Nonnull;

/**
 * Client side connection
 */
@NotThreadSafe
public interface ControlSocketClientConnection extends ControlSocketConnection {

    /**
     * Add new listener
     *
     * @param listener the listener
     */
    void addListener(Listener listener);

    void removeListener(Listener listener);

    boolean containsListener(Listener listener);

    /**
     * Wait for socket connected and authenticated
     */
    void awaitConnection();

    /**
     * Listen received messages
     */
    interface Listener {
        /**
         * Can this listener listen message. <code>true</code> - message instance handled by {@link #listen(ControlSocketMessage)} method,
         * <code>false</code> - ignore listener
         * @param messageClazz message class
         */
        boolean canListen(Class<?> messageClazz);

        /**
         * Listen message class. It receive original server message and you can cast it to your type
         *
         * @param message the message
         */
        void listen(@Nonnull ControlSocketMessage message);
    }

    /**
     * Contains data for authentication
     */
    interface Authenticator {
        /**
         * Return server password
         */
        String getPassword();

        /**
         * Return server login
         */
        String getLogin();
    }
}
