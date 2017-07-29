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

import com.alesharik.webserver.api.server.wrapper.server.mx.ExecutorPoolMXBean;
import com.alesharik.webserver.configuration.SubModule;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.Future;

/**
 * ExecutorPool is thread pool for HTTP server. It has 2 types of threads: selector and worker.
 * Selector threads are using for building, validating and routing requests.
 * Worker threads handle filter chains and {@link com.alesharik.webserver.api.server.wrapper.bundle.HttpHandler}s.
 * ExecutorPool is {@link SubModule}, this means that it must have ability to start on {@link SubModule#start()}, shutdown on {@link SubModule#shutdown()} or {@link SubModule#shutdownNow()}. Also it is preferable to have unique name.
 * ExecutorPool can forget about tasks, published when it is not running.
 * HTTP server use @{@link com.alesharik.webserver.api.name.Named} system for find used-defined executor. This means, that you must give unique name for your executor realisation with @{@link com.alesharik.webserver.api.name.Named} system.
 * ExecutorPool realisation MUST have public constructor with 3 parameters(first - thread count in selector pool, second - thread count in worker pool, {@link ThreadGroup} - thread group for all threads)
 */
@ThreadSafe
public interface ExecutorPool extends ExecutorPoolMXBean, SubModule {
    <T, K> Future<T> submitSelectorTask(@Nonnull BatchingForkJoinTask<K, T> task);

    void executeSelectorTask(@Nonnull BatchingRunnableTask task);

    <T, K> Future<T> submitWorkerTask(@Nonnull BatchingForkJoinTask<K, T> task);

    void executeWorkerTask(@Nonnull BatchingRunnableTask task);
}
