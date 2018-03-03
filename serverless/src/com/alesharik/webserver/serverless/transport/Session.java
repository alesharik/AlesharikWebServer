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

package com.alesharik.webserver.serverless.transport;

import com.alesharik.webserver.serverless.exception.AgentException;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;

/**
 * Session wraps socket
 */
public interface Session {
    /**
     * Return session unique ID
     *
     * @return unique ID
     */
    @Nonnull
    UUID getId();

    /**
     * Return thread-safe KV storage for all session. Contents will be released after session close
     *
     * @return the storage
     */
    @Nonnull
    Map<String, Object> getDataStorage();

    /**
     * Add the reader to the session
     *
     * @param reader the reader
     */
    void addReader(@Nonnull Reader reader);

    /**
     * Remove the reader from the session
     *
     * @param reader the reader
     */
    void removeReader(@Nonnull Reader reader);

    /**
     * Add the listener to the session
     *
     * @param listener the listener
     */
    void addListener(@Nonnull Listener listener);

    /**
     * Remove the listener from the session
     *
     * @param listener the listener
     */
    void removeListener(@Nonnull Listener listener);

    /**
     * Write byte array to the socket
     *
     * @param data the data to write
     */
    void write(@Nonnull byte[] data);

    /**
     * Close the socket and release all data. Must be called after {@link #open()}
     */
    void close() throws AgentException;

    /**
     * Open the socket. Must be called before any {@link #write(byte[])} operations
     */
    void open() throws AgentException;

    /**
     * The socket reader
     */
    interface Reader {
        /**
         * Read data from socket
         *
         * @param session current session
         * @param data    received data
         */
        void read(@Nonnull Session session, @Nonnull byte[] data);
    }

    /**
     * Listen all socket events
     */
    interface Listener {
        /**
         * Called after socket is opened
         *
         * @param session current session
         */
        void onOpen(@Nonnull Session session);

        /**
         * Called after socket is closed
         *
         * @param session current session
         */
        void onClose(@Nonnull Session session);

        /**
         * Called after socket receive data
         *
         * @param session current session
         * @param data    real data array. All modifications will reflect on readers
         */
        void onRead(@Nonnull Session session, @Nonnull byte[] data);

        /**
         * Called before socket send data
         *
         * @param session current session
         * @param data    real data array. All modifications will reflect on sending data
         */
        byte[] onWrite(@Nonnull Session session, @Nonnull byte[] data);
    }
}
