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

package com.alesharik.webserver.control.dashboard.websocket;

import com.alesharik.webserver.configuration.SubModule;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * SubModule implementation for DashboardWebSocketPlugin
 */
public abstract class DashboardWebSocketPluginSubModule implements SubModule {
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    /**
     * Return plugin name
     */
    @Override
    public abstract String getName();

    @Override
    public void start() {
        isRunning.set(true);
    }

    /**
     * Executes on abnormal webSocket close code
     */
    @Override
    public void shutdownNow() {
        isRunning.set(false);
    }

    /**
     * Executes on normal webSocket close code
     */
    @Override
    public void shutdown() {
        isRunning.set(false);
    }

    @Override
    public boolean isRunning() {
        return isRunning.get();
    }
}
