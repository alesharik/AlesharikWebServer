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

package com.alesharik.webserver.serverless.notify;

import com.alesharik.webserver.serverless.ManagedThread;

import javax.annotation.Nonnull;

/**
 * Notify manager contains {@link NotifyManager} and manages all {@link ManagedThread} for calling {@link NotifyMediator}'s methods
 */
public interface NotifyManager {
    /**
     * Return the {@link NotifyMediator}
     *
     * @return the {@link NotifyMediator}
     */
    @Nonnull
    NotifyMediator getNotifier();

    /**
     * Register new thread as notify thread. It will be started/stopped automatically with agent start/stop. This thread must
     * notify discovery and heartbeat from {@link NotifyMediator}
     *
     * @param timerThread the timer thread
     */
    void registerNotifyTimer(@Nonnull ManagedThread timerThread);

    /**
     * Unregister already registered thread
     *
     * @param timerThread the thread
     */
    void unregisterNotifyTimer(@Nonnull ManagedThread timerThread);
}
