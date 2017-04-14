package com.alesharik.webserver.api.control.messaging;

public interface ControlSocketConnection {
    String getRemoteHost();

    int getRemotePort();

    void sendMessage(ControlSocketMessage message);
}
