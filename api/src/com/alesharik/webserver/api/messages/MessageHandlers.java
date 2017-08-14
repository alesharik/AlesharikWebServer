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

    void registerHandler(MessageHandler handler) {
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

    boolean contains(MessageHandler handler) {
        return handlers.containsKey(handler);
    }

    void unregisterHandler(MessageHandler handler) {
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
            method.setAccessible(true);
            if(needMessage) {
                Class<?>[] params = method.getParameterTypes();
                Object[] parameters = new Object[params.length];
                for(int i = 0; i < params.length; i++) {
                    if(Message.class.isAssignableFrom(params[i])) {
                        parameters[i] = message;
                    } else {
                        parameters[i] = null;
                    }
                }
                try {
                    method.invoke(handler, parameters);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    Logger.log(e);
                }
            } else {
                try {
                    method.invoke(handler);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    Logger.log(e);
                }
            }
        }

        @Override
        public boolean equals(Object o) {
            if(this == o) return true;
            if(!(o instanceof Holder)) return false;

            Holder holder = (Holder) o;

            if(needMessage != holder.needMessage) return false;
            if(handler != null ? !handler.equals(holder.handler) : holder.handler != null) return false;
            return method != null ? method.equals(holder.method) : holder.method == null;
        }

        @Override
        public int hashCode() {
            int result = handler != null ? handler.hashCode() : 0;
            result = 31 * result + (method != null ? method.hashCode() : 0);
            result = 31 * result + (needMessage ? 1 : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Holder{" +
                    "handler=" + handler +
                    ", method=" + method +
                    ", needMessage=" + needMessage +
                    '}';
        }
    }
}
