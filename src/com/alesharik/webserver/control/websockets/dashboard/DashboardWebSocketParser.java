package com.alesharik.webserver.control.websockets.dashboard;

public final class DashboardWebSocketParser {
    private final DashboardWebSocketApplication application;

    public DashboardWebSocketParser(DashboardWebSocketApplication application) {
        this.application = application;
    }

    public void parse(String msg) {
        String[] parts = msg.split(":");
        switch (parts[0]) {
            case "plugin":
                parsePluginMessage(parts);
        }
    }

    private void parsePluginMessage(String[] parts) {
        application.directMessage(parts[1], parts[2]);
    }
}
