package com.alesharik.webserver.api;

import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefix;
import lombok.SneakyThrows;
import one.nio.serial.DataStream;
import one.nio.serial.Repository;
import one.nio.serial.Serializer;
import one.nio.serial.SerializerNotFoundException;
import org.glassfish.grizzly.http.util.Base64Utils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class used for serialize and deserialize classes using one-nio method
 */
//TODO use own serialization
public final class SerialRepository {
    private static final ReentrantLock snapshotLock = new ReentrantLock();
    private volatile static File snapshotFile;
    private volatile static long updateTime = 1000;
    private static final AtomicBoolean isSnapshotEnabled = new AtomicBoolean(true);

    static {
        snapshotLock.lock();
        SnapshotThread snapshotThread = new SnapshotThread();
        snapshotThread.start();
    }

    private SerialRepository() {
    }

    /**
     * DO NOT USE IT
     */
    public static void setSnapshotFile(@Nonnull File file) {
        snapshotFile = file;
        snapshotLock.unlock();
    }

    /**
     * DO NOT USE IT
     */
    public static void setUpdateTime(long time) {
        updateTime = time;
    }

    /**
     * DO NOT USE IT
     */
    public static void loadSnapshotFile(File file) {
        try {
            Repository.loadSnapshot(Files.readAllBytes(file.toPath()));
        } catch (IOException | ClassNotFoundException e) {
            Logger.log(e);
        }
    }

    /**
     * DO NOT USE IT
     */
    public static void snapshotEnabled(boolean is) {
        isSnapshotEnabled.set(is);
    }

    /**
     * Deserialize string into class instance before serialization
     *
     * @param serialized base64 serialized {@link DataStream}
     * @return class instance
     * @throws SerializerNotFoundException throw if {@link Repository} has no serializer to deserialize string.
     *                                     For deserialization you need to request serializer with specific uid. Exception contains needed uid.
     */
    public static Object deserialize(String serialized) throws SerializerNotFoundException {
        try {
            DataStream stream = new DataStream(Base64Utils.decodeFast(serialized));
            return stream.readObject();
        } catch (SerializerNotFoundException e) {
            throw new SerializerNotFoundException(e.getUid());
        } catch (IOException | ClassNotFoundException e) {
            Logger.log(e);
        }
        return null;
    }

    /**
     * Serialize object
     *
     * @param object class instance ot serialize
     * @return base64 encoded {@link DataStream} with object
     */
    public static String serialize(Serializable object) {
        try {
            DataStream stream = new DataStream(256);
            stream.writeObject(object);
            return Base64Utils.encodeToString(stream.array(), false);
        } catch (IOException e) {
            Logger.log(e);
        }
        return "";
    }

    /**
     * Serialize specific serializer for send to other server
     *
     * @param uid uid of serializer
     * @return base64 encoded {@link DataStream} with serializer
     * @throws SerializerNotFoundException throw if serializer not found
     */
    public static String serializeSerializer(long uid) throws SerializerNotFoundException {
        try {
            DataStream dataStream = new DataStream(256);
            dataStream.writeObject(Repository.requestSerializer(uid));
            return Base64Utils.encodeToString(dataStream.array(), false);
        } catch (IOException e) {
            Logger.log(e);
        }
        return "";
    }

    /**
     * Deserialize serializer
     *
     * @param serialized base64 {@link DataStream} with serializer
     * @return serializer instance
     */
    public static Serializer deserializeSerializer(String serialized) {
        try {
            DataStream dataStream = new DataStream(Base64Utils.decodeFast(serialized));
            return (Serializer) dataStream.readObject();
        } catch (ClassNotFoundException | IOException e) {
            Logger.log(e);
        }
        return null;
    }

    /**
     * Deserialize and add serializer to {@link Repository}
     *
     * @param serialized base64 {@link DataStream} with serializer
     * @return new serializer
     */
    public static Serializer addSerializedSerializer(String serialized) {
        Serializer serializer = deserializeSerializer(serialized);
        if(serializer != null) {
            Repository.provideSerializer(serializer);
        }
        return serializer;
    }

    @Prefix("[SnapshotThread]")
    private static final class SnapshotThread extends Thread {
        private RandomAccessFile file;

        public SnapshotThread() {
            setName("SerializerRepositorySnapshotThread");
            setDaemon(true);
        }

        @Override
        @SneakyThrows
        public void run() {
            snapshotLock.lock();
            try {
                if(snapshotFile == null) {
                    Logger.log("Snapshot file not selected! Stopping snapshot thread...");
                    return;
                } else {
                    file = new RandomAccessFile(snapshotFile, "rw");
                }
                //noinspection InfiniteLoopStatement because we exit only on vm shutdown
                while(true) {
                    file.setLength(0);
                    file.write(Repository.saveSnapshot());
                    try {
                        Thread.sleep(updateTime);
                    } catch (InterruptedException e) {
                        Logger.log("Snapshot thread stopped!");
                    }
                }
            } finally {
                snapshotLock.unlock();
                file.close();
            }
        }
    }
}
