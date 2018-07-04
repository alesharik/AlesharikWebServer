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

package com.alesharik.webserver.module.http.addon;

import com.alesharik.webserver.module.http.http.Request;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Socket context
 */
public interface AddOnSocketContext {
    /**
     * Write bytes to the socket
     *
     * @param byteBuffer the bytes
     */
    void writeBytes(@Nonnull ByteBuffer byteBuffer);

    /**
     * Return socket channel. Do not write in it or close it
     *
     * @return socket channel
     */
    @Nonnull
    SocketChannel getChannel();

    /**
     * Set context parameter
     * @param name parameter name
     * @param o the object, <code>null</code> - remove parameter
     */
    void setParameter(@Nonnull String name, @Nullable Object o);

    /**
     * Return context parameter
     * @param name parameter name
     * @return parameter value, <code>null</code> - parameter not found
     */
    @Nullable
    Object getParameter(@Nonnull String name);

    /**
     * Close socket
     */
    void close();

    /**
     * Return handshake request
     * @return the handshake request
     */
    Request getHandshakeRequest();
}
