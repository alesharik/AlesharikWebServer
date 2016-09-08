package com.alesharik.webserver.control.websockets.dashboard;

import com.alesharik.webserver.control.dashboard.CommandBuilderFactory;
import com.alesharik.webserver.control.dashboard.PluginDataHolder;

final class DashboardWebSocketParser {
    private final DashboardWebSocketApplication application;
    private final PluginDataHolder holder;

    public DashboardWebSocketParser(DashboardWebSocketApplication application, PluginDataHolder holder) {
        this.application = application;
        this.holder = holder;
    }

    public void parse(String msg) {
        String[] parts = msg.split(":");
        switch (parts[0]) {
            case "plugin":
                parsePluginMessage(parts);
                break;
            case "menu":
                parseMenuMessage(parts);
                break;
            default:
                break;
        }
    }

    private void parsePluginMessage(String[] parts) {
        application.directMessage(parts[1], parts[2]);
    }

    private void parseMenuMessage(String[] parts) {
        if(parts[1].equals("update")) {
            application.sendMessage(CommandBuilderFactory.menu().setMenu(holder.getMenu()).build());
        }
    }
}
