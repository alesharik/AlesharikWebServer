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

package com.alesharik.webserver.module.execution.background;

import com.alesharik.webserver.configuration.Module;

import javax.annotation.concurrent.ThreadSafe;

/**
 * @see BackgroundExecutorModuleMXBean
 * @see BackgroundExecutor
 * @see Module
 */
@ThreadSafe
public interface BackgroundExecutorModule extends Module, BackgroundExecutor, BackgroundExecutorModuleMXBean {
    /**
     * Shutdown executor after all tasks completion
     */
    @Override
    void shutdown();

    /**
     * Shutdown without waiting for tasks completion
     */
    @Override
    void shutdownNow();

    /**
     * Start the threads here
     */
    @Override
    void start();
}
