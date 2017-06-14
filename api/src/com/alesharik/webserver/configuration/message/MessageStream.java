/*
 *  This file is part of AlesharikWebServer.
 *
 *     AlesharikWebServer is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     AlesharikWebServer is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with AlesharikWebServer.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

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
