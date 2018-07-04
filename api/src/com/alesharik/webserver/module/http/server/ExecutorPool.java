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

import com.alesharik.webserver.extension.module.Shutdown;
import com.alesharik.webserver.extension.module.ShutdownNow;
import com.alesharik.webserver.extension.module.Start;
import com.alesharik.webserver.extension.module.layer.SubModule;
import com.alesharik.webserver.module.http.server.mx.ExecutorPoolMXBean;
import com.alesharik.webserver.module.http.server.socket.ServerSocketWrapper;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Future;

/**
 * ExecutorPool is thread pool for HTTP server. It has 2 types of threads: selector and worker.
 * Selector threads are using for building, validating and routing requests.
 * Worker threads handle filter chains and {@link com.alesharik.webserver.module.http.bundle.HttpHandlerBundle}s.
 * ExecutorPool can forget about tasks, published when it is not running.
 * HTTP server use @{@link com.alesharik.webserver.api.name.Named} system for find used-defined executor. This means, that you must give unique name for your executor realisation with @{@link com.alesharik.webserver.api.name.Named} system.
 * ExecutorPool realisation MUST have public constructor with 3 parameters(first - thread count in selector pool, second - thread count in worker pool, {@link ThreadGroup} - thread group for all threads)
 */
@ThreadSafe
@SubModule("executor-pool")
public interface ExecutorPool extends ExecutorPoolMXBean {
    void setSelectorContexts(SelectorContext.Factory factory);

    /**
     * Add socket to one of selector contexts
     */
    void selectSocket(SelectableChannel socket, SocketChannel socketChannel, ServerSocketWrapper.SocketManager socketManager);

    <T, K> Future<T> submitWorkerTask(@Nonnull BatchingForkJoinTask<K, T> task);

    void executeWorkerTask(@Nonnull BatchingRunnableTask task);

    @Start
    void start();

    @Shutdown
    void shutdown();

    @ShutdownNow
    void shutdownNow();
}
