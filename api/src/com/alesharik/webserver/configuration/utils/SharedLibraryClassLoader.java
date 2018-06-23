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

import lombok.Getter;

import javax.annotation.Nonnull;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * This classloader will load all Shared Libraries
 */
public abstract class SharedLibraryClassLoader extends URLClassLoader {
    @Getter
    @Nonnull
    private final SharedLibrary sharedLibrary;

    protected SharedLibraryClassLoader(URL url, ClassLoader parent, @Nonnull SharedLibrary sharedLibrary) {
        super(new URL[]{url}, parent);
        this.sharedLibrary = sharedLibrary;
    }
}
