package com.alesharik.webserver.api.control;

import java.util.Set;

/**
 * MXBean for {@link ControlSocketServerModule}
 */
public interface ControlSocketServerModuleMXBean {
    /**
     * Return client alive connection count
     */
    int connectionCount();

    /**
     * Return server port
     */
    int getPort();

    /**
     * Return listen addresses
     */
    Set<String> getListenAddresses();
}
