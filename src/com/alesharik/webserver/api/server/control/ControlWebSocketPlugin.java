package com.alesharik.webserver.api.server.control;

/**
 * This interface used for receive messages form {@link com.alesharik.webserver.control.websockets.control.ControlWebSocket}
 */
public interface ControlWebSocketPlugin {
    /**
     * Return unique name of this plugin.<br>
     * This name must be not null or empty!
     * WARNING!Name must contains no ':' character!
     */
    String getName();

    /**
     * Called if plugin has string message
     */
    void onMessage(String message);

    /**
     * Called if plugin has Object message
     */
    void onMessage(Object message);
}
