package com.alesharik.webserver.control.dashboard.websocket.plugins;

import com.alesharik.webserver.api.ComputerData;
import com.alesharik.webserver.control.dashboard.websocket.DashboardWebSocketPlugin;
import com.alesharik.webserver.control.dashboard.websocket.WebSocketSender;
import com.alesharik.webserver.logger.Prefixes;

import javax.annotation.Nonnull;
import java.util.Timer;
import java.util.TimerTask;

@Prefixes({"[DashboardWebSocketPlugin]", "[CurrentCompInfoDashboardWebSocketPlugin]"})
public class CurrentCompInfoDashboardWebSocketPlugin extends DashboardWebSocketPlugin {
    private static final String SET_COMMAND = "set";
    private static final String START_COMMAND = "start";
    private static final String STOP_COMMAND = "stop";

    private final Timer timer;
    private Sender task;

    public CurrentCompInfoDashboardWebSocketPlugin(WebSocketSender sender) {
        super(sender);
        timer = new Timer("CurrentCompInfoDashboardWebSocketPlugin-SenderTimer", true);
        task = new Sender();
    }

    @Override
    public String getName() {
        return "currentCompInfo";
    }

    @Override
    public void receive(@Nonnull String command, @Nonnull String text) {
        switch (command) {
            case START_COMMAND:
                task = new Sender();
                timer.scheduleAtFixedRate(task, 0, 1000);
                break;
            case STOP_COMMAND:
                task.cancel();
                break;
            default:
                System.err.println("Unexpected command " + command + " with data " + text);
        }
    }

    @Override
    public void shutdownNow() {
        task.cancel();
    }

    @Override
    public void shutdown() {
        task.cancel();
    }

    private final class Sender extends TimerTask {
        @Override
        public void run() {
            send(SET_COMMAND, ComputerData.INSTANCE.stringify());
        }
    }
}
