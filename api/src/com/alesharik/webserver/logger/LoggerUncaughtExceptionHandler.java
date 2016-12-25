package com.alesharik.webserver.logger;

/**
 * This singleton used for log all uncaught exceptions in thread with {@link Logger}
 */
public final class LoggerUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    public static final LoggerUncaughtExceptionHandler INSTANCE = new LoggerUncaughtExceptionHandler();

    private LoggerUncaughtExceptionHandler() {
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Logger.log(e);
    }
}
