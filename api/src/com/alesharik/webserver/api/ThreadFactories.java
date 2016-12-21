package com.alesharik.webserver.api;

import java.util.concurrent.ThreadFactory;

/**
 * This class used for create different thread factories
 */
public final class ThreadFactories {
    public static ThreadFactory newThreadFactory(ThreadGroup threadGroup) {
        return new GroupThreadFactory(threadGroup);
    }

    public static ThreadFactory newThreadFactory(ThreadGroup threadGroup, Thread.UncaughtExceptionHandler exceptionHandler) {
        return new GroupThreadFactoryWithUncaughtExceptionHandler(threadGroup, exceptionHandler);
    }

    private static class GroupThreadFactory implements ThreadFactory {
        private final ThreadGroup threadGroup;

        GroupThreadFactory() {
            threadGroup = Thread.currentThread().getThreadGroup();
        }

        GroupThreadFactory(ThreadGroup threadGroup) {
            this.threadGroup = threadGroup;
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(threadGroup, r);
        }
    }

    private static class GroupThreadFactoryWithUncaughtExceptionHandler extends GroupThreadFactory {
        private final Thread.UncaughtExceptionHandler exceptionHandler;

        GroupThreadFactoryWithUncaughtExceptionHandler(ThreadGroup threadGroup, Thread.UncaughtExceptionHandler exceptionHandler) {
            super(threadGroup);
            this.exceptionHandler = exceptionHandler;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = super.newThread(r);
            thread.setUncaughtExceptionHandler(exceptionHandler);
            return thread;
        }
    }
}
