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

import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * This type of factory doesn't do anything expect creating object in {@link #getInstance()}
 * @param <T> the containing type
 */
@RequiredArgsConstructor
public final class NoCachedObjectFactory<T extends Recyclable> implements CachedObjectFactory<T> {
    @Nonnull
    private final Supplier<T> supplier;

    @Override
    public T getInstance() {
        return supplier.get();
    }

    @Override
    public void putInstance(T i) {

    }

    @Override
    public void refill() {

    }

    @Override
    public int getMaxCachedObjectCount() {
        return 0;
    }

    @Override
    public int getMinCachedObjectCount() {
        return 0;
    }

    @Override
    public int getCurrentCachedObjectCount() {
        return 0;
    }
}
