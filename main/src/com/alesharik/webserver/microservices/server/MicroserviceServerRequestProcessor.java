package com.alesharik.webserver.microservices.server;

import com.alesharik.webserver.api.SerialRepository;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefix;
import com.alesharik.webserver.microservices.api.MicroserviceEvent;
import com.lmax.disruptor.RingBuffer;
import one.nio.net.Socket;
import one.nio.serial.DataStream;
import one.nio.serial.SerializerNotFoundException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;

@Prefix("[MicroserviceServerRequestProcessor]")
class MicroserviceServerRequestProcessor {
    private final String host;
    private final int port;

    private ForkJoinPool pool;
    private volatile RingBuffer<Event> buffer;
    private AtomicBoolean isRunning = new AtomicBoolean(true);

    public MicroserviceServerRequestProcessor(RingBuffer<Event> buffer, String host, int port, MicroserviceServer.WorkingMode mode) {
        this.buffer = buffer;
        this.host = host;
        this.port = port;
        if(mode == MicroserviceServer.WorkingMode.SIMPLE) {
            pool = new ForkJoinPool(2);
        } else {
            pool = new ForkJoinPool(16);
        }
    }

    public synchronized void setBuffer(RingBuffer<Event> buffer) {
        this.buffer = buffer;
    }

    public void start() {
        isRunning.set(true);
        pool = new ForkJoinPool(10);

        try {
            Socket serverSocket = Socket.createServerSocket();
            serverSocket.bind(host, port, 0);

            while(isRunning.get()) {
                Socket socket = serverSocket.accept();
                pool.submit(new ProcessTask(socket, buffer));
            }

            serverSocket.close();
        } catch (IOException e) {
            Logger.log(e);
        }
    }

    public void shutdown() {
        isRunning.set(false);
        pool.shutdown();
    }

    private static class ProcessTask implements Runnable {
        private final Socket socket;
        private final RingBuffer<Event> ringBuffer;

        public ProcessTask(Socket socket, RingBuffer<Event> ringBuffer) {
            this.socket = socket;
            this.ringBuffer = ringBuffer;
        }

        @Override
        public void run() {
            try {
                //Read message
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int nRead = 1024;

                while(nRead >= 1024) {
                    nRead = socket.read(buffer, 0, 1024);
                    stream.write(buffer, 0, nRead);
                }

                DataStream data = new DataStream(stream.toByteArray());

                String type = data.readUTF();

                //Parse message
                if(type.equals("event")) {
                    long sequence = ringBuffer.next();
                    Event event = ringBuffer.get(sequence);

                    try {
                        event.setName(data.readUTF());
                        event.setEvent((MicroserviceEvent) data.readObject());
                    } catch (SerializerNotFoundException e) {
                        try {
                            getSerializer(e);
                            event.setName(data.readUTF());
                            event.setEvent((MicroserviceEvent) data.readObject());
                        } catch (IOException | ClassNotFoundException e1) {
                            Logger.log(e1);
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        Logger.log(e);
                    }

                    ringBuffer.publish(sequence);
                }
                DataStream dataStream = new DataStream(256);
                dataStream.writeUTF("OK");
                byte[] array = dataStream.array();
                socket.write(array, 0, array.length, 0);
                socket.close();
            } catch (IOException e) {
                Logger.log(e);
            }
        }

        private void getSerializer(SerializerNotFoundException e) throws IOException {
            DataStream dataStream = new DataStream(256);
            dataStream.writeUTF("serializer");
            dataStream.writeLong(e.getUid());
            byte[] array = dataStream.array();
            socket.write(array, 0, array.length, 0);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int nRead;
            boolean isStarted = false;

            while((nRead = socket.read(buffer, 0, 1024)) != -1 || !isStarted) {
                stream.write(buffer, 0, nRead);
                isStarted = true;
            }

            DataStream dataStream1 = new DataStream(stream.toByteArray());
            SerialRepository.addSerializedSerializer(dataStream1.readUTF());
        }
    }
}
