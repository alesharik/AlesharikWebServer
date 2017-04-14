package com.alesharik.webserver.control.socket;

import com.alesharik.webserver.api.SerialRepository;
import com.alesharik.webserver.logger.Logger;
import one.nio.net.Socket;
import one.nio.serial.DataStream;
import one.nio.serial.SerializerNotFoundException;
import org.glassfish.grizzly.utils.Charsets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * String message:
 * ┌─────────────┬──────────────────┬──────────────────┐
 * │action {byte}│ name{UTF string} │ data{UTF string} │
 * └─────────────┴──────────────────┴──────────────────┘
 * Object message:
 * ┌─────────────┬──────────────────┬─────────────────────┬─────────────┐
 * │action {byte}│ name{UTF string} │ serializerUid{long} │ data{int[]} │
 * └─────────────┴──────────────────┴─────────────────────┴─────────────┘
 * Get serializer message:
 * send:
 * ┌─────────────┬─────────────────────┐
 * │action {byte}│ serializerUid{long} │
 * └─────────────┴─────────────────────┘
 * return:
 * ┌────────────────────────┐
 * │ serializer{UTF string} │
 * └────────────────────────┘
 */
@Deprecated
class ControlSocketServerPool extends Thread {
    private static final byte MESSAGE = 1;
    private static final byte MESSAGE_OBJECT = 2;
    private static final byte GET_SERIALIZER = 4;

    private final ForkJoinPool forkJoinPool;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final String host;
    private final ControlSocketManagerImpl manager;

    private final int port;

    private Socket socket;

    public ControlSocketServerPool(int parallelism, String host, ControlSocketManagerImpl manager, int port) {
        this.host = host;
        this.manager = manager;
        this.port = port;
        this.forkJoinPool = new ForkJoinPool(parallelism);
    }

    @Override
    public synchronized void start() {
        isRunning.set(true);
        super.start();
    }

    @Override
    public void run() {
        try {
            socket = Socket.createServerSocket();
            socket.bind(host, port, 0);
            isRunning.set(false);
        } catch (IOException e) {
            Logger.log(e);
        }
        while(isRunning.get()) {
            try {
                Socket client = socket.accept();
                forkJoinPool.submit(new ClientSocketTask(client, manager));
            } catch (IOException e) {
                Logger.log(e);
            }
        }
        socket.close();
    }

    public void shutdown() {
        isRunning.set(false);
    }

    private static final class ClientSocketTask implements Runnable {
        private final Socket socket;
        private final ControlSocketManagerImpl manager;

        public ClientSocketTask(Socket socket, ControlSocketManagerImpl manager) {
            this.socket = socket;
            this.manager = manager;
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
                DataStream dataStream = new DataStream(msg.toByteArray());
                msg.close();

                byte action = dataStream.readByte();

                switch (action) {
                    case MESSAGE:
                        parseMessage(dataStream);
                        break;
                    case MESSAGE_OBJECT:
                        parseObjectMessage(dataStream);
                        break;
                    case GET_SERIALIZER:
                        parseGetSerializerMessage(dataStream);
                        break;
                    default:
                        Logger.log("Unexpected action: " + action);
                }
                socket.close();
            } catch (IOException | InterruptedException e) {
                Logger.log(e);
                socket.close();
            }
        }

        private void parseMessage(DataStream dataStream) {
            manager.receiveMessage(dataStream.readUTF(), dataStream.readUTF());
        }

        private void parseObjectMessage(DataStream dataStream) throws InterruptedException, IOException {
            try {
                manager.receiveMessage(dataStream.readUTF(), dataStream.readObject());
            } catch (SerializerNotFoundException e) {
                ControlSocketClient controlSocketClient = new ControlSocketClient(socket.getRemoteAddress().getHostName(), socket.getRemoteAddress().getPort());
                DataStream dataStream1 = new DataStream(512);
                dataStream1.writeByte(GET_SERIALIZER);
                dataStream1.writeLong(e.getUid());
                controlSocketClient.connectAndSend(dataStream1, true);
                controlSocketClient.shutdown();
                try {
                    manager.receiveMessage(dataStream.readUTF(), dataStream.readObject());
                } catch (IOException | ClassNotFoundException e1) {
                    Logger.log(e1);
                }
            } catch (IOException | ClassNotFoundException e) {
                Logger.log(e);
            }
        }

        private void parseGetSerializerMessage(DataStream dataStream) throws InterruptedException, IOException {
            try {
                String serializer = SerialRepository.serializeSerializer(dataStream.readLong());
                DataStream ret = new DataStream(serializer.getBytes(Charsets.UTF8_CHARSET).length + 256);
                ret.writeUTF(serializer);
                byte[] bytes = serializer.getBytes();
                socket.write(bytes, 0, bytes.length, 0);
            } catch (SerializerNotFoundException e) {
                Logger.log(e);
            }
        }
    }
}
