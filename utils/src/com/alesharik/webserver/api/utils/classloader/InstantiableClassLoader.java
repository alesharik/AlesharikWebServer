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

package com.alesharik.webserver.api.utils.classloader;

import com.alesharik.webserver.exception.error.UnexpectedBehaviorError;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * This classloader has {@link #newInstance(String)} method, which creates new object instances from it's names
 */
public class InstantiableClassLoader extends ClassLoader {
    public InstantiableClassLoader(ClassLoader parent) {
        super(parent);
    }

    public InstantiableClassLoader() {
    }

    /**
     * Create new class instance
     *
     * @param name class binary name
     * @return new instance or null if class not found or class doesn't have no-args constructor
     * @throws RuntimeException with exception from constructor
     */
    @Nullable
    public Object newInstance(String name) {
        try {
            Class<?> clazz = loadClass(name);
            try {
                return clazz.newInstance();
            } catch (IllegalAccessException e) {
                try {
                    Constructor<?> constructor = clazz.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    return constructor.newInstance();
                } catch (NoSuchMethodException e1) {
                    return null;
                } catch (IllegalAccessException e1) {
                    throw new UnexpectedBehaviorError("IllegalAccessException after setAccessible? Java 9 is not supported yet", e1);
                } catch (InvocationTargetException e1) {
                    throw new RuntimeException(e.getCause());
                }
            }
        } catch (ClassNotFoundException e) {
            return null;
        } catch (InstantiationException e) {
            if(e.getCause() instanceof NoSuchMethodException)
                return null;

            throw new RuntimeException(e.getCause());
        }
    }
}
