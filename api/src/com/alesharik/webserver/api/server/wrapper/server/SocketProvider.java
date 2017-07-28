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

package com.alesharik.webserver.api.server.wrapper.server;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * This class provides registration for server socket
 */
public interface SocketProvider {
    /**
     * Register selector for {@link java.nio.channels.SelectionKey#OP_ACCEPT} key. This must attach {@link ServerSocketWrapper} to SelectionKey!
     */
    void registerSelector(Selector selector);

    default void setExecutorPool(ExecutorPool executorPool) {

    }

    @AllArgsConstructor
    @Getter
    class ServerSocketWrapper {
        protected final ServerSocketChannel serverSocket;
        protected final SocketManager socketManager;
    }

    /**
     * Manage real {@link java.nio.channels.SocketChannel}
     */
    interface SocketManager {
        SocketManager DEFAULT = new SocketManager() {
        };

        /**
         * Called before first socket read. Used to perform handshakes
         */
        default void initSocket(SocketChannel socketChannel) {
        }

        default byte[] read(SocketChannel socketChannel) {
            return new byte[0];
        }

        default void write(SocketChannel socketChannel, byte[] data) {
        }

        default boolean hasCustomRW(SocketChannel socketChannel) {
            return false;
        }

        default void listenSocketClose(SocketChannel socketChannel) {

        }

        default boolean isSecure(SocketChannel socketChannel) {
            return false;
        }
    }
}
