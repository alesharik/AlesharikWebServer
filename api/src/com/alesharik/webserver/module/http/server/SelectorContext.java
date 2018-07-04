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

package com.alesharik.webserver.module.http.server;

import com.alesharik.webserver.module.http.server.socket.ServerSocketWrapper;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;

/**
 * This context allows HTTP server to delegate selector thread logic to {@link ExecutorPool}
 */
public interface SelectorContext {
    /**
     * Return active socket count
     */
    long getSocketCount();

    /**
     * Register new socket into selector
     */
    void registerSocket(SelectableChannel socket, SocketChannel socketChannel, ServerSocketWrapper.SocketManager manager);

    /**
     * Executes while selector thread is alive
     */
    void iteration();

    /**
     * Executes immediately return from {@link #iteration()}
     */
    void wakeup();

    void close();

    interface Factory {
        SelectorContext newInstance();
    }
}
