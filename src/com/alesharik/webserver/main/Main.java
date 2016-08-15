package com.alesharik.webserver.main;

import com.alesharik.webserver.logger.Logger;

import java.io.File;
import java.util.Date;

//TODO rewrite site as green terminal
public class Main {
    public static final String HOST = Helpers.getMachineExternalIP();
    public static final File USER_DIR = new File(System.getProperty("user.dir"));
    public static final File LOGS_FOLDER = new File(USER_DIR + "/logs");
    public static final File WWW = new File(USER_DIR + "/www");
    public static final File SERVER_DASHBOARD = new File(USER_DIR + "/serverDashboard");

    private static ServerController controller;
//    public static final String HOST = "127.0.0.1";

    public static void main(String[] args) {
        try {
            initStructure();
            Logger.setupLogger(new File(LOGS_FOLDER + generateLogName()));
            controller = new ServerController();
            controller.start();
//            WebSocketController controller = new WebSocketController(new URI("ws://" + HOST + ":7000/serverControl"), "admin", "admin");
//            controller.connect();
//            Logger.log(controller.getComputerInfo());
            System.in.read();
        } catch (Exception e) {
            Logger.log(e);
        }
//        try {
//            FileManager manager = new FileManager(USER_DIR, FileManager.FileHoldingMode.HOLD_AND_CHECK, FileManager.FileHoldingParams.IGNORE_HIDDEN_FILES);
//            System.out.println(new String(manager.readFile("/log.log")));
//        } catch (Exception e) {
//            Logger.log(e);
//        }
//        ServerController controllerServer = new ServerController();
//        controllerServer.loadConfig();
//        com.alesharik.webserver.tests.Server server = new com.alesharik.webserver.tests.Server(HOST);
//        server.start();
//        ServerController controllerServer = new ServerController();
//        controllerServer.initWebsocketServer();
//        controllerServer.initMainServer();
//        System.in.read();
//        server.shutdown();
//        AdminDataHolder holder = new AdminDataHolder(new File(System.getProperty("user.dir")), "test", "test");
//        holder.check("admin", "admin");
//        AsyncHttpClientConfig cfg = new DefaultAsyncHttpClientConfig.Builder().build();
//        AsyncHttpClient client = new DefaultAsyncHttpClient(cfg);
//        ServerController controller = new ServerController(client);
//        controller.connect("ws://" + HOST + ":6999/", new WebSocketControlListener("admin", "admin"));
//        OldPluginManager pl = new OldPluginManager(ServerController.USER_DIR);
//        pl.loadPlugins();
//        pl.test();

    }

    public static void shutdown() {
        Logger.log("[MAIN]", "Stopping...");
        controller.shutdown();
        System.exit(0);
    }

    private static String generateLogName() {
        Date date = new Date();
        date.setTime(System.currentTimeMillis());
        return "/Log-" + date.toString().replace(" ", "_");
    }

    private static void initStructure() {
        if(!LOGS_FOLDER.exists()) {
            if(!LOGS_FOLDER.mkdir()) {
                Logger.log("Can't create logs folder!");
                shutdown();
            }
        }
        if(!WWW.exists()) {
            if(!WWW.mkdir()) {
                Logger.log("Can't create www folder!");
                shutdown();
            }
        }
        if(!SERVER_DASHBOARD.exists()) {
            if(!SERVER_DASHBOARD.mkdir()) {
                Logger.log("Can't create server dashboard folder!");
                shutdown();
            }
        }
    }
}
