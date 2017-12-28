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

package com.alesharik.webserver.base.mode;

import javax.annotation.Nonnull;

/**
 * {@link ModeClient} reflection class allows to interact with client
 *
 * @see ModeClient
 */
public interface ModeClientReflection {
    /**
     * Set client's mode
     *
     * @param mode the mode
     */
    void setMode(@Nonnull Mode mode);

    /**
     * Return client's mode
     *
     * @return the mode
     */
    @Nonnull
    Mode getMode();

    /**
     * Return the client class
     *
     * @return the client class
     */
    @Nonnull
    Class<?> getClient();
}
