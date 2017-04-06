package com.alesharik.webserver.control.dashboard.websocket.plugins;

import com.alesharik.webserver.api.GsonUtils;
import com.alesharik.webserver.control.dashboard.websocket.DashboardWebSocketPlugin;
import com.alesharik.webserver.control.dashboard.websocket.WebSocketSender;

import javax.annotation.Nonnull;

/**
 * Used in navigator
 */
public class MenuDashboardWebSocketPlugin extends DashboardWebSocketPlugin {
    public MenuDashboardWebSocketPlugin(WebSocketSender sender) {
        super(sender);
    }

    @Override
    public String getName() {
        return "menu";
    }

    @Override
    public void receive(@Nonnull String command, @Nonnull String text) {
        switch (command) {
            case "update":
                send("set", GsonUtils.getGson().toJson(getDashboardDataHolder().getMenu()));
                send("render", "");
                break;
        }
    }
}
