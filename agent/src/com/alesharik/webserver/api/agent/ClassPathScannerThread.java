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
import com.alesharik.webserver.api.agent.classPath.SuppressClassLoaderUnloadWarning;
import com.alesharik.webserver.api.agent.classPath.reload.ListenReloadEnd;
import com.alesharik.webserver.api.agent.classPath.reload.ListenReloadStart;
import com.alesharik.webserver.api.agent.classPath.reload.UnloadClassLoaderHandler;
import com.alesharik.webserver.api.agent.transformer.ClassTransformer;
import com.alesharik.webserver.api.collections.ConcurrentTripleHashMap;
import com.alesharik.webserver.api.misc.Triple;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefixes;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.jctools.queues.atomic.MpscLinkedAtomicQueue;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
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
    private final MpscLinkedAtomicQueue<ClassLoader> removeQueue;
    private final CopyOnWriteArraySet<ClassLoader> classLoaders;
    private final Object scanLock = new Object();

    private final ConcurrentTripleHashMap<Class<?>, Type, MethodHandle> listeners;
    private final MultiValuedMap<Type, MethodHandle> commonListeners = new ArrayListValuedHashMap<>();

    private final Map<MethodHandle, Class<?>> relations = new ConcurrentHashMap<>();

    public ClassPathScannerThread() {
        setName("ClassPath scanner thread");
        setDaemon(true);

        workerPool = new ForkJoinPool(PARALLELISM);
        classLoaderQueue = new LinkedBlockingQueue<>();
        removeQueue = new MpscLinkedAtomicQueue<>();
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
            synchronized (scanLock) {
                scanLock.wait();
            }

            ClassLoader classLoader = classLoaderQueue.poll();
            if(classLoader != null)
                scanClassLoader(classLoader);
            ClassLoader toRescan = removeQueue.poll();
            if(toRescan != null)
                removeClassLoaderImpl(toRescan);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void scanClassLoader(ClassLoader classLoader) throws InterruptedException, ExecutionException {
        taskCount.incrementAndGet();
        ConcurrentTripleHashMap<Class<?>, Type, MethodHandle> newListeners = new ConcurrentTripleHashMap<>();
        Map<MethodHandle, Class<?>> rel = new HashMap<>();

        ScanResult result = new FastClasspathScanner()
                .ignoreParentClassLoaders()
                .overrideClassLoaders(classLoader)
                .matchClassesWithAnnotation(ClassPathScanner.class, classWithAnnotation -> matchClassPathScanner(classWithAnnotation, newListeners, false, rel))
                .matchClassesWithAnnotation(ClassTransformer.class, transformer -> {
                    if(transformer.isAnnotationPresent(Ignored.class))
                        return;
                    AgentClassTransformer.addTransformer(transformer, false);
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
        relations.putAll(rel);
        listeners.putAll(newListeners);
    }

    private void removeClassLoaderImpl(ClassLoader classLoader) throws InterruptedException, ExecutionException {
        taskCount.incrementAndGet();
        classLoaders.remove(classLoader);
        for(Triple<Class<?>, Type, MethodHandle> classTypeMethodHandleTriple : listeners.entrySet()) {
            if(relations.get(classTypeMethodHandleTriple.getC()).getClassLoader() == classLoader) {
                listeners.remove(classTypeMethodHandleTriple.getA());
                relations.remove(classTypeMethodHandleTriple.getC());
            }
        }
        for(Map.Entry<Type, Collection<MethodHandle>> typeCollectionEntry : commonListeners.asMap().entrySet()) {
            for(MethodHandle methodHandle : typeCollectionEntry.getValue()) {
                if(relations.get(methodHandle).getClassLoader() == classLoader) {
                    commonListeners.removeMapping(typeCollectionEntry.getKey(), methodHandle);
                    relations.remove(methodHandle);
                }
            }
        }
        new ClassLoaderRemoveTask(commonListeners, classLoader).run();
    }

    public void addClassLoader(ClassLoader classLoader) {
        classLoaderQueue.add(classLoader);
        synchronized (scanLock) {
            scanLock.notifyAll();
        }
    }

    private void matchClassPathScanner(Class<?> clazz, ConcurrentTripleHashMap<Class<?>, Type, MethodHandle> newListeners, boolean replace, Map<MethodHandle, Class<?>> rel) {
        if(clazz.isAnnotationPresent(Ignored.class))
            return;
        if(listeners.containsKey(clazz)) {
            if(replace) {
                MethodHandle methodHandle = listeners.remove(clazz).getC();
                relations.remove(methodHandle);
            } else
                return;
        }

        AtomicBoolean classLoaderMemoryLeakSuspected = new AtomicBoolean(!clazz.isAnnotationPresent(SuppressClassLoaderUnloadWarning.class));
        Stream.of(clazz.getDeclaredMethods())
                .filter(method -> Modifier.isStatic(method.getModifiers()))
                .filter(method -> method.isAnnotationPresent(ListenAnnotation.class) || method.isAnnotationPresent(ListenClass.class) || method.isAnnotationPresent(ListenInterface.class)
                        || method.isAnnotationPresent(ListenReloadStart.class) || method.isAnnotationPresent(ListenReloadEnd.class) || method.isAnnotationPresent(UnloadClassLoaderHandler.class))
                .forEach(method -> {
                    if(method.isAnnotationPresent(ListenAnnotation.class)) {
                        ListenAnnotation annotation = method.getAnnotation(ListenAnnotation.class);
                        try {
                            method.setAccessible(true);
                            MethodHandle unreflect = LOOKUP.unreflect(method);
                            newListeners.put(annotation.value(), Type.ANNOTATION, unreflect);
                            rel.put(unreflect, method.getDeclaringClass());
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    } else if(method.isAnnotationPresent(ListenClass.class)) {
                        ListenClass annotation = method.getAnnotation(ListenClass.class);
                        try {
                            method.setAccessible(true);
                            MethodHandle unreflect = LOOKUP.unreflect(method);
                            newListeners.put(annotation.value(), Type.CLASS, unreflect);
                            rel.put(unreflect, method.getDeclaringClass());
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    } else if(method.isAnnotationPresent(ListenInterface.class)) {
                        ListenInterface annotation = method.getAnnotation(ListenInterface.class);
                        try {
                            method.setAccessible(true);
                            MethodHandle unreflect = LOOKUP.unreflect(method);
                            newListeners.put(annotation.value(), Type.INTERFACE, unreflect);
                            rel.put(unreflect, method.getDeclaringClass());
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    } else if(method.isAnnotationPresent(ListenReloadStart.class)) {
                        try {
                            method.setAccessible(true);
                            MethodHandle unreflect = LOOKUP.unreflect(method);
                            commonListeners.put(Type.RELOAD_START, unreflect);
                            relations.put(unreflect, method.getDeclaringClass());
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    } else if(method.isAnnotationPresent(ListenReloadEnd.class)) {
                        try {
                            method.setAccessible(true);
                            MethodHandle unreflect = LOOKUP.unreflect(method);
                            commonListeners.put(Type.RELOAD_END, unreflect);
                            relations.put(unreflect, method.getDeclaringClass());
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    } else if(method.isAnnotationPresent(UnloadClassLoaderHandler.class)) {
                        classLoaderMemoryLeakSuspected.set(false);
                        try {
                            method.setAccessible(true);
                            MethodHandle unreflect = LOOKUP.unreflect(method);
                            commonListeners.put(Type.UNLOAD_CLASS_LOADER, unreflect);
                            relations.put(unreflect, method.getDeclaringClass());
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                });
        if(classLoaderMemoryLeakSuspected.get())
            System.err.println("Class " + clazz.getCanonicalName() + " is suspected in classloader memory leak. Every ClassPathScanner MUST clear all classes of removed classloader. See @UnloadClassLoaderHandler");
    }

    public void removeClassLoader(ClassLoader classLoader) {
        removeQueue.add(classLoader);
        synchronized (scanLock) {
            scanLock.notifyAll();
        }
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
        INTERFACE,
        RELOAD_START,
        RELOAD_END,
        UNLOAD_CLASS_LOADER
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
            FastClasspathScanner scanner = new FastClasspathScanner().ignoreParentClassLoaders();
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

    private static final class ClassLoaderRemoveTask implements Runnable {
        private final MultiValuedMap<Type, MethodHandle> listeners;
        private final ClassLoader classLoader;

        public ClassLoaderRemoveTask(MultiValuedMap<Type, MethodHandle> listeners, ClassLoader classLoader) {
            this.listeners = listeners;
            this.classLoader = classLoader;
        }

        @Override
        public void run() {
            for(MethodHandle methodHandle : listeners.get(Type.RELOAD_START)) {
                try {
                    methodHandle.invoke();
                } catch (Error error) {
                    throw error;
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }

            for(MethodHandle methodHandle : listeners.get(Type.UNLOAD_CLASS_LOADER)) {
                try {
                    methodHandle.invoke(classLoader);
                } catch (Error error) {
                    throw error;
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }

            for(MethodHandle methodHandle : listeners.get(Type.RELOAD_END)) {
                try {
                    methodHandle.invoke();
                } catch (Error error) {
                    throw error;
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }
    }
}
