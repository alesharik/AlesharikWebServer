package com.alesharik.webserver.main;

import org.glassfish.grizzly.http.util.Base64Utils;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class Test {
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException, ClassNotFoundException {
//        AtomicReference<Socket> server = new AtomicReference<>(Socket.createServerSocket());
//        server.get().bind("localhost", 4090, 0);
//        new Thread(() -> {
//            try {
//                server.get().accept();
//                System.out.println("Yay!");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }).start();
//        Thread.sleep(1000);
//        server.get().close();
//        Testa.asd();
//        Utils.Partition[] partitions = Utils.getComputerPartitions();
//        Arrays.asList(partitions);
//        Thread.currentThread().setUncaughtExceptionHandler(LoggerUncaughtExceptionHandler.INSTANCE);
//        RouterServer routerServer = new RouterServer(9982, "localhost", 5);
//
//        Router router = new Router(9982, "localhost");
//        Logger.setupLogger(new File(LOGS_FOLDER + generateLogName()));
//
//        while(true) {
//            routerServer.setup();
//            router.setup();
//
//            String[] strings = {"asd", "sdf"};
//            router.addNewMicroserviceServer(strings);
//            System.out.println(router.getIpForMicroservice("asd").get());
//            router.removeMicroserviceServer();
//
//            router.shutdown();
//            routerServer.shutdown();
//        }
//        Logger.TextFormatter textFormatter = new Logger.TextFormatter(Logger.ForegroundColor.CYAN, Logger.BackgroundColor.RED, false);
//        System.out.print("asda ");
//        System.out.println(textFormatter.format("test"));
//        String dotFileContents =
//                new FastClasspathScanner("com.alesharik.webserver.api.ticking")
//                        .strictWhitelist()
//                        .ignoreFieldVisibility()
//                        .enableFieldTypeIndexing()
//                        .scan()
//                        .generateClassGraphDotFile(100F, 100F);
//        File file = new File("./text.txt");
//        file.createNewFile();
//        Files.write(file.toPath(), dotFileContents.getBytes(Charsets.UTF8_CHARSET));
//        DataStream dataStream = new DataStream(40960);
//        dataStream.writeObject(new Testt(213));
//        DataStream dataStream1 = new DataStream(dataStream.array());
//        Testt t = (Testt) dataStream1.readObject();
//        System.out.println(t.test);

//        List<Field> defaultFields = Arrays.asList(Testt.class.getDeclaredFields());
//        byte[] code = DelegateGenerator.generate(Testt.class, new FieldDescriptor[0], defaultFields);
//        System.out.println(new String(code, Charsets.UTF8_CHARSET));
//
//        java.nio.file.Files.write(new java.io.File("TesttS.class").toPath(), Repository.get(TestBenchmark.Testt.class).code());
//
//
//        Options options = new OptionsBuilder()
//                .include(".*" + TestBenchmark.class.getSimpleName() + ".*")
//                .forks(1)
//                .build();
////        Logger.setupLogger(new File(com.alesharik.webserver.main.Main.USER_DIR + "/loggg.log"));
//        try {
//            new Runner(options).run();
//        } catch (RunnerException e) {
//            e.printStackTrace();
//        }
//        Logger.setupLogger(File.createTempFile("adgsdf", "sdfgsadf"), 10);
//        System.out.println("Hi!");
//        System.err.println("Hi!");
//        System.out.println("df");
        byte[] bytes = new byte[1024];
        new SecureRandom().nextBytes(bytes);
        System.out.println(Base64Utils.encodeToString(bytes, false));
    }

    private static String generateLogName() {
        Date date = new Date();
        date.setTime(System.currentTimeMillis());
        return "/Log-" + date.toString().replace(" ", "_");
    }
}
