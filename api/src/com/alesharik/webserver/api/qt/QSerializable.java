package com.alesharik.webserver.api.qt;

import javax.annotation.Nonnull;
import java.io.Serializable;

/**
 * Class, which implements this interface, can be serialized in QDataStream. If class has empty constructor, when it will
 * be called for creating object
 */
public interface QSerializable extends Serializable {
    /**
     * Write values to {@link QDataStream}
     *
     * @param stream the stream
     */
    void write(@Nonnull QDataStream stream);

    /**
     * Read values from {@link QDataStream}. All local variables will be uninitialized if class doesn't have empty constructor
     *
     * @param stream the stream
     */
    void read(@Nonnull QDataStream stream);
}
