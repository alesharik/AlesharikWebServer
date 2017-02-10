package com.alesharik.webserver.logger;

/**
 * LoggerListener listen all {@link Logger} or {@link NamedLogger} messages. Use LoggerListenerThread for execution.
 */
public interface LoggerListener {
    /**
     * Listen {@link Logger} or {@link NamedLogger} message. Executes in LoggerListenerThread.
     *
     * @param prefixes message prefixes
     * @param message  the message
     */
    void listen(String prefixes, String message);
}
