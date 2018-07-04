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

package com.alesharik.webserver.module.http.server.socket;

import com.alesharik.webserver.configuration.config.lang.element.ConfigurationObject;
import com.alesharik.webserver.extension.module.layer.SubModule;
import com.alesharik.webserver.extension.module.meta.ScriptElementConverter;
import com.alesharik.webserver.module.http.server.CloseSocketException;
import com.alesharik.webserver.module.http.server.ExecutorPool;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * ServerSocketWrapper used by HTTP server for server socket management. Realisations are specified by user in config. All
 * realisation classes must be named with @{@link com.alesharik.webserver.api.name.Named}.
 * Realisation classes also must have empty public constructor!
 */
@SubModule("socket")
public interface ServerSocketWrapper {
    /**
     * Parse module configuration
     *
     * @param element the configuration element
     */
    void parseConfig(@Nullable ConfigurationObject element, @Nonnull ScriptElementConverter scriptElementConverter);

    ServerSocketChannel getChannel();

    SocketManager getSocketManager();

    /**
     * Register selector for {@link java.nio.channels.SelectionKey#OP_ACCEPT} key
     */
    default void registerSelector(Selector selector) {
        try {
            getChannel().register(selector, SelectionKey.OP_ACCEPT);
        } catch (ClosedChannelException e) {
            throw new CloseSocketException();
        }
    }

    default void setExecutorPool(ExecutorPool executorPool) {
    }

    /**
     * Manage real {@link java.nio.channels.SocketChannel}
     */
    interface SocketManager {

        /**
         * Called before first socket read. Used to perform handshakes
         */
        void init(SocketChannel socketChannel) throws IOException;

        void close(SocketChannel socketChannel) throws IOException;

        void read(SocketChannel socketChannel, ByteBuffer byteBuffer) throws IOException;

        void write(SocketChannel socketChannel, ByteBuffer data, SocketWriter writer) throws IOException;

        default boolean isSecure(SocketChannel socketChannel) {
            return false;
        }
    }
}
