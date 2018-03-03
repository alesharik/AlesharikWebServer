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

import com.alesharik.webserver.serverless.exception.IllegalMessageException;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * The message factory creates messages from byte array
 *
 * @param <M> message type
 */
public interface MessageFactory<M extends Message> {
    /**
     * Parse message from byte array
     *
     * @param data the byte array
     * @return the parsed message
     * @throws IllegalMessageException if the byte array contains illegal message data
     */
    @Nonnull
    M parse(@Nonnull byte[] data) throws IllegalMessageException;

    /**
     * Return bound message ID
     *
     * @return bound message ID
     */
    @Nonnegative
    int getMessageId();
}
