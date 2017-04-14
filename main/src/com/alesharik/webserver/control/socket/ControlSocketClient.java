package com.alesharik.webserver.control.socket;

import com.alesharik.webserver.api.SerialRepository;
import com.alesharik.webserver.logger.Logger;
import one.nio.net.Socket;
import one.nio.serial.DataStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

@Deprecated
class ControlSocketClient extends Thread {
    private final String host;
    private final int port;
    private final AtomicBoolean isGetSerializer = new AtomicBoolean(false);

    private volatile Socket socket;

    public ControlSocketClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connectAndSend(DataStream dataStream, boolean getSerializer) throws IOException {
        isGetSerializer.set(getSerializer);
        socket = Socket.create();
        socket.connect(host, port);
        byte[] array = dataStream.array();
        socket.write(array, 0, array.length, 0);
        start();
    }

    public void run() {
        if(isGetSerializer.get()) {
            try {
                final ByteArrayOutputStream msg = new ByteArrayOutputStream();
                int nRead = 1024;
                byte[] buffer = new byte[1024];
                while(nRead >= 1024) {
                    nRead = socket.read(buffer, 0, 1024);
                    msg.write(buffer);
                }
                DataStream dataStream = new DataStream(msg.toByteArray());
                msg.close();

                SerialRepository.addSerializedSerializer(dataStream.readUTF());
            } catch (IOException e) {
                Logger.log(e);
            }
        } else {
            while(socket.isOpen()) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    //Thread shutdown
                }
            }
        }
        notifyAll();
    }

    /**
     * Wait for socket completion
     */
    public void shutdown() throws InterruptedException {
        wait();
    }

    /**
     * Close socket immediately
     */
    public void shutdownNow() {
        this.interrupt();
        socket.close();
    }
}
