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

package com.alesharik.webserver.hook;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Hooks allows to listen events form server and it's plugins. Hooks config fetched form main configuration.
 * All hooks have name and group. In configuration they defined as <code>group.name</code>.
 * All hooks are singletons. If hook has no no-args constructor, it will be created via Unsafe.
 * System hook reloading not supported!
 */
public interface Hook extends Listener {
    /**
     * Returns hook name
     */
    @Nonnull
    String getName();

    /**
     * Returns hook group. Group can be nullable only for user-defined hooks!
     */
    @Nullable
    String getGroup();
}
