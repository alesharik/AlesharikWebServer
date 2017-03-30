package com.alesharik.webserver.configuration.message;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.TimeUnit;

/**
 * {@link MessageStream} send and receive messages
 *
 * @param <M> {@link Message} class
 */
@ThreadSafe
public interface MessageStream<M extends Message> {
    /**
     * Send message
     *
     * @param message the message
     */
    void sendMessage(M message);

    /**
     * Get and return message sended from another end. Wait for the message if it not exists.
     *
     * @return the message
     * @throws InterruptedException if thread waiting for message is terminated
     */
    @Nonnull
    M receiveMessage() throws InterruptedException;

    /**
     * Get and return message sended from another end. Wait timeout for the message if it not exists.
     * If do not receive message and timeout is expired then send <code>null</code>
     *
     * @param timeout  waiting timeout
     * @param timeUnit units for timeout time
     * @return the message
     * @throws InterruptedException if thread waiting for message is terminated
     */
    @Nullable
    M receiveMessage(long timeout, TimeUnit timeUnit) throws InterruptedException;

    String senderModule();
}
