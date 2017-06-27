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
import com.alesharik.webserver.api.collections.ConcurrentTripleHashMap;
import com.alesharik.webserver.api.collections.TripleHashMap;
import com.alesharik.webserver.api.server.wrapper.NetworkListenerConfiguration;
import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@ClassPathScanner
@UtilityClass
public class AddOnManager {
    private static final TripleHashMap<String, Class<?>, UseCondition> addons = new ConcurrentTripleHashMap<>();

    @ListenAnnotation(HttpServerAddOn.class)
    public static void listenAddon(Class<?> clazz) {
        try {
            HttpServerAddOn annotation = clazz.getAnnotation(HttpServerAddOn.class);

            UseCondition useCondition = annotation.condition().newInstance();

            addons.put(annotation.value(), clazz, useCondition);
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();

        }
    }

    /**
     * Return addon for name
     *
     * @param name          addon name
     * @param configuration configuration for {@link UseCondition} to check
     * @param cast          class to cast
     * @param <T>           cast
     * @return new Addon instance
     * @throws ConditionException       if condition throw an exception or deny crate new instance
     * @throws IllegalArgumentException if addon not found
     * @throws RuntimeException         wrapper for all reflections exceptions
     * @throws ClassCastException       if addon class cannot be cast to cast class
     */
    @SuppressWarnings("unchecked")
    public static <T> T getAddonForName(@Nonnull String name, @Nonnull NetworkListenerConfiguration configuration, @Nonnull Class<?> cast) {
        if(!addons.containsKey(name))
            throw new IllegalArgumentException("Addon not found!");
        UseCondition condition = addons.getAddition(name);

        try {
            if(!condition.allow(configuration))
                throw new ConditionException(condition);
        } catch (Exception e) {
            throw new ConditionException(e, condition);
        }

        try {
            Constructor<?> constructor = getConstructor(name);
            constructor.setAccessible(true);
            return (T) cast.cast(constructor.newInstance());
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
