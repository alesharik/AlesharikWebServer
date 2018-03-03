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

package com.alesharik.webserver.serverless.message;

import com.alesharik.webserver.serverless.RemoteAgent;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Wraps {@link com.alesharik.webserver.serverless.transport.Session} to send/receive messages. All unknown messages will be ignored
 */
public interface MessageChannel {
    /**
     * Send the message to remote agent
     *
     * @param message the message
     */
    void sendMessage(@Nonnull Message message);

    /**
     * Add new message reader
     *
     * @param reader the reader
     */
    void addReader(@Nonnull Reader reader);

    /**
     * Remove the message reader
     *
     * @param reader the reader
     */
    void removeReader(@Nonnull Reader reader);

    /**
     * Close the channel
     */
    void close();

    /**
     * Return the channel context
     *
     * @return the channel context
     */
    @Nonnull
    Context getContext();

    /**
     * Reads messages from the channel
     */
    interface Reader {
        /**
         * Read the message
         *
         * @param channel the channel
         * @param message the message
         */
        void read(@Nonnull MessageChannel channel, @Nonnull Message message);
    }

    /**
     * The message context
     */
    interface Context {
        /**
         * Return thread-safe KV storage. All data will be cleared after channel close
         *
         * @return thread-safe KV storage. All data will be cleared after channel close
         */
        @Nonnull
        Map<String, Object> getDataStorage();

        /**
         * Return the bound agent
         *
         * @return the bound agent
         */
        @Nonnull
        RemoteAgent getAgent();
    }
}
