package com.alesharik.webserver.api.control;

import java.util.Set;

public interface ControlSocketServerModuleMXBean {
    int connectionCount();

    int getPort();

    Set<String> getListenAddresses();
}
