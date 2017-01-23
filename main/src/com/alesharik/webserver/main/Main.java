package com.alesharik.webserver.main;

import com.alesharik.webserver.api.agent.RequireLoaded;
import com.alesharik.webserver.api.sharedStorage.SharedStorageClassTransformer;
import com.alesharik.webserver.api.sharedStorage.annotations.UseSharedStorage;
import com.alesharik.webserver.logger.Logger;

import java.io.File;
import java.io.IOException;

//TODO rewrite site as green terminal
//TODO add more prefixes to java
//TODO написать профилирование базы данных и реквестов

@UseSharedStorage("config")
@RequireLoaded(MainLoggerConfiguration.class)
public class Main {
    @SuppressWarnings("unused")
    private static final SharedStorageClassTransformer transformer = new SharedStorageClassTransformer();

    public static final File USER_DIR = new File(System.getProperty("user.dir"));
    @Deprecated
    public static final File LOGS_FOLDER = new File(USER_DIR + "/logs");
    @Deprecated
    public static final File WWW = new File(USER_DIR + "/www");
    @Deprecated
    public static final File SERVER_DASHBOARD = new File(USER_DIR + "/serverDashboard");

    private static ServerController controller;
//    public static final String HOST = "127.0.0.1";

    public static void main(String[] args) throws InterruptedException {
        try {
//            Agent.reload(Main.class);
//            initStructure();
//            Logger.setupLogger(new File(LOGS_FOLDER + generateLogName()));
            controller = new ServerController();
            controller.start();
//            Class<?> clazz = Class.class;
//            Runnable runnable = () -> {new NullPointerException();};
//            runnable.run();
//            SharedStorageManagerFactory.INSTANCE.newInstance(new Testt());
//            new Testt().run();
//
////            WebSocketController controller = new WebSocketController(new URI("ws://" + HOST + ":7000/serverControl"), "admin", "admin");
////            controller.connectAndSend();
////            Logger.log(controller.getComputerInfo());
////            Logger.log(Utils.getExternalIp());

        } catch (Throwable e) {
            Logger.log(e);
//            e.printStackTrace();
            System.exit(0);
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
//        AdminDataStorageImpl holder = new AdminDataStorageImpl(new File(System.getProperty("user.dir")), "test", "test");
//        holder.check("admin", "admin");
//        AsyncHttpClientConfig cfg = new DefaultAsyncHttpClientConfig.Builder().build();
//        AsyncHttpClient client = new DefaultAsyncHttpClient(cfg);
//        ServerController controller = new ServerController(client);
//        controller.connectAndSend("ws://" + HOST + ":6999/", new WebSocketControlListener("admin", "admin"));
//        OldPluginManager pl = new OldPluginManager(ServerController.USER_DIR);
//        pl.loadPlugins();
//        pl.test();
//        FileManager mainFileManager = new FileManager(Main.SERVER_DASHBOARD, FileManager.FileHoldingMode.HOLD_AND_CHECK,
//                FileManager.FileHoldingParams.IGNORE_HIDDEN_FILES,
//                FileManager.FileHoldingParams.DISABLE_IGNORE_LOGS_FOLDER
//                FileManager.FileHoldingParams.ENABLE_COMPRESSION.setValue(CompressionUtils.CompressLevel.BEST_COMPRESSION.getValue()));
//        );
//        new Thread(() -> {
//            while(true) {
//                try {
//                    sleep(1);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
        // Executor that will be used to construct new threads for consumers
//        Executor execution = Executors.newCachedThreadPool();
//
//        // Specify the size of the ring buffer, must be power of 2.
//        int bufferSize = 1024;
//
//        // Construct the Disruptor
//        Disruptor<LongEvent> disruptor = new Disruptor<LongEvent>(() -> new LongEvent(), bufferSize, new ThreadFactory() {
//            @Override
//            public Thread newThread(Runnable r) {
//                Thread thread = new Thread(r);
//                return thread;
//            }
//        });
//
//        // Connect the handler
//        disruptor.handleEventsWith(Main::handleEvent);
//
//        // Start the Disruptor, starts all threads running
//        disruptor.start();
//
//        // Get the ring buffer from the Disruptor to be used for publishing.
//        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();
//
//        ByteBuffer bb = ByteBuffer.allocate(8);
//        for (long l = 0; true; l++)
//        {
//            bb.putLong(0, l);
//            ringBuffer.publishEvent(Main::translate, bb);
//            Thread.sleep(1000);
//        }
    }

    public static void shutdown() throws IOException {
        Logger.log("[MAIN]", "Stopping...");
        controller.shutdown();
        System.exit(0);
    }

    private static void initStructure() {
        if(!LOGS_FOLDER.exists()) {
            if(!LOGS_FOLDER.mkdir()) {
                Logger.log("Can't create logs folder!");
            }
        }
        if(!WWW.exists()) {
            if(!WWW.mkdir()) {
                Logger.log("Can't create www folder!");
            }
        }
        if(!SERVER_DASHBOARD.exists()) {
            if(!SERVER_DASHBOARD.mkdir()) {
                Logger.log("Can't create server dashboard folder!");
            }
        }
    }
}