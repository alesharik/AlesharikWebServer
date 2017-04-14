package com.alesharik.webserver.api.control;

import com.alesharik.webserver.api.control.messaging.ControlSocketServerConnection;

import java.util.List;
import java.util.Set;

public interface ControlSocketServerModuleMXBean {
    int connectionCount();

    List<ControlSocketServerConnection> getConnections();

    int getPort();

    Set<String> getListenAddresses();
}
