package com.alesharik.webserver.api;

import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * This class used for create different thread factories
 */
@UtilityClass
public final class ThreadFactories {
    public static ThreadFactory newThreadFactory(@Nonnull ThreadGroup threadGroup) {
        return new GroupThreadFactory(threadGroup);
    }

    public static ThreadFactory newThreadFactory(@Nonnull ThreadGroup threadGroup, @Nonnull Thread.UncaughtExceptionHandler exceptionHandler) {
        return new GroupThreadFactoryWithUncaughtExceptionHandler(threadGroup, exceptionHandler);
    }

    public static ThreadFactory newThreadFactory(@Nonnull ThreadGroup threadGroup, @Nonnull Supplier<String> nameSupplier) {
        return new NamedGroupThreadFactory(threadGroup, nameSupplier);
    }

    public static Supplier<String> incrementalSupplier(String name) {
        return new Supplier<String>() {
            private final AtomicInteger counter = new AtomicInteger(0);

            @Override
            public String get() {
                return name.concat(String.valueOf(counter.getAndIncrement()));
            }
        };
    }

    private static class GroupThreadFactory implements ThreadFactory {
        private final ThreadGroup threadGroup;

        GroupThreadFactory(ThreadGroup threadGroup) {
            this.threadGroup = threadGroup;
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(threadGroup, r);
        }
    }

    private static class NamedGroupThreadFactory implements ThreadFactory {
        private final ThreadGroup threadGroup;
        private final Supplier<String> nameSupplier;

        private NamedGroupThreadFactory(ThreadGroup threadGroup, Supplier<String> nameSupplier) {
            this.threadGroup = threadGroup;
            this.nameSupplier = nameSupplier;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(threadGroup, r);
            thread.setName(nameSupplier.get());
            return thread;
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
