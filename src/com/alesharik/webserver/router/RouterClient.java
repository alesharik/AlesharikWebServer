package com.alesharik.webserver.router;

import com.alesharik.webserver.api.ConcurrentCompletableFuture;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefix;
import one.nio.net.Socket;
import org.glassfish.grizzly.utils.Charsets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class is the router client thread. Used for interact with socket
 */
@Prefix("[RouterClientThread]")
class RouterClient extends Thread {
    private final int port;
    private final String host;
    private final AtomicBoolean isRunning;
    private final ConcurrentLinkedQueue<Message> messageQueue;

    public RouterClient(int port, String host) {
        this.port = port;
        this.host = host;
        isRunning = new AtomicBoolean(false);
        messageQueue = new ConcurrentLinkedQueue<>();
        setName("RouterClientThread");
    }

    public void start() {
        super.start();
        isRunning.set(true);
        Logger.log("Router client thread successfully started at " + host + ":" + port);
    }

    public void shutdown() {
        isRunning.set(false);
        this.interrupt();
        Logger.log("Router client thread interrupted! Remaining tasks - " + messageQueue.size());
        messageQueue.clear();
    }

    public void run() {
        try {
            while(isRunning.get()) {
                Message message = messageQueue.poll();
                if(message != null) {
                    message.getFuture().set(sendMessage(message.getMessage()));
                } else {
                    Thread.sleep(1);
                }
            }
        } catch (InterruptedException e) {
            if(isRunning.get()) {
                Logger.log(e);
            }
        } catch (IOException e) {
            Logger.log(e);
        } finally {
            isRunning.set(false);
        }
    }

    private String sendMessage(String msg) throws IOException {
        Socket socket = Socket.create();
        socket.connect(host, port);
        byte[] data = msg.getBytes(Charsets.UTF8_CHARSET);
        socket.write(data, 0, data.length, 0);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int nRead = 1024;
        while(nRead >= 1024) {
            nRead = socket.read(buffer, 0, 1024);
            byteArrayOutputStream.write(buffer, 0, 1024);
        }
        String ret = new String(byteArrayOutputStream.toByteArray(), Charsets.UTF8_CHARSET);
        byteArrayOutputStream.close();
        socket.close();
        return ret;
    }

    synchronized Future<String> send(String message) {
        if(!isRunning()) {
            throw new RuntimeException("Thread not running!");
        }
        Message msg = new Message(message);
        Logger.log("msg " + message);
        messageQueue.add(msg);
        return msg.getFuture();
    }

    public boolean isRunning() {
        return isRunning.get() || this.isInterrupted();
    }

    private static final class Message {
        private final String message;
        private final ConcurrentCompletableFuture<String> future;

        public Message(String message) {
            this.message = message;
            this.future = new ConcurrentCompletableFuture<>();
        }

        public String getMessage() {
            return message;
        }

        public ConcurrentCompletableFuture<String> getFuture() {
            return future;
        }
    }
}
