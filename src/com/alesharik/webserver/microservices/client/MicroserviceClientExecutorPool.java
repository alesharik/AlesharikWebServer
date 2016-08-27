package com.alesharik.webserver.microservices.client;

import com.alesharik.webserver.api.SerialRepository;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.microservices.api.MicroserviceEvent;
import one.nio.net.Socket;
import one.nio.serial.DataStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ForkJoinPool;

class MicroserviceClientExecutorPool {
    private ForkJoinPool pool;

    public MicroserviceClientExecutorPool(MicroserviceClient.WorkingMode mode) {
        if(mode == MicroserviceClient.WorkingMode.SIMPLE) {
            pool = new ForkJoinPool(2);
        } else {
            pool = new ForkJoinPool(10);
        }
    }

    public void send(String address, String microserviceName, MicroserviceEvent message) throws IOException {
        DataStream stream = new DataStream(256);
        stream.writeUTF("event");
        stream.writeUTF(microserviceName);
        stream.writeObject(message);
        pool.submit(new Send(stream.array(), address));
    }

    private static class Send implements Runnable {
        private byte[] array;
        private String[] address;

        public Send(byte[] array, String address) {
            this.array = array;
            this.address = address.split(":");
        }

        @Override
        public void run() {
            try {
                Socket socket = Socket.create();
                socket.connect(address[0], Integer.parseInt(address[1]));
                socket.write(array, 0, array.length, 0);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int nRead = 1024;

                while(nRead >= 1024) {
                    nRead = socket.read(buffer, 0, 1024);
                    stream.write(buffer, 0, nRead);
                }

                DataStream stream1 = new DataStream(stream.toByteArray());
                String s = stream1.readUTF();
                if(s.equals("serializer")) {
                    DataStream dataStream = new DataStream(256);
                    dataStream.writeUTF(SerialRepository.serializeSerializer(stream1.readLong()));
                    byte[] arr = dataStream.array();
                    socket.write(arr, 0, arr.length, 0);

                    s = waitMessage(socket);
                }
                if(!s.equals("OK")) {
                    Logger.log("Wat");
                }

                socket.close();
            } catch (IOException e) {
                Logger.log(e);
            }
        }

        private String waitMessage(Socket socket) throws IOException {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int nRead = 1024;

            while(nRead >= 1024) {
                nRead = socket.read(buffer, 0, 1024);
                stream.write(buffer, 0, nRead);
            }

            DataStream stream1 = new DataStream(stream.toByteArray());
            return stream1.readUTF();
        }
    }
}
