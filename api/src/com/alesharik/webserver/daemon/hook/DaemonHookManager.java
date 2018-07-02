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

package com.alesharik.webserver.daemon.hook;

import com.alesharik.webserver.hook.Hook;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * This contains all hook logic. It MUST handle start and shutdown hooks!
 */
@Deprecated
public interface DaemonHookManager {
    /**
     * Register new hook
     *
     * @param event hook event name
     * @param hook  the hook
     */
    void registerHook(@Nonnull String event, @Nonnull Hook hook);

    /**
     * Return available method, not affect hook parsing process
     */
    @Nonnull
    String[] getEvents();

    /**
     * Return hook count for event
     *
     * @param event event name
     * @return positive/zero integer
     */
    int getHookCount(@Nonnull String event);

    /**
     * Return all hooks for event
     * @param event event name
     * @return unmodifiable collection
     */
    @Nonnull
    List<Hook> getHooks(@Nonnull String event);
}
