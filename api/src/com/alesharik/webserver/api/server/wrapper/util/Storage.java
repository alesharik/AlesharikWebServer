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

package com.alesharik.webserver.api.server.wrapper.util;

import com.alesharik.webserver.api.server.wrapper.http.Request;

import javax.annotation.Nonnull;

/**
 * Storage store session data
 */
public interface Storage {
    /**
     * Storage factory create {@link Storage} implementation instances
     */
    interface Factory {
        /**
         * Create new {@link Storage} instance for request
         *
         * @param request the request
         * @return new storage
         */
        @Nonnull
        Storage newInstance(@Nonnull Request request);
    }
}