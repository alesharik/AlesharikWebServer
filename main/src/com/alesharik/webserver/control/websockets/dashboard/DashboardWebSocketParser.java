/*
 *  This file is part of AlesharikWebServer.
 *
 *     AlesharikWebServer is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     AlesharikWebServer is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with AlesharikWebServer.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.alesharik.webserver.control.websockets.dashboard;

import com.alesharik.webserver.api.ComputerData;
import com.alesharik.webserver.control.dashboard.CommandBuilderFactory;
import com.alesharik.webserver.control.dashboard.DashboardDataHolder;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefixes;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Prefixes({"[ServerControl]", "[DashboardWebSocket]", "[DashboardWebSocketParser]"})
@Deprecated
final class DashboardWebSocketParser {
    private static final ThreadGroup THREAD_GROUP = new ThreadGroup("DashboardWebSocketThreads");
    private final DashboardWebSocketApplication application;
    private final DashboardDataHolder holder;
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(10, r -> new Thread(THREAD_GROUP, r));
    private final CurrentCompInfoTask currentCompInfoTask;

    public DashboardWebSocketParser(DashboardWebSocketApplication application, DashboardDataHolder holder) {
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
            case "system":
                parseSystemMessage(parts);
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
        if(parts[1].equals("getIpForMicroservice")) {
            application.sendMessage("menuPlugins:set:" + holder.getAllMenuPluginsAsJSONArray());
        }
    }

    private void parseCurrentCompInfoMessage(String[] parts) {
        if(parts[1].equals("start")) {
            currentCompInfoTask.start();
        } else if(parts[1].equals("shutdown")) {
            currentCompInfoTask.stop();
        }
    }

    @SuppressFBWarnings({"DM_GC", "DM_GC"}) //Because we need to handle "Collect GC" from dashboard
    private void parseSystemMessage(String[] parts) {
        if(parts[1].equals("gc")) {
            System.gc();
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
