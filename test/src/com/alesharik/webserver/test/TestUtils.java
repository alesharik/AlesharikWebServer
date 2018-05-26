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

package com.alesharik.webserver.test;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

/**
 * USE ONLY FOR TESTING PURPOSES!
 */
@UtilityClass
public final class TestUtils {
    /**
     * Check if class is utility class
     *
     * @param clazz the class
     */
    public static void assertUtilityClass(Class<?> clazz) {
        if(!Modifier.isFinal(clazz.getModifiers())) {
            throw new AssertionError("Utility class must be final!");
        }
        Constructor<?>[] constructors = ArrayUtils.addAll(clazz.getConstructors(), clazz.getDeclaredConstructors());
        if(constructors.length == 0)
            throw new AssertionError("Utility class must have no public/protected constructors!");
        for(Constructor<?> constructor : constructors) {
            if(!Modifier.isPrivate(constructor.getModifiers())) {
                throw new AssertionError("Utility class must have no public/protected constructors!");
            }
            if(constructor.getParameterCount() > 0) {
                throw new AssertionError("Utility class can't have constructor with arguments!");
            }
            constructor.setAccessible(true);
            try {
                constructor.newInstance();
            } catch (IllegalAccessException | InstantiationException e) {
                throw new AssertionError(e);
            } catch (InvocationTargetException e) {
                if(!(e.getCause() instanceof UnsupportedOperationException)) {
                    throw new AssertionError("Utility class constructor must throw UnsupportedOperationException!");
                }
            }
        }
    }
}
