package com.alesharik.webserver.api.messages;

import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefixes;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

final class MessageHandlers {
    private ConcurrentHashMap<MessageHandler, ArrayList<Holder>> handlers;

    MessageHandlers() {
        handlers = new ConcurrentHashMap<>();
    }

    synchronized void registerHandler(MessageHandler handler) {
        ArrayList<Holder> holders = new ArrayList<>();

        Stream.of(handler.getClass().getMethods())
                .filter(method -> method.getAnnotation(Subscribe.class) != null)
                .forEach(method -> {
                    boolean needMessage = Stream.of(method.getParameterTypes()).anyMatch(Message.class::isAssignableFrom);
                    Holder holder = new Holder(method, needMessage, handler);
                    Subscribe annotation = method.getAnnotation(Subscribe.class);
                    Messages.registerNewListener(annotation.name(), annotation.subName(), holder);
                    holders.add(holder);
                });
        handlers.put(handler, holders);
    }

    synchronized void unregisterHandler(MessageHandler handler) {
        ArrayList<Holder> holders = handlers.remove(handler);
        if(holders != null) {
            holders.forEach(holder -> {
                Subscribe annotation = holder.method.getAnnotation(Subscribe.class);
                Messages.unregisterListener(annotation.name(), annotation.subName(), holder);
            });
        }
    }

    @Prefixes("[Messages][MessageHandler]")
    private static class Holder implements MessageListener<Message> {
        private final MessageHandler handler;
        private final Method method;
        private final boolean needMessage;

        public Holder(Method method, boolean needMessage, MessageHandler handler) {
            this.method = method;
            this.needMessage = needMessage;
            this.handler = handler;
        }

        @Override
        public void listen(Message message) {
            if(needMessage) {
                Class<?>[] params = method.getParameterTypes();
                Object[] parameters = new Object[params.length];
                for(int i = 0; i < params.length; i++) {
                    if(Message.class.isAssignableFrom(params[i])) {
                        parameters[i] = params[i];
                    } else {
                        parameters[i] = null;
                    }
                }
                try {
                    method.invoke(handler, parameters);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    Logger.log(e);
                }
            }
        }
    }
}
