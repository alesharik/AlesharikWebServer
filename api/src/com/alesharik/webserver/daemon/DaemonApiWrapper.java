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

package com.alesharik.webserver.daemon;

import javax.annotation.Nullable;

/**
 * Wrapper wraps daemon api for keeping api consistency
 */
@Deprecated
public abstract class DaemonApiWrapper {
    /**
     * Return {@link DaemonApi} or <code>null</code> if it is'n found
     */
    @Nullable
    public abstract DaemonApi get();

    /**
     * Return {@link T} or <code>null</code> if it is'n found
     *
     * @param cast api cast
     */
    public <T> T get(Class<T> cast) {
        return cast.cast(get());
    }

    /**
     * Return <code>true</code> if API instance available, overwise false
     */
    public abstract boolean available();
}
