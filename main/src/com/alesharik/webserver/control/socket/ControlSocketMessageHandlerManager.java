package com.alesharik.webserver.control.socket;

import com.alesharik.webserver.api.agent.classPath.ClassPathScanner;
import com.alesharik.webserver.api.agent.classPath.ListenInterface;
import com.alesharik.webserver.api.control.messaging.ControlSocketMessageHandler;
import lombok.experimental.UtilityClass;

import java.lang.reflect.ParameterizedType;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@ClassPathScanner
@UtilityClass
public class ControlSocketMessageHandlerManager {
    private static final ConcurrentHashMap<Class<?>, ControlSocketMessageHandler<?>> handlers = new ConcurrentHashMap<>();

    @ListenInterface(ControlSocketMessageHandler.class)
    public static void listenMessageHandler(Class<?> clazz) {
        Class<?> messageClass = (Class<?>) ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[0];
        try {
            handlers.put(messageClass, (ControlSocketMessageHandler<?>) clazz.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static Optional<ControlSocketMessageHandler> getHandlerFor(Class<?> clazz) {
        return Optional.ofNullable(handlers.get(clazz));
    }
}
