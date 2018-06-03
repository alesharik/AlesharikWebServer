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

package com.alesharik.webserver.configuration.module;

import javax.annotation.Nonnull;

/**
 * Links Shared Library to the module
 */
public @interface Dependency {
    /**
     * Shared library name
     */
    @Nonnull
    String value();

    /**
     * Shared library version pattern. All versions are starting with equality operators(<, >, <=, >=, =)(optional) and ends with version string(numbers, separated by dots)
     *
     * @return shared library version pattern
     */
    @Nonnull
    String version() default "";

    /**
     * If required library is't found, server will throw an error. If optional library is't found, server will ignore it
     *
     * @return <code>true</code> - dependency is optional, <code>false</code> - dependency is required
     */
    boolean optional() default false;
}
