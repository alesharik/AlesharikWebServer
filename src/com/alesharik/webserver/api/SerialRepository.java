package com.alesharik.webserver.api;

import com.alesharik.webserver.logger.Logger;
import one.nio.serial.DataStream;
import one.nio.serial.Repository;
import one.nio.serial.Serializer;
import one.nio.serial.SerializerNotFoundException;
import org.glassfish.grizzly.http.util.Base64Utils;

import java.io.IOException;

public class SerialRepository {
    public static Object deserialize(String message) throws SerializerNotFoundException {
        try {
            DataStream stream = new DataStream(Base64Utils.decodeFast(message));
            return stream.readObject();
        } catch (SerializerNotFoundException e) {
            throw new SerializerNotFoundException(e.getUid());
        } catch (IOException | ClassNotFoundException e) {
            Logger.log(e);
        }
        return null;
    }

    public static String serialize(Object object) {
        try {
            DataStream stream = new DataStream(256);
            stream.writeObject(object);
            return Base64Utils.encodeToString(stream.array(), false);
        } catch (IOException e) {
            Logger.log(e);
        }
        return "";
    }

    public static String serializeSerializer(long uid) throws SerializerNotFoundException {
        try {
            DataStream dataStream = new DataStream(256);
            dataStream.writeObject(Repository.requestSerializer(uid));
            return Base64Utils.encodeToString(dataStream.array(), false)
        } catch (IOException e) {
            Logger.log(e);
        }
        return "";
    }

    public static Serializer deserializeSerializer(String message) {
        try {
            DataStream dataStream = new DataStream(Base64Utils.decodeFast(message));
            return (Serializer) dataStream.readObject();
        } catch (ClassNotFoundException | IOException e) {
            Logger.log(e);
        }
        return null;
    }

    public static void addSerializedSeriaizer(String message) {
        Serializer serializer = deserializeSerializer(message);
        if(serializer != null) {
            Repository.provideSerializer(serializer);
        }
    }
}
