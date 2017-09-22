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

package com.alesharik.webserver.api.agent;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ClassLoaderHelper {
    /**
     * Return true if you can rescan classloader
     *
     * @param classLoader the classloader
     * @see Rescanable
     */
    public static boolean isRescanable(ClassLoader classLoader) {
        return classLoader.getClass().isAnnotationPresent(Rescanable.class);
    }

    /**
     * Return true if classloader must be ignored when checking
     *
     * @param classLoader the classloader
     * @see IgnoreClassLoader
     */
    public static boolean isIgnored(ClassLoader classLoader) {
        return classLoader.getClass().isAnnotationPresent(IgnoreClassLoader.class);
    }

    /**
     * Return true if classloader can be closed
     *
     * @param classLoader the classloader
     * @see CloseableClassLoader
     */
    public static boolean isCloseable(ClassLoader classLoader) {
        return classLoader instanceof CloseableClassLoader;
    }

    /**
     * Return true if classloader is closed
     *
     * @param classLoader the classloader
     * @see CloseableClassLoader
     */
    public static boolean isClosed(ClassLoader classLoader) {
        return classLoader instanceof CloseableClassLoader && ((CloseableClassLoader) classLoader).isClosed();
    }

    /**
     * Return true if classloader is open
     *
     * @param classLoader the classloader
     * @see CloseableClassLoader
     */
    public static boolean isOpen(ClassLoader classLoader) {
        return !(classLoader instanceof CloseableClassLoader) || !((CloseableClassLoader) classLoader).isClosed();
    }
}
