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

package com.alesharik.webserver.module.socket;

import com.alesharik.webserver.api.agent.classPath.ClassPathScanner;
import com.alesharik.webserver.api.agent.classPath.ListenInterface;
import com.alesharik.webserver.module.control.messaging.ControlSocketMessageHandler;
import com.alesharik.webserver.module.control.messaging.WireControlSocketMessage;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class holds all {@link ControlSocketMessageHandler}s and listen it
 */
@ClassPathScanner
@UtilityClass
public class ControlSocketMessageHandlerManager {
    private static final ConcurrentHashMap<Class<?>, ControlSocketMessageHandler> handlers = new ConcurrentHashMap<>();

    @ListenInterface(ControlSocketMessageHandler.class)
    static void listenMessageHandler(Class<?> clazz) {
        if(!clazz.isAnnotationPresent(WireControlSocketMessage.class)) {
            System.err.println("Class " + clazz + " doesn't have WireControlSocketMessage annotation! Skipping...");
            return;
        }

        Class<?> messageClass = clazz.getAnnotation(WireControlSocketMessage.class).value();
        try {
            Constructor<?> constructor = clazz.getConstructor();
            constructor.setAccessible(true);
            handlers.put(messageClass, (ControlSocketMessageHandler) constructor.newInstance());
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * Return handler for message class
     *
     * @param clazz message class
     */
    public static Optional<ControlSocketMessageHandler> getHandlerFor(Class<?> clazz) {
        return Optional.ofNullable(handlers.get(clazz));
    }
}
