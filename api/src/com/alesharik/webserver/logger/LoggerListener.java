package com.alesharik.webserver.logger;

/**
 * This listener listen {@link Logger} and all {@link NamedLogger}s messages. It will call in Logger Listener Thread
 */
public interface LoggerListener {
    /**
     * Listen message
     *
     * @param prefixes message prefixes
     * @param message  the message
     * @param caller class, which call logger
     */
    void listen(String prefixes, String message, Class<?> caller);
}
