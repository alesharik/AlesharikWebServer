package com.alesharik.webserver.api.control.messaging;

import net.jcip.annotations.NotThreadSafe;

/**
 * Client side connection
 */
@NotThreadSafe
public interface ControlSocketClientConnection extends ControlSocketConnection {

    void addListener(Listener listener);

    void removeListener(Listener listener);

    boolean containsListener(Listener listener);

    /**
     * Listen received messages
     */
    interface Listener {
        boolean canListen(Class<?> messageClazz);

        void listen(ControlSocketMessage message);
    }

    /**
     * Contains auth data
     */
    interface Authenticator {
        String getPassword();

        String getLogin();
    }
}
