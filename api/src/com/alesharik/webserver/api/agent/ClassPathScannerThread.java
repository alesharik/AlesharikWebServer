package com.alesharik.webserver.api.agent;

import com.alesharik.webserver.api.agent.classPath.ClassPathScanner;
import com.alesharik.webserver.api.agent.classPath.ListenAnnotation;
import com.alesharik.webserver.api.agent.classPath.ListenClass;
import com.alesharik.webserver.api.agent.classPath.ListenInterface;
import com.alesharik.webserver.api.agent.transformer.ClassTransformer;
import com.alesharik.webserver.api.collections.ConcurrentTripleHashMap;
import com.alesharik.webserver.api.collections.TripleHashMap;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.LoggerUncaughtExceptionHandler;
import com.alesharik.webserver.logger.Prefix;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import lombok.SneakyThrows;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Stream;

/**
 * This thread find annotated classes
 */
@Prefix("[ClassPathScannerThread]")
final class ClassPathScannerThread extends Thread {
    /**
     * FJP parallelism
     */
    private static final int PARALLELISM = 5;

    private final ForkJoinPool workerPool;
    private final LinkedBlockingQueue<ClassLoader> classLoaderQueue;
    private final CopyOnWriteArraySet<ClassLoader> classLoaders;

    private final ConcurrentTripleHashMap<Class<?>, Type, Method> listeners;

    public ClassPathScannerThread() {
        setName("ClassPath scanner thread");
        setDaemon(true);

        setDefaultUncaughtExceptionHandler(LoggerUncaughtExceptionHandler.INSTANCE);

        workerPool = new ForkJoinPool(PARALLELISM);
        classLoaderQueue = new LinkedBlockingQueue<>();
        classLoaders = new CopyOnWriteArraySet<>();
        listeners = new ConcurrentTripleHashMap<>();
    }

    @Override
    @SneakyThrows
    public void run() {
        while(isAlive()) {
            ClassLoader classLoader = classLoaderQueue.take();
            ConcurrentTripleHashMap<Class<?>, Type, Method> newListeners = new ConcurrentTripleHashMap<>();
            new FastClasspathScanner()
                    .overrideClassLoaders(classLoader)
                    .matchClassesWithAnnotation(ClassPathScanner.class, classWithAnnotation -> matchClassPathScanner(classWithAnnotation, newListeners))
                    .matchClassesWithAnnotation(ClassTransformer.class, AgentClassTransformer::addTransformer)
                    .scanAsync(workerPool, PARALLELISM)
                    .get();

            workerPool.submit(new ClassLoaderScanTask(listeners, Collections.singleton(classLoader), workerPool));
            classLoaders.add(classLoader);
            workerPool.submit(new ClassLoaderScanTask(newListeners, classLoaders, workerPool));
            listeners.putAll(newListeners);
        }
    }

    public void addClassLoader(ClassLoader classLoader) {
        classLoaderQueue.add(classLoader);
    }

    private void matchClassPathScanner(Class<?> clazz, TripleHashMap<Class<?>, Type, Method> newListeners) {
        Stream.of(clazz.getDeclaredMethods())
                .filter(method -> Modifier.isStatic(method.getModifiers()))
                .filter(method -> method.isAnnotationPresent(ListenAnnotation.class) || method.isAnnotationPresent(ListenClass.class) || method.isAnnotationPresent(ListenInterface.class))
                .forEach(method -> {
                    method.setAccessible(true);
                    if(method.isAnnotationPresent(ListenAnnotation.class)) {
                        ListenAnnotation annotation = method.getAnnotation(ListenAnnotation.class);
                        newListeners.put(annotation.value(), Type.ANNOTATION, method);
                    } else if(method.isAnnotationPresent(ListenClass.class)) {
                        ListenClass annotation = method.getAnnotation(ListenClass.class);
                        newListeners.put(annotation.value(), Type.CLASS, method);
                    } else if(method.isAnnotationPresent(ListenInterface.class)) {
                        ListenInterface annotation = method.getAnnotation(ListenInterface.class);
                        newListeners.put(annotation.value(), Type.INTERFACE, method);
                    }
                });
    }

    @Override
    public void interrupt() {
        workerPool.shutdownNow();
    }

    private enum Type {
        ANNOTATION,
        CLASS,
        INTERFACE
    }

    private static final class ClassLoaderScanTask implements Runnable {
        private final TripleHashMap<Class<?>, Type, Method> listeners;
        private final Set<ClassLoader> classLoaders;
        private final ForkJoinPool forkJoinPool;

        public ClassLoaderScanTask(TripleHashMap<Class<?>, Type, Method> listeners, Set<ClassLoader> classLoaders, ForkJoinPool forkJoinPool) {
            this.listeners = listeners;
            this.classLoaders = classLoaders;
            this.forkJoinPool = forkJoinPool;
        }

        @Override
        public void run() {
            FastClasspathScanner scanner = new FastClasspathScanner();
            listeners.forEach((aClass, type, method) -> {
                switch (type) {
                    case ANNOTATION:
                        scanner.matchClassesWithAnnotation(aClass, classWithAnnotation -> {
                            try {
                                method.invoke(null, classWithAnnotation);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                Logger.log(e);
                            }
                        });
                        break;
                    case CLASS:
                        scanner.matchSubclassesOf(aClass, subclass -> {
                            try {
                                method.invoke(null, subclass);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                Logger.log(e);
                            }
                        });
                        break;
                    case INTERFACE:
                        scanner.matchClassesImplementing(aClass, implementingClass -> {
                            try {
                                method.invoke(null, implementingClass);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                Logger.log(e);
                            }
                        });
                }
            });
            scanner.overrideClassLoaders(classLoaders.toArray(new ClassLoader[0]));
            scanner.scanAsync(forkJoinPool, PARALLELISM);
        }
    }
}