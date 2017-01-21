package com.alesharik.webserver.plugin.accessManagers;

import com.alesharik.webserver.api.control.ControlWebSocketPlugin;
import com.alesharik.webserver.api.control.ControlWebSocketPluginWrapper;

public interface ControlAccessManager {
    ControlWebSocketPluginWrapper registerNewControlWebSocketPlugin(ControlWebSocketPlugin plugin);

    void unregisterControlWebSocketPlugin(ControlWebSocketPlugin plugin);
}
