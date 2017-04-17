package com.alesharik.webserver.control.socket;

import com.alesharik.webserver.api.agent.classPath.ClassPathScanner;
import com.alesharik.webserver.api.agent.classPath.ListenInterface;
import com.alesharik.webserver.api.control.messaging.ControlSocketMessageHandler;
import com.alesharik.webserver.api.control.messaging.WireControlSocketMessage;
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
    public static void listenMessageHandler(Class<?> clazz) {
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
