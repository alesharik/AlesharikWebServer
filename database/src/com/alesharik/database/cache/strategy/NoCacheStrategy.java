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

package com.alesharik.database.cache.strategy;

import javax.annotation.Nonnull;

/**
 * This caching strategy does nothing
 */
public final class NoCacheStrategy implements CachingStrategy {
    static final NoCacheStrategy INSTANCE = new NoCacheStrategy();

    private NoCacheStrategy() {
    }

    @Override
    public void addListener(Listener listener) {

    }

    @Override
    public void removeListener(Listener listener) {

    }

    @Override
    public void created(Object o) {

    }

    @Override
    public void updated(Object o) {

    }

    @Override
    public void activate(@Nonnull Object o) {

    }

    @Override
    public void reset() {

    }
}
