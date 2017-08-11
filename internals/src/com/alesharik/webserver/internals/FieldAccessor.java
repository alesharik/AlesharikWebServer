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

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@UtilityClass
public class FieldAccessor {
    private static final Field MODIFIERS_FIELD;

    static {
        try {
            MODIFIERS_FIELD = Field.class.getDeclaredField("modifiers");
            MODIFIERS_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new Error(e);
        }
    }

    /**
     * Set field, ignoring final modifier
     */
    @SneakyThrows
    public static void setField(Object o, Object val, String name) {
        Field field = findField(o.getClass(), name);
        if(field == null)
            throw new IllegalArgumentException("Field not found!");
        field.setAccessible(true);
        setField(o, val, field);
    }

    /**
     * @param field must be accessible!
     */
    @SneakyThrows
    public static void setField(Object o, Object val, Field field) {
        if((field.getModifiers() & Modifier.FINAL) == Modifier.FINAL) {
            MODIFIERS_FIELD.set(field, field.getModifiers() & ~Modifier.FINAL);
        }
        field.set(o, val);
    }

    public static Field findField(Class<?> clazz, String name) {
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            try {
                return clazz.getField(name);
            } catch (NoSuchFieldException e1) {
                throw new IllegalStateException(e1);
            }
        }
    }
}
