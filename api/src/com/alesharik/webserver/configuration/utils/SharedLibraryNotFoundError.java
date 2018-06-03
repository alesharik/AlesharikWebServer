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

package com.alesharik.webserver.configuration.utils;

import javax.annotation.Nonnull;

/**
 * This exception will be thrown if server can't find shared library
 */
public final class SharedLibraryNotFoundError extends Error {
    private static final long serialVersionUID = -8068735485106424310L;

    /**
     * @param libName library name
     */
    public SharedLibraryNotFoundError(@Nonnull String libName) {
        super("Shared Library " + libName + " not found!");
    }
}
