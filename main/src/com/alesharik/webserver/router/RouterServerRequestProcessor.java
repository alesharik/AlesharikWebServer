package com.alesharik.webserver.router;

import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefix;
import one.nio.net.Socket;
import org.glassfish.grizzly.utils.Charsets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;

@Prefix("[RouterServerRequestProcessor]")
class RouterServerRequestProcessor extends Thread {
    private final int port;
    private final String host;
    private final ForkJoinPool pool;
    private final RouterServer.Servers servers;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    private volatile Socket serverSocket;

    public RouterServerRequestProcessor(int port, String host, int threadCount, RouterServer.Servers servers) {
        this.port = port;
        this.host = host;
        this.servers = servers;
        this.pool = new ForkJoinPool(threadCount);
        setName("RouterServerRequestProcessor");
    }

    @Override
    public void start() {
        try {
            serverSocket = Socket.createServerSocket();
            serverSocket.bind(host, port, 0);

            super.start();
            isRunning.set(true);
        } catch (IOException e) {
            Logger.log(e);
            isRunning.set(false);
        }
    }

    @Override
    public void run() {
        try {
            while(isRunning.get()) {
                Socket socket = serverSocket.accept();
                Logger.log("ass");
                pool.submit(new ProcessRequestTask(socket, servers));
            }
        } catch (IOException e) {
            if(e.getLocalizedMessage().equals("Socket closed")) {
                Logger.log("ok");
            }
            Logger.log(e);
            serverSocket.close();
            isRunning.set(false);
        }
    }

    public void shutdown() {
        isRunning.set(false);
        serverSocket.close();
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    private static class ProcessRequestTask implements Runnable {
        private final Socket socket;
        private final RouterServer.Servers servers;

        public ProcessRequestTask(Socket socket, RouterServer.Servers servers) {
            this.socket = socket;
            this.servers = servers;
        }

        @Override
        public void run() {
            try {
                final ByteArrayOutputStream msg = new ByteArrayOutputStream();
                int nRead = 1024;
                byte[] buffer = new byte[1024];
                while(nRead >= 1024) {
                    nRead = socket.read(buffer, 0, 1024);
                    msg.write(buffer);
                }
                final String message = new String(msg.toByteArray(), Charsets.UTF8_CHARSET);
                msg.close();

                String[] parts = message.split(":");
                switch (parts[0]) {
                    case "remove":
                        removeServer();
                        break;
                    case "add":
                        addServer(parseMicroservices(parts[1]));
                        break;
                    case "get":
                        getServer(parts[1]);
                        break;
                    default:
                        socket.close();
                }

            } catch (IOException e) {
                if(socket.isOpen()) {
                    byte[] data = RouterServer.INTERNAL_ERROR.getBytes(Charsets.UTF8_CHARSET);
                    try {
                        socket.write(data, 0, data.length, 0);
                    } catch (IOException e1) {
                        Logger.log(e1);
                    }
                    socket.close();
                }
            }
        }

        private void removeServer() throws IOException {
            if(servers.containsServer(socket.getRemoteAddress().getHostName() + ":" + socket.getRemoteAddress().getPort())) {
                servers.removeServer(socket.getRemoteAddress().getHostName() + ":" + socket.getRemoteAddress().getPort());
                byte[] data = RouterServer.OK.getBytes(Charsets.UTF8_CHARSET);
                socket.write(data, 0, data.length, 0);
                socket.close();
            } else {
                byte[] data = RouterServer.NOT_FOUND.getBytes(Charsets.UTF8_CHARSET);
                socket.write(data, 0, data.length, 0);
                socket.close();
            }
        }

        private void addServer(String[] microservices) throws IOException {
            servers.addServer(socket.getRemoteAddress().getHostName() + ":" + socket.getRemoteAddress().getPort(), microservices);
            byte[] data = RouterServer.OK.getBytes(Charsets.UTF8_CHARSET);
            socket.write(data, 0, data.length, 0);
            socket.close();
        }

        //TODO write distributed loading
        private void getServer(String microserviceName) throws IOException {
            String[] serverArray = servers.getServers(microserviceName);
            if(serverArray.length == 0) {
                byte[] data = RouterServer.NOT_FOUND.getBytes(Charsets.UTF8_CHARSET);
                socket.write(data, 0, data.length, 0);
                socket.close();
            } else {
                byte[] data = serverArray[0].getBytes(Charsets.UTF8_CHARSET);
                socket.write(data, 0, data.length, 0);
                socket.close();
            }
        }

        private static String[] parseMicroservices(String arr) {
            String array = arr.substring(1, arr.lastIndexOf("]"));
            return array.split(", ");
        }
    }
}
