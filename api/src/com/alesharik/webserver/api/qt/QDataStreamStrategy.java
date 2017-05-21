package com.alesharik.webserver.api.qt;

import sun.misc.Unsafe;

public interface QDataStreamStrategy {
    String readString(Unsafe unsafe, QDataStream dataStream);

    void writeString(Unsafe unsafe, String s, QDataStream dataStream);

    void writeObject(Unsafe unsafe, QSerializable serializable, QDataStream stream);

    <T extends QSerializable> T readObject(Unsafe unsafe, Class<?> clazz, QDataStream stream) throws InstantiationException;
}
