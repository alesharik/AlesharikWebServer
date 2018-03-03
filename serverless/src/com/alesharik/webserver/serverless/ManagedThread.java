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

package com.alesharik.webserver.serverless;

/**
 * Managed thread allows to control itself
 */
public interface ManagedThread {
    /**
     * Start the thread. Can be called multiple times
     */
    void startThread();

    /**
     * Shutdown the thread
     */
    void shutdown();

    /**
     * Shutdown the thread immediately
     */
    void shutdownNow();

    /**
     * Return <code>true</code> if thread is running
     *
     * @return <code>true</code> if thread is running
     */
    boolean isRunning();
}
