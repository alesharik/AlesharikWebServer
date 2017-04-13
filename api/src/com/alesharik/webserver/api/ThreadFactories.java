package com.alesharik.webserver.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
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
    @Nonnull
    public static ThreadFactory newThreadFactory(@Nonnull ThreadGroup threadGroup) {
        return new GroupThreadFactory(threadGroup);
    }

    @Nonnull
    public static ThreadFactory newThreadFactory(@Nonnull ThreadGroup threadGroup, @Nonnull Thread.UncaughtExceptionHandler exceptionHandler) {
        return new GroupThreadFactoryWithUncaughtExceptionHandler(threadGroup, exceptionHandler);
    }

    @Nonnull
    public static ThreadFactory newThreadFactory(@Nonnull ThreadGroup threadGroup, @Nonnull Supplier<String> nameSupplier) {
        return new NamedGroupThreadFactory(threadGroup, nameSupplier);
    }

    @Nonnull
    public static ThreadFactory newThreadFactory(@Nonnull Supplier<String> nameSupplier) {
        return new NamedThreadFactory(nameSupplier);
    }

    /**
     * Create supplier with counter. It will supply <code>name + counter.get()</code> string and increment counter on every step.
     * Supplier is thread-safe. Counter starts form 0
     *
     * @param name the name
     * @return supplier
     */
    @Nonnull
    public static Supplier<String> incrementalSupplier(@Nonnull String name) {
        return new IncrementalSupplier(name);
    }

    private static final class IncrementalSupplier implements Supplier<String> {
        private final String name;
        private final AtomicInteger counter;

        private IncrementalSupplier(String name) {
            this.name = name;
            this.counter = new AtomicInteger(0);
        }

        @Override
        public String get() {
            return name + counter.getAndIncrement();
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static class GroupThreadFactory implements ThreadFactory {
        private final ThreadGroup threadGroup;

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(threadGroup, r);
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static class NamedGroupThreadFactory implements ThreadFactory {
        private final ThreadGroup threadGroup;
        private final Supplier<String> nameSupplier;

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(threadGroup, r);
            thread.setName(nameSupplier.get());
            return thread;
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static class NamedThreadFactory implements ThreadFactory {
        private final Supplier<String> nameSupplier;

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
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
