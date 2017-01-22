package com.alesharik.webserver.control.socket;

/**
 * Class, implementing this interface, used only for send message to control socket
 */
public interface ControlSocketSender {
    /**
     * Send {@link String} message
     *
     * @param message the message
     */
    void send(String message);

    /**
     * Send {@link Object} message
     *
     * @param message the message
     */
    void send(Object message);
}
