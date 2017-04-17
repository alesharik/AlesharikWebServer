package com.alesharik.webserver.api.control.messaging;

import net.jcip.annotations.NotThreadSafe;

import java.io.IOException;

/**
 * This class is base ControlSocket connection
 */
@NotThreadSafe
public interface ControlSocketConnection {
    /**
     * Return remote host
     */
    String getRemoteHost();

    /**
     * Return remote port
     */
    int getRemotePort();

    /**
     * Send message to opponent
     *
     * @param message the message
     * @throws IOException if anything happens
     */
    void sendMessage(ControlSocketMessage message) throws IOException;
}
