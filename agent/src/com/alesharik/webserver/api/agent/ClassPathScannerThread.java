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

package com.alesharik.webserver.api.agent;

import com.alesharik.webserver.api.agent.classPath.ClassPathScanner;
import com.alesharik.webserver.api.agent.classPath.ListenAnnotation;
import com.alesharik.webserver.api.agent.classPath.ListenClass;
import com.alesharik.webserver.api.agent.classPath.ListenInterface;
import com.alesharik.webserver.api.agent.transformer.ClassTransformer;
import com.alesharik.webserver.api.collections.ConcurrentTripleHashMap;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefixes;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * This thread find annotated classes
 */
@Prefixes({"[Agent]", "[ClassPathScannerThread]"})
final class ClassPathScannerThread extends Thread {
    private static final AtomicInteger taskCount = new AtomicInteger(0);
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    /**
     * FJP parallelism
     */
    private static final int PARALLELISM = 5;

    private final ForkJoinPool workerPool;
    private final LinkedBlockingQueue<ClassLoader> classLoaderQueue;
    private final CopyOnWriteArraySet<ClassLoader> classLoaders;

    private final ConcurrentTripleHashMap<Class<?>, Type, MethodHandle> listeners;

    public ClassPathScannerThread() {
        setName("ClassPath scanner thread");
        setDaemon(true);

        workerPool = new ForkJoinPool(PARALLELISM);
        classLoaderQueue = new LinkedBlockingQueue<>();
        classLoaders = new CopyOnWriteArraySet<>();
        listeners = new ConcurrentTripleHashMap<>();
    }

    @Override
    public void run() {
        try {
            while(isAlive() && !isInterrupted())
                iteration();
        } catch (InterruptedException e) {
            Logger.log("Shutdown thread...");
        }
    }

    private void iteration() throws InterruptedException {
        try {
            ClassLoader classLoader = classLoaderQueue.take();
            taskCount.incrementAndGet();
            ConcurrentTripleHashMap<Class<?>, Type, MethodHandle> newListeners = new ConcurrentTripleHashMap<>();

            ScanResult result = new FastClasspathScanner()
                    .overrideClassLoaders(classLoader)
                    .matchClassesWithAnnotation(ClassPathScanner.class, classWithAnnotation -> matchClassPathScanner(classWithAnnotation, newListeners))
                    .matchClassesWithAnnotation(ClassTransformer.class, transformer -> {
                        if(transformer.isAnnotationPresent(Ignored.class))
                            return;
                        AgentClassTransformer.addTransformer(transformer);
                    })
                    .verbose(false)
                    .scanAsync(workerPool, PARALLELISM)
                    .get();

            if(result.getMatchProcessorExceptions().size() > 0) {
                System.err.println("Exceptions detected while processing classloader!");
                for(Throwable throwable : result.getMatchProcessorExceptions())
                    throwable.printStackTrace();
            }

            new ClassLoaderScanTask(listeners, Collections.singleton(classLoader), workerPool).run();
            classLoaders.add(classLoader);
            new ClassLoaderScanTask(newListeners, classLoaders, workerPool).run();
            listeners.putAll(newListeners);
            taskCount.decrementAndGet();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void addClassLoader(ClassLoader classLoader) {
        classLoaderQueue.add(classLoader);
    }

    private void matchClassPathScanner(Class<?> clazz, ConcurrentTripleHashMap<Class<?>, Type, MethodHandle> newListeners) {
        if(clazz.isAnnotationPresent(Ignored.class))
            return;
        if(listeners.containsKey(clazz))
            return;

        Stream.of(clazz.getDeclaredMethods())
                .filter(method -> Modifier.isStatic(method.getModifiers()))
                .filter(method -> method.isAnnotationPresent(ListenAnnotation.class) || method.isAnnotationPresent(ListenClass.class) || method.isAnnotationPresent(ListenInterface.class))
                .forEach(method -> {
                    if(method.isAnnotationPresent(ListenAnnotation.class)) {
                        ListenAnnotation annotation = method.getAnnotation(ListenAnnotation.class);
                        try {
                            method.setAccessible(true);
                            newListeners.put(annotation.value(), Type.ANNOTATION, LOOKUP.unreflect(method));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    } else if(method.isAnnotationPresent(ListenClass.class)) {
                        ListenClass annotation = method.getAnnotation(ListenClass.class);
                        try {
                            method.setAccessible(true);
                            newListeners.put(annotation.value(), Type.CLASS, LOOKUP.unreflect(method));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    } else if(method.isAnnotationPresent(ListenInterface.class)) {
                        ListenInterface annotation = method.getAnnotation(ListenInterface.class);
                        try {
                            method.setAccessible(true);
                            newListeners.put(annotation.value(), Type.INTERFACE, LOOKUP.unreflect(method));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    @Override
    public void interrupt() {
        workerPool.shutdownNow();
    }

    public boolean isFree() {
        return workerPool.isQuiescent() && taskCount.get() == 0;
    }

    private enum Type {
        ANNOTATION,
        CLASS,
        INTERFACE
    }

    private static final class ClassLoaderScanTask implements Runnable {
        private final ConcurrentTripleHashMap<Class<?>, Type, MethodHandle> listeners;
        private final Set<ClassLoader> classLoaders;
        private final ForkJoinPool forkJoinPool;

        public ClassLoaderScanTask(ConcurrentTripleHashMap<Class<?>, Type, MethodHandle> listeners, Set<ClassLoader> classLoaders, ForkJoinPool forkJoinPool) {
            this.listeners = listeners;
            this.classLoaders = classLoaders;
            this.forkJoinPool = forkJoinPool;
        }

        @Override
        public void run() {
            FastClasspathScanner scanner = new FastClasspathScanner();
            ClassPathScannerThread.taskCount.incrementAndGet();
            listeners.forEach((aClass, type, method) -> {
                switch (type) {
                    case ANNOTATION:
                        scanner.matchClassesWithAnnotation(aClass, classWithAnnotation -> {
                            if(classWithAnnotation.isAnnotationPresent(Ignored.class))
                                return;
                            try {
                                method.invokeExact(classWithAnnotation);
                            } catch (Throwable e) {
                                Logger.log(e);
                            }
                        });
                        break;
                    case CLASS:
                        scanner.matchSubclassesOf(aClass, subclass -> {
                            if(subclass.isAnnotationPresent(Ignored.class))
                                return;
                            try {
                                method.invokeExact(subclass);
                            } catch (Throwable e) {
                                Logger.log(e);
                            }
                        });
                        break;
                    case INTERFACE:
                        scanner.matchClassesImplementing(aClass, implementingClass -> {
                            if(implementingClass.isAnnotationPresent(Ignored.class))
                                return;
                            try {
                                method.invokeExact(implementingClass);
                            } catch (Throwable e) {
                                Logger.log(e);
                            }
                        });
                }
            });
            scanner.overrideClassLoaders(classLoaders.toArray(new ClassLoader[classLoaders.size()]));
            CompletableFuture.supplyAsync(ClassPathScannerThread.taskCount::incrementAndGet)
                    .thenApply(integer -> {
                        try {
                            return scanner.scanAsync(forkJoinPool, PARALLELISM).get();
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .thenRun(ClassPathScannerThread.taskCount::decrementAndGet);

            ClassPathScannerThread.taskCount.decrementAndGet();
        }
    }
}
