package com.alesharik.webserver.control.websockets.dashboard;

import com.alesharik.webserver.api.ComputerData;
import com.alesharik.webserver.control.dashboard.CommandBuilderFactory;
import com.alesharik.webserver.control.dashboard.PluginDataHolder;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefixes;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Prefixes({"[ServerControl]", "[DashboardWebSocket]", "[DashboardWebSocketParser]"})
final class DashboardWebSocketParser {
    private static final ThreadGroup THREAD_GROUP = new ThreadGroup("DashboardWebSocketThreads");
    private final DashboardWebSocketApplication application;
    private final PluginDataHolder holder;
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(10, r -> new Thread(THREAD_GROUP, r));
    private final CurrentCompInfoTask currentCompInfoTask;

    public DashboardWebSocketParser(DashboardWebSocketApplication application, PluginDataHolder holder) {
        this.application = application;
        this.holder = holder;

        currentCompInfoTask = new CurrentCompInfoTask(application);
        executor.scheduleAtFixedRate(currentCompInfoTask, 0, 1, TimeUnit.SECONDS);
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
            case "menuPlugins":
                parseTopPluginsMessage(parts);
                break;
            case "currentCompInfo":
                parseCurrentCompInfoMessage(parts);
                break;
            default:
                Logger.log("Strange message: " + msg);
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

    private void parseTopPluginsMessage(String[] parts) {
        if(parts[1].equals("get")) {
            application.sendMessage("menuPlugins:set:" + holder.getAllMenuPluginsAsJSONArray());
        }
    }

    private void parseCurrentCompInfoMessage(String[] parts) {
        if(parts[1].equals("start")) {
            currentCompInfoTask.start();
        } else if(parts[1].equals("stop")) {
            currentCompInfoTask.stop();
        }
    }

    private static class CurrentCompInfoTask implements Runnable {
        private volatile boolean isRunning = false;
        private final DashboardWebSocketApplication application;

        public CurrentCompInfoTask(DashboardWebSocketApplication application) {
            this.application = application;
        }

        @Override
        public void run() {
            if(isRunning) {
                try {
                    application.sendMessage("currentCompInfo:set:" + ComputerData.INSTANCE.stringify());
                } catch (Exception e) {
                    Logger.log(e);
                }
            }
        }

        public synchronized void start() {
            isRunning = true;
        }

        public synchronized void stop() {
            isRunning = false;
        }
    }
}
