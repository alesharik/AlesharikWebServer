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

package com.alesharik.webserver.api.serial;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * This class contains mapping for id(long) <===> {@link Class} and manages ids
 */
@ThreadSafe
public interface SerializationClassConversionMap {
    /**
     * Add conversion for id
     * @param id class id
     * @param clazz mapped class
     * @throws IllegalStateException if id is already occupied
     */
    void addConversion(long id, @Nonnull Class<?> clazz);

    /**
     * Add conversion for class
     * @param clazz the class
     * @return conversion id
     */
    default long addConversion(@Nonnull Class<?> clazz) {
        long nextId = getNextId();
        addConversion(nextId, clazz);
        return nextId;
    }

    /**
     * Finds mapping for given class
     * @param clazz the class
     * @return mapping or -1 if mapping is not found
     */
    long getConversionFor(Class<?> clazz);

    /**
     * Return next free id
     * @return next free id
     */
    long getNextId();

    /**
     * Resolve conversion for id
     * @param id the id
     * @return mapped class or <code>null</code> if mapping not found
     */
    @Nullable
    Class<?> resolveConversion(long id);

    long getOrCreateConversionFor(Class<?> clazz);
}
