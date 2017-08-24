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

package com.alesharik.webserver.configuration;

import com.alesharik.webserver.hook.Hook;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Hook manager manages all custom hooks for module
 */
public interface HookManager {
    /**
     * Load hooks from configuration. All last-loaded hooks must be removed
     *
     * @param hooks configured hook pairs
     */
    void loadHooks(@Nonnull List<Pair<String, Hook>> hooks);

    /**
     * Return human-readable event names
     */
    @Nonnull
    String[] getEventNames();

    /**
     * Return hook count for event
     *
     * @param eventName event name, retrieved from {@link #getEventNames()}
     */
    int getHooks(String eventName);
}
