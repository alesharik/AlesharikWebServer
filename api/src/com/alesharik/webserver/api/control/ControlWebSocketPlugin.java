package com.alesharik.webserver.api.control;

/**
 * Class, implementing this interface, used for receive messages form ControlWebSocket
 */
public interface ControlWebSocketPlugin {
    /**
     * Return unique name of this plugin.<br>
     * This name must be not null or empty!
     * WARNING!Name must contains no ':' character!
     */
    String getName();

    /**
     * Process socket string message
     */
    void onMessage(String message);

    /**
     * Process socket {@link Object} message. This message deserialized with one-nio serialization.
     * If you know the class of the object, you can cast it to the class
     */
    void onMessage(Object message);
}
