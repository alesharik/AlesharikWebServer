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

import org.w3c.dom.Element;

import javax.annotation.Nonnull;

/**
 * Hook factories create user-defined hooks from configuration.
 * Hook factories are singletons.
 * If factory has no no-args constructor, it will be created via Unsafe
 */
public interface HookFactory {
    /**
     * Create hook from configuration
     *
     * @param config hook configuration
     * @param name   hook name(defined by user)
     * @return hook with no group
     * @throws com.alesharik.webserver.exceptions.error.ConfigurationParseError if config error happens
     */
    @Nonnull
    Hook create(@Nonnull Element config, @Nonnull String name);

    /**
     * Return hook factory unique name
     */
    @Nonnull
    String getName();
}
