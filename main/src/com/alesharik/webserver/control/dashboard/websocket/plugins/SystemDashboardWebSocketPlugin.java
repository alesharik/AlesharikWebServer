package com.alesharik.webserver.control.dashboard.websocket.plugins;

import com.alesharik.webserver.control.dashboard.websocket.DashboardWebSocketPlugin;
import com.alesharik.webserver.control.dashboard.websocket.WebSocketSender;
import com.alesharik.webserver.logger.Prefixes;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.annotation.Nonnull;

@Prefixes({"[DashboardWebSocketPlugin]", "[SystemDashboardWebSocketPlugin]"})
public class SystemDashboardWebSocketPlugin extends DashboardWebSocketPlugin {
    private static final String GC_COMMAND = "gc";

    public SystemDashboardWebSocketPlugin(WebSocketSender sender) {
        super(sender);
    }

    @Override
    public String getName() {
        return "system";
    }

    @SuppressFBWarnings("DM_GC") //Because I need to call GC
    @Override
    public void receive(@Nonnull String command, @Nonnull String text) {
        switch (command) {
            case GC_COMMAND:
                System.gc();
                break;
            default:
                System.err.println("Command " + command + " with data " + text + " not expected!");
        }
    }
}
