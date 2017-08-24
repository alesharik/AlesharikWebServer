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

package com.alesharik.webserver.api.name;

import com.alesharik.webserver.api.agent.classPath.ClassPathScanner;
import com.alesharik.webserver.api.agent.classPath.ListenAnnotation;
import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class listen and holds all classes with {@link Named} annotation
 *
 * @see Named
 */
@UtilityClass
@ClassPathScanner
public class NamedManager {
    static final AtomicInteger PARALLELISM = new AtomicInteger(2);//TODO add config value

    static final ConcurrentHashMap<Class<?>, String> entries = new ConcurrentHashMap<>();

    @ListenAnnotation(Named.class)
    static void listenNamedClass(Class<?> clazz) {
        entries.put(clazz, clazz.getAnnotation(Named.class).value());
    }

    /**
     * @param name name from {@link Named} annotation
     * @return first listened named class or null
     */
    @Nullable
    public static Class<?> getClassForName(@Nonnull String name) {
        return entries.searchEntries(PARALLELISM.get(), entry -> {
            if(name.equals(entry.getValue()))
                return entry.getKey();
            return null;
        });
    }

    /**
     * @param name name from {@link Named} annotation
     * @param type supertype or class type
     * @return first listened named class with type superclass or null
     */
    @Nullable
    public static Class<?> getClassForNameAndType(@Nonnull String name, @Nonnull Class<?> type) {
        return entries.searchEntries(PARALLELISM.get(), entry -> {
            if(name.equals(entry.getValue()) && type.isAssignableFrom(entry.getKey()))
                return entry.getKey();
            return null;
        });
    }
}
