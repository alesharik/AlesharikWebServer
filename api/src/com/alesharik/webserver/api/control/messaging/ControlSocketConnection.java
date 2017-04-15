package com.alesharik.webserver.api.control.messaging;

import net.jcip.annotations.NotThreadSafe;

import java.io.IOException;

@NotThreadSafe
public interface ControlSocketConnection {
    String getRemoteHost();

    int getRemotePort();

    void sendMessage(ControlSocketMessage message) throws IOException;
}
