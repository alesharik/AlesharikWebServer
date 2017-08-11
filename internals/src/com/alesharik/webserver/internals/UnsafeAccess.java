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


import java.lang.reflect.Field;

abstract class UnsafeAccess {
    static final UnsafeAccess INSTANCE;

    static {
        if(unsafeSupported())
            try {
                INSTANCE = (UnsafeAccess) Class.forName("com.alesharik.webserver.internals.DefaultUnsafeAccess").newInstance();
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                throw new Error(e);
            }
        else
            INSTANCE = null;//FIXME
    }

    private static boolean unsafeSupported() {
        try {
            Class<?> clazz = Class.forName("sun.misc.Unsafe");
            Field field = clazz.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            Object o = field.get(null);
            return o != null;
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            return false;
        }
    }

    /**
     * Create object without calling constructor
     */
    public abstract Object newInstance(Class<?> clazz);
}
