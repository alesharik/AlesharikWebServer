package com.alesharik.webserver.router;

import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.microservices.server.MicroserviceServer;
import one.nio.net.Socket;
import one.nio.serial.DataStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

class RouterClient extends Thread {
    private int port;
    private String host;
    private MicroserviceServer server;
    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private Socket socket;

    public RouterClient(int port, String host, MicroserviceServer server) {
        this.port = port;
        this.host = host;
        this.server = server;
    }

    public void run() {
        try {
            connect();
            isRunning.set(true);
            while(isRunning.get()) {
                Thread.sleep(10 * 60 * 1000);
            }
            disconnect();
        } catch (IOException | InterruptedException e) {
            Logger.log(e);
        }
    }

    synchronized String get(String name) throws IOException {
        if(!isRunning.get()) {
            return "";
        }
        socket = Socket.create();
        socket.connect(host, port);
        DataStream dataStream = new DataStream(256);
        dataStream.writeUTF("get");
        dataStream.writeUTF(name);

        byte[] array = dataStream.array();
        socket.write(array, 0, array.length, 0);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int nRead = 1024;

        while(nRead >= 1024) {
            nRead = socket.read(buffer, 0, 1024);
            stream.write(buffer, 0, nRead);
        }

        DataStream data = new DataStream(stream.toByteArray());
        socket.close();
        return data.readUTF();
    }

    private void connect() throws IOException {
        if(server != null) {
            socket = Socket.create();
            socket.connect(host, port);
            DataStream dataStream = new DataStream(256);
            dataStream.writeUTF("add");
            dataStream.writeObject(server.getMicroserviceNames());
            byte[] array = dataStream.array();
            socket.write(array, 0, array.length, 0);
            socket.close();
        }
    }

    private void disconnect() throws IOException {
        socket = Socket.create();
        socket.connect(host, port);
        DataStream dataStream = new DataStream(256);
        dataStream.writeUTF("remove");
        byte[] array = dataStream.array();
        socket.write(array, 0, array.length, 0);

        socket.close();
    }

    public void shutdown() {
        isRunning.set(false);
    }
}
