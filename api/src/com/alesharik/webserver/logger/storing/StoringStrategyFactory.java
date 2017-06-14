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

package com.alesharik.webserver.logger.storing;

import java.io.File;

/**
 * {@link com.alesharik.webserver.logger.NamedLogger} use this class for create new {@link StoringStrategy}
 *
 * @param <T> storing strategy
 */
@FunctionalInterface
public interface StoringStrategyFactory<T extends StoringStrategy> {
    /**
     * Create new instance
     * @param file log file
     * @return new instance
     */
    T newInstance(File file);
}
