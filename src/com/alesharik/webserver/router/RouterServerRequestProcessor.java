package com.alesharik.webserver.router;

import com.alesharik.webserver.logger.Logger;
import one.nio.net.Socket;
import one.nio.serial.DataStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;

class RouterServerRequestProcessor extends Thread {
    private final int port;
    private final String host;
    private final ForkJoinPool pool;
    private final ConcurrentHashMap<ArrayList<String>, String> servers;
    private AtomicBoolean isRunning = new AtomicBoolean(true);
    private volatile Socket serverSocket;

    public RouterServerRequestProcessor(int port, String host, RouterServer.WorkingMode mode, ConcurrentHashMap<ArrayList<String>, String> servers) {
        this.port = port;
        this.host = host;
        this.servers = servers;
        if(mode == RouterServer.WorkingMode.SIMPLE) {
            pool = new ForkJoinPool(2);
        } else {
            pool = new ForkJoinPool(20);
        }
    }

    @Override
    public void run() {
        try {
            serverSocket = Socket.createServerSocket();
            serverSocket.bind(host, port, 0);

            while(isRunning.get()) {
                Socket socket = serverSocket.accept();
                pool.submit(new ProcessRequest(socket, servers));
            }
            Logger.log("dsaf");
            serverSocket.close();
        } catch (IOException e) {
            Logger.log(e);
        }
    }

    public void shutdown() throws IOException {
        isRunning.set(false);
        Socket socket = Socket.create();
        socket.connect(host, port);
        DataStream dataStream = new DataStream(256);
        dataStream.writeUTF("asd");
        byte[] array = dataStream.array();
        socket.write(array, 0, array.length, 0);
        socket.close();
    }

    private static class ProcessRequest implements Runnable {
        private final Socket socket;
        private final ConcurrentHashMap<ArrayList<String>, String> servers;

        public ProcessRequest(Socket socket, ConcurrentHashMap<ArrayList<String>, String> servers) {
            this.socket = socket;
            this.servers = servers;
        }

        @Override
        public void run() {
            try {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int nRead = 1024;

                while(nRead >= 1024) {
                    nRead = socket.read(buffer, 0, 1024);
                    stream.write(buffer, 0, nRead);
                }

                DataStream data = new DataStream(stream.toByteArray());

                String utf = data.readUTF();
                switch (utf) {
                    case "add":
                        servers.put((ArrayList<String>) data.readObject(), socket.getRemoteAddress().toString());
                        break;
                    case "get":
                        String name = data.readUTF();
                        for(ArrayList<String> strings : servers.keySet()) {
                            if(strings.contains(name)) {
                                String address = servers.get(strings);

                                DataStream dataStream = new DataStream(256);
                                dataStream.writeUTF(address);
                                byte[] array = dataStream.array();
                                socket.write(array, 0, array.length, 0);
                                break;
                            }
                        }
                        break;
                    case "remove":
                        String server = socket.getRemoteAddress().toString();
                        servers.entrySet().stream()
                                .filter(arrayListStringEntry -> arrayListStringEntry.getValue().equals(server))
                                .forEachOrdered(arrayListStringEntry -> servers.remove(arrayListStringEntry.getKey()));
                        break;
                }
            } catch (IOException | ClassNotFoundException e) {
                Logger.log(e);
            }
        }
    }
}
