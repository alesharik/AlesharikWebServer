package com.alesharik.webserver.api;

import com.alesharik.webserver.logger.Logger;
import one.nio.serial.DataStream;
import one.nio.serial.Repository;
import one.nio.serial.Serializer;
import one.nio.serial.SerializerNotFoundException;
import org.glassfish.grizzly.http.util.Base64Utils;

import java.io.IOException;
import java.io.Serializable;

/**
 * This class used for serialize and deserialize classes using one-nio method
 */
public final class SerialRepository {
    private SerialRepository() {
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
     */
    public static void addSerializedSerializer(String serialized) {
        Serializer serializer = deserializeSerializer(serialized);
        if(serializer != null) {
            Repository.provideSerializer(serializer);
        }
    }
}
