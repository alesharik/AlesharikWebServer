package com.alesharik.webserver.control.dashboard.websocket;

import java.util.Collection;

public interface DashboardWebSocketPluginManagerMXBean {
    /**
     * Return plugin count
     */
    int pluginCount();

    /**
     * Return all plugin classes
     */
    Collection<Class<?>> pluginClasses();
}
