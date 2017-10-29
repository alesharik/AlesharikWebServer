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

package com.alesharik.webserver.api.utils.classloader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Throws from {@link ClassLoader} custom realisations when classloader already know class with same name
 */
public final class ClassAlreadyExistsException extends IllegalStateException {
    private static final long serialVersionUID = -478886861017579074L;

    private final String name;

    public ClassAlreadyExistsException(@Nonnull String name) {
        if(name.isEmpty())
            throw new IllegalArgumentException("Name shouldn't be empty!");

        this.name = name;
    }

    /**
     * Return problematic class name
     *
     * @return problematic class name
     */
    @Nonnull
    public String getClassName() {
        return name;
    }

    @Nonnull
    @Override
    public String getMessage() {
        return "Class " + name + " already exists!";
    }

    @Nullable
    @Override
    public Throwable getCause() {
        return null;
    }

    @Nullable
    @Override
    public Throwable initCause(Throwable cause) {
        return null;
    }
}
