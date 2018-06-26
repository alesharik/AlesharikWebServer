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

package com.alesharik.webserver.extension.module.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * This class manages all {@link SharedLibrary} instances
 */
public interface SharedLibraryManager {
    /**
     * Return all shared libs
     *
     * @return all shared libs
     */
    @Nonnull
    List<SharedLibrary> getSharedLibs();

    /**
     * Return shared library by name
     *
     * @param name the library name
     * @return <code>null</code> - not found, library - found
     */
    @Nullable
    SharedLibrary getLibrary(@Nonnull String name);

    /**
     * Checks if manager has the library
     *
     * @param name           the library name
     * @param minimumVersion the library minimum version. Can be null
     * @return <code>true</code> - manager has the library, <code>false</code> - manages doesn't have the library
     */
    boolean hasLibrary(@Nonnull String name, @Nullable SharedLibraryVersion minimumVersion);

    /**
     * Return classloader for the library
     *
     * @param sharedLibrary the library
     * @return the classloader
     */
    @Nonnull
    SharedLibraryClassLoader getClassLoader(@Nonnull SharedLibrary sharedLibrary);

    /**
     * Checks if manager has the library
     *
     * @param name the library name
     * @return <code>true</code> - manager has the library, <code>false</code> - manages doesn't have the library
     */
    default boolean hasLibrary(@Nonnull String name) {
        return hasLibrary(name, null);
    }

    /**
     * Add listener to manager. All listeners must be removed at the end of owner's cycle
     *
     * @param listener the listener
     */
    void addListener(@Nonnull UpdateListener listener);

    /**
     * Remove listener
     *
     * @param listener the listener
     */
    void removeListener(@Nonnull UpdateListener listener);

    /**
     * Update listeners listen runtime updates. They will be active only after the manager loads all libraries
     */
    interface UpdateListener {
        /**
         * Triggers when library got deleted
         *
         * @param library the library
         */
        default void onLibraryDelete(@Nonnull SharedLibrary library) {
        }

        /**
         * Triggers when library get updated
         *
         * @param library the library
         */
        default void onLibraryUpdate(@Nonnull SharedLibrary library) {

        }

        /**
         * Triggers when library got added
         *
         * @param library the library
         */
        default void onLibraryAdd(@Nonnull SharedLibrary library) {

        }
    }
}
