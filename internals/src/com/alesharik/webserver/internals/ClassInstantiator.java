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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * This class provides class creation methods
 */
@UtilityClass
public class ClassInstantiator {
    /**
     * Instantiate class without calling it's constructor
     */
    public static Object instantiateUnsafe(Class<?> clazz) {
        return UnsafeAccess.INSTANCE.newInstance(clazz);
    }

    /**
     * Instantiate class by calling it's empty declared constructor
     */
    @SneakyThrows //IllegalAccessException will never happens
    public static Object instantiateSerialization(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (NoSuchMethodException e) {
            return null;
        } catch (InstantiationException | InvocationTargetException e) {
            throw new ClassInstantiationException(e);
        }
    }

    /**
     * Instantiate class by calling first declared constructor and passing null's to it's parameters
     */
    public static Object instantiateNullConstructor(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
            constructor.setAccessible(true);
            Object[] nulls = new Object[constructor.getParameterCount()];
            for(int i = 0; i < constructor.getParameterCount(); i++) {
                nulls[i] = null;
            }
            return constructor.newInstance(nulls);
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        } catch (InstantiationException | InvocationTargetException e) {
            throw new ClassInstantiationException(e);
        } catch (IllegalAccessException e) {
            //Never happens!
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Instantiate class by most preferable for object way. This means what it tries to create object by calling it's declared empty constructor.
     * If class doesn't have declared empty constructor, it will be instantiated by {@link #instantiateUnsafe(Class)}
     */
    @SneakyThrows
    public static Object instantiate(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (NoSuchMethodException e) {
            return instantiateUnsafe(clazz);
        } catch (InstantiationException | InvocationTargetException e) {
            throw new ClassInstantiationException(e);
        }
    }
}