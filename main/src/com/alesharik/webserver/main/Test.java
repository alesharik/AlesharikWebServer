package com.alesharik.webserver.main;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class Test {
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        Testa.asd();
//        Utils.Partition[] partitions = Utils.getComputerPartitions();
//        Arrays.asList(partitions);
//        Thread.currentThread().setUncaughtExceptionHandler(LoggerUncaughtExceptionHandler.INSTANCE);
//        RouterServer routerServer = new RouterServer(9982, "localhost", 5);
//
//        Router router = new Router(9982, "localhost");
//        Logger.setupLogger(new File(LOGS_FOLDER + generateLogName()));
//
//        while(true) {
//            routerServer.start();
//            router.start();
//
//            String[] strings = {"asd", "sdf"};
//            router.addNewMicroserviceServer(strings);
//            System.out.println(router.getIpForMicroservice("asd").get());
//            router.removeMicroserviceServer();
//
//            router.shutdown();
//            routerServer.shutdown();
//        }
    }


    private static final class Testa {
        public static final int asd() {
            try {
                return 0;
            } finally {
                System.out.println("asd");
            }
        }
    }

    private static String generateLogName() {
        Date date = new Date();
        date.setTime(System.currentTimeMillis());
        return "/Log-" + date.toString().replace(" ", "_");
    }
}
