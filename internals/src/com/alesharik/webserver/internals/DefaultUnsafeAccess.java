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

package com.alesharik.webserver.internals;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

final class DefaultUnsafeAccess extends UnsafeAccess {
    private static final Unsafe U;

    static {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            U = (Unsafe) theUnsafe.get(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new UnsafeAccessError(e);
        }
    }

    public DefaultUnsafeAccess() {
    }

    @Override
    public Object newInstance(Class<?> clazz) {
        try {
            return U.allocateInstance(clazz);
        } catch (InstantiationException e) {
            throw new ClassInstantiationException(e);
        }
    }

    @Override
    public int pageSize() {
        return U.pageSize();
    }
}
