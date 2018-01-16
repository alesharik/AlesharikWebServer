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

package com.alesharik.webserver.api.server.wrapper.addon;

import com.alesharik.webserver.api.agent.classPath.ClassPathScanner;
import com.alesharik.webserver.api.agent.classPath.ListenAnnotation;
import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ClassPathScanner
@UtilityClass
@Deprecated
public class AddOnManager {
    private static final Map<String, Class<?>> addons = new ConcurrentHashMap<>();

    @ListenAnnotation(HttpServerAddOn.class)
    static void listenAddon(Class<?> clazz) {
        HttpServerAddOn annotation = clazz.getAnnotation(HttpServerAddOn.class);

        addons.put(annotation.value(), clazz);
    }

    /**
     * Return addon for name
     *
     * @param name          addon name
     * @return new Addon instance or null
     * @throws RuntimeException         wrapper for all reflections exceptions
     * @throws ClassCastException       if addon class cannot be cast to cast class
     */
    @Nullable
    public static Addon getAddonForName(@Nonnull String name) {
        if(!addons.containsKey(name))
            return null;

        try {
            Constructor<?> constructor = getConstructor(name);
            constructor.setAccessible(true);
            return (Addon) constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    protected static Constructor<?> getConstructor(String name) {
        try {
            return addons.get(name).getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            try {
                return addons.get(name).getConstructor();
            } catch (NoSuchMethodException e1) {
                throw new RuntimeException(e1);
            }
        }
    }
}
