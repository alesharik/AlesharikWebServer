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

package com.alesharik.webserver.logger.mx;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface NamedLoggerMXBean {
    /**
     * Return logger name
     */
    @Nonnull
    String getName();

    /**
     * Return logger storing strategy class canonical name if exists, overwise class name.
     * If logger doesn't have strategy, return empty string
     */
    @Nonnull
    String getStoringStrategy();

    /**
     * Return logger file path
     */
    @Nullable
    String getFile();

    /**
     * Return logger default prefix
     */
    @Nonnull
    String getDefaultPrefix();
}
