package com.alesharik.webserver.control.socket;

import com.alesharik.webserver.api.SerialRepository;
import com.alesharik.webserver.api.control.messaging.ControlSocketClientConnection;
import com.alesharik.webserver.api.control.messaging.ControlSocketConnection;
import com.alesharik.webserver.api.control.messaging.ControlSocketMessage;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import net.jcip.annotations.NotThreadSafe;
import one.nio.serial.DataStream;
import one.nio.serial.Serializer;
import one.nio.serial.SerializerNotFoundException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@NotThreadSafe
@AllArgsConstructor
abstract class AbstractControlSocketConnection implements ControlSocketConnection, Runnable {
    private static final byte MESSAGE_BYTE = 1;
    private static final byte GET_SERIALIZER_BYTE = 2;
    private static final byte SET_SERIALIZER_BYTE = 4;
    private static final byte AUTHENTICATION_REQUESTED = 8;
    private static final byte AUTHENTICATION_FAILED = 16;
    private static final byte AUTHENTICATION_SUCCESS = 32;
    private static final byte ERROR_BYTE = 64;

    private final Socket sslSocket;
    private final ControlSocketClientConnection.Authenticator authenticator;

    private final Map<Long, ByteBuffer> awaitSerializers = new ConcurrentHashMap<>();
    protected final AtomicBoolean isAuthenticated = new AtomicBoolean(false);


    @Override
    public String getRemoteHost() {
        return sslSocket.getInetAddress().getCanonicalHostName();
    }

    @Override
    public int getRemotePort() {
        return sslSocket.getPort();
    }

    @Override
    public void sendMessage(ControlSocketMessage message) throws IOException {
        DataStream dataStream = new DataStream(256);
        dataStream.writeByte(MESSAGE_BYTE);
        dataStream.writeObject(message);
        OutputStream outputStream = sslSocket.getOutputStream();
        outputStream.write(dataStream.array());
        outputStream.flush();
        dataStream.close();
    }

    @Override
    @SneakyThrows
    public void run() {
        while(!sslSocket.isConnected()) {
            Thread.sleep(1); //Wait for socket opening
        }
        authenticate();
        while(!sslSocket.isClosed()) {
            try {
                InputStream inputStream = sslSocket.getInputStream();
                byte[] buffer = new byte[1024];
                int nRead;
                ByteArrayOutputStream dataBuffer = new ByteArrayOutputStream();
                while((nRead = inputStream.read(buffer)) != 0) {
                    dataBuffer.write(buffer, 0, nRead);
                    if(inputStream.available() < 1) {
                        break;
                    }
                }

                byte[] data = dataBuffer.toByteArray();
                dataBuffer.close();

                DataStream dataStream = new DataStream(data);

                byte command = dataStream.readByte();
                switch (command) {
                    case MESSAGE_BYTE:
                        try {
                            parseMessage(dataStream);
                        } catch (SerializerNotFoundException e) {
                            awaitSerializers.put(e.getUid(), ByteBuffer.wrap(data));
                            requestSerializer(e.getUid());
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }

                        break;
                    case GET_SERIALIZER_BYTE:
                        sendSerializer(dataStream);
                        break;
                    case SET_SERIALIZER_BYTE:
                        setSerializer(dataStream);
                        break;
                    case ERROR_BYTE:
                        System.err.println("Connection from " + getRemoteHost() + ":" + getRemotePort() + " has some errors! Closing...");
                        writeError();
                        close();
                        return;
                    case AUTHENTICATION_REQUESTED:
                        parseAuthentication(dataStream);
                        break;
                    case AUTHENTICATION_FAILED:
                        System.out.println("Authentication failed on " + getRemoteHost() + ":" + getRemotePort() + "! Closing...");
                        isAuthenticated.set(false);
                        close();
                        break;
                    case AUTHENTICATION_SUCCESS:
                        isAuthenticated.set(true);
                        break;
                    default:
                        System.err.println("Unexpected control byte(" + Byte.toString(command) + ") from " + getRemoteHost() + ":" + getRemotePort() + "! Closing...");
                        writeError();
                        close();
                        return;
                }

                dataStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void authenticate() {
        if(isAuthenticated.get()) {
            return;
        }
        DataStream send = new DataStream(256);
        send.writeByte(AUTHENTICATION_REQUESTED);
        send.writeUTF(authenticator.getLogin());
        send.writeUTF(authenticator.getPassword());

        try {
            OutputStream outputStream = sslSocket.getOutputStream();
            outputStream.write(send.array());
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        send.close();
    }

    private void parseAuthentication(DataStream dataStream) {
        String login = dataStream.readUTF();
        String password = dataStream.readUTF();

        DataStream retStream = new DataStream(1);

        if(processAuthentication(login, password)) {
            retStream.writeByte(AUTHENTICATION_SUCCESS);
        } else {
            retStream.writeByte(AUTHENTICATION_FAILED);
        }

        try {
            OutputStream outputStream = sslSocket.getOutputStream();
            outputStream.write(retStream.array());
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void requestSerializer(long uid) {
        DataStream dataStream = new DataStream(9); // 1 - control byte, 8 - uid
        dataStream.writeByte(GET_SERIALIZER_BYTE);
        dataStream.writeLong(uid);

        try {
            OutputStream outputStream = sslSocket.getOutputStream();
            outputStream.write(dataStream.array());
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        dataStream.close();
    }

    private void parseMessage(DataStream dataStream) throws IOException, ClassNotFoundException {
        ControlSocketMessage message = (ControlSocketMessage) dataStream.readObject();
        parseMessageObject(message);
    }

    protected abstract boolean processAuthentication(String login, String password);

    protected abstract void parseMessageObject(ControlSocketMessage controlSocketMessage);

    private void sendSerializer(DataStream dataStream) {
        try {
            long id = dataStream.readLong();
            String retData = SerialRepository.serializeSerializer(id);

            DataStream ret = new DataStream(retData.length() + 1); // 1 for control byte
            ret.writeByte(SET_SERIALIZER_BYTE);
            ret.writeUTF(retData);

            OutputStream outputStream = sslSocket.getOutputStream();
            outputStream.write(ret.array());
            outputStream.flush();

            ret.close();
        } catch (IOException e) {
            writeError();
            e.printStackTrace();
        }
    }

    private void setSerializer(DataStream dataStream) {
        String data = dataStream.readUTF();
        Serializer serializer = SerialRepository.addSerializedSerializer(data);
        if(awaitSerializers.containsKey(serializer.uid())) {
            ByteBuffer byteBuffer = awaitSerializers.get(serializer.uid());
            DataStream msgStream = new DataStream(byteBuffer.array());
            msgStream.readByte(); //Remove command byte
            try {
                parseMessage(msgStream);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            msgStream.close();
            byteBuffer.clear();
            awaitSerializers.remove(serializer.uid());
        }
    }

    private void writeError() {
        try {
            OutputStream outputStream = sslSocket.getOutputStream();
            DataStream dataStream = new DataStream(1);
            dataStream.writeByte(ERROR_BYTE);
            outputStream.write(dataStream.array());
            outputStream.flush();
            dataStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            sslSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isClosed() {
        return sslSocket.isClosed();
    }

    public boolean isConnected() {
        return !sslSocket.isClosed() && sslSocket.isConnected() && isAuthenticated.get();
    }
}
