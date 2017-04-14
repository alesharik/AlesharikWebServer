package com.alesharik.webserver.api.control.messaging;

/**
 * Client side connection
 */
public interface ControlSocketClientConnection extends ControlSocketConnection {

    void addListener(Listener listener);

    void removeListener(Listener listener);

    boolean containsListener(Listener listener);

    /**
     * Listen received messages
     */
    interface Listener {
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
