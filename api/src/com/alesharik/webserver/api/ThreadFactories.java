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

package com.alesharik.webserver.api;

import com.alesharik.webserver.exception.error.UnexpectedBehaviorError;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessControlContext;
import java.security.ProtectionDomain;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
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

    public static ForkJoinPool.ForkJoinWorkerThreadFactory newFJPThreadFactory(@Nonnull ThreadGroup threadGroup) {
        return new ForkJoinPoolThreadFactory(threadGroup);
    }

    private static final class ForkJoinPoolThreadFactory implements ForkJoinPool.ForkJoinWorkerThreadFactory {
        private static final AccessControlContext ACC =
                new AccessControlContext(
                        new ProtectionDomain[]{
                                new ProtectionDomain(null, null)
                        });
        private static final Constructor<?> constructor;

        private final ThreadGroup group;

        public ForkJoinPoolThreadFactory(ThreadGroup group) {
            this.group = group;
        }

        static {
            try {
                constructor = ForkJoinWorkerThread.class.getDeclaredConstructor(ForkJoinPool.class, ThreadGroup.class, AccessControlContext.class);
                constructor.setAccessible(true);
            } catch (NoSuchMethodException e) {
                throw new UnexpectedBehaviorError("ForkJoinWorkerThread doesn't have protected constructor(ForkJoinPool, ThreadGroup, AccessControlContext). Check your java version!", e);
            }
        }

        @Override
        public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
            try {
                return (ForkJoinWorkerThread) constructor.newInstance(pool, group, ACC);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                return null;
            }
        }
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
