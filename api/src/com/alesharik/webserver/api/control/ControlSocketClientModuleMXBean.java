package com.alesharik.webserver.api.control;

/**
 * MXBean for {@link ControlSocketClientModule}
 */
public interface ControlSocketClientModuleMXBean {
    /**
     * Return client alive connection count
     */
    int getConnectionCount();
}
