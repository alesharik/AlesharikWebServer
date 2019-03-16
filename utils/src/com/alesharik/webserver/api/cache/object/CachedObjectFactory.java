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

package com.alesharik.webserver.api.cache.object;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 * This interface describes all cached object factories. Cached object factory is a thing that will try to reuse
 * existing classes instead of creating new ones. But you must explicitly return class to the factory by {@link #putInstance(Recyclable)}
 * after the moment when the object can be recycled and reused
 *
 * @param <T> the object type
 */
@ThreadSafe
public interface CachedObjectFactory<T extends Recyclable> extends CachedObjectFactoryMXBean {
    /**
     * Return cached or new object instance
     * @return cached or new object instance
     */
    @Nonnull
    T getInstance();

    /**
     * Put instance for recycling
     * @param i the instance
     */
    void putInstance(@Nonnull T i);

    /**
     * Refill object factory with new instances
     */
    void refill();
}
