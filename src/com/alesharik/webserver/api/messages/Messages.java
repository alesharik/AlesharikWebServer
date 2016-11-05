package com.alesharik.webserver.api.messages;

import com.alesharik.webserver.api.Utils;
import com.alesharik.webserver.logger.Logger;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class used for Message system in AlesharikWebServer. This system used for send messages between the systems.
 * The message lifecycle:<br><pre>
 *     <code>publishMessage(name, subName)</code>
 *          |
 *          ∨
 *     {@link AbstractMessageFactory} - used for generate messages
 *          |
 *          ∨
 *     {@link MessageTranslator} - used for setup messages
 *          |
 *          ∨
 *     <code>   message.clone()</code>
 *          |       |
 *          |       ---------->{@link MessageHandler} - used for listen multiple messages
 *          ∨
 *     {@link MessageListener} - used for listen messages IN NEW THREAD OR SERVICE THREAD
 * </pre>
 * The messages divided by groups: the message has the name(received as method var, is a group) and the
 * subName(received form {@link AbstractMessageFactory}, is a name of message)
 *
 * @see Message
 * @see AbstractMessageFactory
 * @see MessageTranslator
 * @see MessageListener
 * @see MessageHandler
 */
public final class Messages {
    private static final ConcurrentHashMap<String, ConcurrentHashMap<String, Holder>> factories = new ConcurrentHashMap<>();
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static final MessageHandlers handlers = new MessageHandlers();

    private Messages() {
    }

    /**
     * @param name       name of group
     * @param subName    name of message
     * @param translator the translator
     * @param <T>        the specified message
     */
    public static <T extends Message> void registerNewTranslator(String name, String subName, MessageTranslator<T> translator) {
        Utils.requireNotNullOrEmpty(name);
        Utils.requireNotNullOrEmpty(subName);
        Objects.requireNonNull(translator);

        Holder holder = getFromFactories(name, subName);
        if(holder == null) {
            throw new IllegalStateException("Please register AbstractMessageFactory at name=" + name + " and subName=" + subName);
        }
        holder.addTranslator(translator);
    }

    /**
     * @param name       name of group
     * @param subName    name of message
     * @param translator the translator
     * @param <T>        the specified message
     */
    public static <T extends Message> void unregisterTranslator(String name, String subName, MessageTranslator<T> translator) {
        Utils.requireNotNullOrEmpty(name);
        Utils.requireNotNullOrEmpty(subName);
        Objects.requireNonNull(translator);

        Holder holder = getFromFactories(name, subName);
        if(holder == null) {
            throw new IllegalStateException("Please register AbstractMessageFactory at name=" + name + " and subName=" + subName);
        }
        holder.removeTranslator(translator);
    }

    /**
     * @param name     name of group
     * @param subName  name of message
     * @param listener the listener
     * @param <T>      the specified message
     */
    public static <T extends Message> void registerNewListener(String name, String subName, MessageListener<T> listener) {
        Utils.requireNotNullOrEmpty(name);
        Utils.requireNotNullOrEmpty(subName);
        Objects.requireNonNull(listener);

        Holder holder = getFromFactories(name, subName);
        if(holder == null) {
            throw new IllegalStateException("Please register AbstractMessageFactory at name=" + name + " and subName=" + subName);
        }
        holder.addListener(listener);
    }

    /**
     * @param name     name of group
     * @param subName  name of message
     * @param listener the listener
     * @param <T>      the specified message
     */
    public static <T extends Message> void unregisterListener(String name, String subName, MessageListener<T> listener) {
        Utils.requireNotNullOrEmpty(name);
        Utils.requireNotNullOrEmpty(subName);
        Objects.requireNonNull(listener);

        Holder holder = getFromFactories(name, subName);
        if(holder == null) {
            throw new IllegalStateException("Please register AbstractMessageFactory at name=" + name + " and subName=" + subName);
        }
        holder.removeListener(listener);
    }

    /**
     * Create and publish the message
     *
     * @param name    name of the group
     * @param subName name of the message
     * @throws IllegalStateException if {@link Messages} don't have any {@link AbstractMessageFactory} for this message
     */
    public static synchronized <T extends Message> void publishMessage(String name, String subName) {
        Utils.requireNotNullOrEmpty(name);
        Utils.requireNotNullOrEmpty(subName);

        try {
            Holder<T> holder = getFromFactories(name, subName);
            if(holder == null) {
                throw new IllegalStateException("Please register AbstractMessageFactory at name=" + name + " and subName=" + subName);
            }
            T message = holder.getMessageFactory().newMessage();
            holder.translate(message);
            holder.listen((T) message.clone(), executor);
        } catch (CloneNotSupportedException e) {
            Logger.log(e);
        }
    }

    /**
     * Generate separated listeners who invoke subscribed methods in handler
     */
    public static void registerNewHandler(MessageHandler handler) {
        Objects.requireNonNull(handler);
        handlers.registerHandler(handler);
    }

    /**
     * Remove all generated from handler listeners
     */
    public static void unregisterHandler(MessageHandler handler) {
        Objects.requireNonNull(handler);
        handlers.unregisterHandler(handler);
    }

    /**
     * @param name name of the group
     */
    public static <T extends Message> void registerNewMessage(String name, AbstractMessageFactory<T> factory) {
        Utils.requireNotNullOrEmpty(name);
        Objects.requireNonNull(factory);

        Holder<T> holder = new Holder<>(factory);
        addToFactories(name, factory.getName(), holder);
    }


    private static void addToFactories(String name, String subName, Holder holder) {
        factories.putIfAbsent(name, new ConcurrentHashMap<>());
        factories.get(name).put(subName, holder);
    }

    private static boolean hasInFactories(String name, String subName) {
        return factories.get(name) != null || factories.get(name).containsKey(subName);
    }

    /**
     * @return Holder or null
     */
    private static Holder getFromFactories(String name, String subName) {
        if(hasInFactories(name, subName)) {
            return factories.get(name).get(subName);
        } else {
            return null;
        }
    }

    private static class Holder<T extends Message> {
        private final AbstractMessageFactory<T> messageFactory;
        private ArrayList<MessageListener<? super T>> listeners;
        private ArrayList<MessageTranslator<? super T>> translators;

        public Holder(AbstractMessageFactory<T> messageFactory) {
            this.messageFactory = messageFactory;
            listeners = new ArrayList<>();
            translators = new ArrayList<>();
        }

        public synchronized void addTranslator(MessageTranslator<? super T> translator) {
            translators.add(translator);
        }

        public synchronized void removeTranslator(MessageTranslator<? super T> translator) {
            translators.remove(translator);
        }

        public synchronized void addListener(MessageListener<? super T> listener) {
            listeners.add(listener);
        }

        public synchronized void removeListener(MessageListener<? super T> listener) {
            listeners.add(listener);
        }

        public AbstractMessageFactory<T> getMessageFactory() {
            return messageFactory;
        }

        public void listen(T message, ExecutorService executorService) {
            listeners.forEach(listener -> executorService.submit(() -> listener.listen(message)));
        }

        public void translate(T message) {
            translators.forEach(messageTranslator -> messageTranslator.translate(message));
        }
    }
}
