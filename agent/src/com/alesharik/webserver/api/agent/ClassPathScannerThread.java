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

import com.alesharik.webserver.api.ExecutionStage;
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
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.jctools.queues.atomic.MpscLinkedAtomicQueue;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
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

    private final Map<MethodHandle, Method> relations = new ConcurrentHashMap<>();

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
        synchronized (scanLock) {
            scanLock.wait();
        }

        ClassLoader classLoader = classLoaderQueue.poll();
        if(classLoader != null)
            scanClassLoader(classLoader);
        ClassLoader toRescan = removeQueue.poll();
        if(toRescan != null)
            removeClassLoaderImpl(toRescan);
    }

    private void scanClassLoader(ClassLoader classLoader) {
        taskCount.incrementAndGet();
        ConcurrentTripleHashMap<Class<?>, Type, MethodHandle> newListeners = new ConcurrentTripleHashMap<>();
        Map<MethodHandle, Method> rel = new HashMap<>();

        try (ScanResult result = new ClassGraph()
                .ignoreParentClassLoaders()
                .overrideClassLoaders(classLoader)
                .ignoreClassVisibility()
                .ignoreFieldVisibility()
                .ignoreMethodVisibility()
                .whitelistPackages("*")
                .enableAllInfo()
                .scan(workerPool, PARALLELISM)) {
            for(ClassInfo classInfo : result.getClassesWithAnnotation(ClassTransformer.class.getCanonicalName())) {
                if(classInfo.hasAnnotation(Ignored.class.getCanonicalName()))
                    continue;

                try {
                    AgentClassTransformer.addTransformer(classLoader.loadClass(classInfo.getName()), false);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

            for(ClassInfo classInfo : result.getClassesWithAnnotation(ClassPathScanner.class.getCanonicalName())) {
                if(classInfo.hasAnnotation(Ignored.class.getCanonicalName()))
                    continue;

                try {
                    matchClassPathScanner(classLoader.loadClass(classInfo.getName()), newListeners, false, rel);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        new ClassLoaderScanTask(listeners, Collections.singleton(classLoader), workerPool, rel).run();
        classLoaders.add(classLoader);
        new ClassLoaderScanTask(newListeners, classLoaders, workerPool, rel).run();
        relations.putAll(rel);
        listeners.putAll(newListeners);
        taskCount.decrementAndGet();
    }

    private void removeClassLoaderImpl(ClassLoader classLoader) {
        taskCount.incrementAndGet();
        classLoaders.remove(classLoader);
        for(Triple<Class<?>, Type, MethodHandle> classTypeMethodHandleTriple : listeners.entrySet()) {
            if(relations.get(classTypeMethodHandleTriple.getC()).getDeclaringClass().getClassLoader() == classLoader) {
                listeners.remove(classTypeMethodHandleTriple.getA());
                relations.remove(classTypeMethodHandleTriple.getC());
            }
        }
        for(Map.Entry<Type, Collection<MethodHandle>> typeCollectionEntry : commonListeners.asMap().entrySet()) {
            for(MethodHandle methodHandle : typeCollectionEntry.getValue()) {
                if(relations.get(methodHandle).getDeclaringClass().getClassLoader() == classLoader) {
                    commonListeners.removeMapping(typeCollectionEntry.getKey(), methodHandle);
                    relations.remove(methodHandle);
                }
            }
        }
        new ClassLoaderRemoveTask(commonListeners, classLoader, relations).run();
        taskCount.decrementAndGet();
    }

    public void addClassLoader(ClassLoader classLoader) {
        classLoaderQueue.add(classLoader);
        synchronized (scanLock) {
            scanLock.notifyAll();
        }
    }

    private void matchClassPathScanner(Class<?> clazz, ConcurrentTripleHashMap<Class<?>, Type, MethodHandle> newListeners, boolean replace, Map<MethodHandle, Method> rel) {
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
                .forEach(method -> {
                    if(method.isAnnotationPresent(ListenAnnotation.class)) {
                        ListenAnnotation annotation = method.getAnnotation(ListenAnnotation.class);
                        try {
                            method.setAccessible(true);
                            MethodHandle unreflect = LOOKUP.unreflect(method);
                            newListeners.put(annotation.value(), Type.ANNOTATION, unreflect);
                            rel.put(unreflect, method);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    } else if(method.isAnnotationPresent(ListenClass.class)) {
                        ListenClass annotation = method.getAnnotation(ListenClass.class);
                        try {
                            method.setAccessible(true);
                            MethodHandle unreflect = LOOKUP.unreflect(method);
                            newListeners.put(annotation.value(), Type.CLASS, unreflect);
                            rel.put(unreflect, method);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    } else if(method.isAnnotationPresent(ListenInterface.class)) {
                        ListenInterface annotation = method.getAnnotation(ListenInterface.class);
                        try {
                            method.setAccessible(true);
                            MethodHandle unreflect = LOOKUP.unreflect(method);
                            newListeners.put(annotation.value(), Type.INTERFACE, unreflect);
                            rel.put(unreflect, method);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    } else if(method.isAnnotationPresent(ListenReloadStart.class)) {
                        try {
                            method.setAccessible(true);
                            MethodHandle unreflect = LOOKUP.unreflect(method);
                            commonListeners.put(Type.RELOAD_START, unreflect);
                            relations.put(unreflect, method);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    } else if(method.isAnnotationPresent(ListenReloadEnd.class)) {
                        try {
                            method.setAccessible(true);
                            MethodHandle unreflect = LOOKUP.unreflect(method);
                            commonListeners.put(Type.RELOAD_END, unreflect);
                            relations.put(unreflect, method);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    } else if(method.isAnnotationPresent(UnloadClassLoaderHandler.class)) {
                        classLoaderMemoryLeakSuspected.set(false);
                        try {
                            method.setAccessible(true);
                            MethodHandle unreflect = LOOKUP.unreflect(method);
                            commonListeners.put(Type.UNLOAD_CLASS_LOADER, unreflect);
                            relations.put(unreflect, method);
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

    @RequiredArgsConstructor
    private final class ClassLoaderRemoveTask implements Runnable {
        private final MultiValuedMap<Type, MethodHandle> listeners;
        private final ClassLoader classLoader;
        private final Map<MethodHandle, Method> rel;

        @Override
        public void run() {
            listeners.get(Type.RELOAD_START).forEach(this::invokeMethod);
            listeners.get(Type.UNLOAD_CLASS_LOADER).forEach(methodHandle -> invokeMethod(methodHandle, classLoader));
            listeners.get(Type.RELOAD_END).forEach(this::invokeMethod);
        }

        private void invokeMethod(MethodHandle methodHandle, ClassLoader classLoader) {
            Stages stages = rel.get(methodHandle).getAnnotation(Stages.class);
            if(stages != null && ExecutionStage.isEnabled() && !ExecutionStage.valid(stages.value()))
                return;
            try {
                methodHandle.invoke(classLoader);
            } catch (Error error) {
                throw error;
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }

        private void invokeMethod(MethodHandle methodHandle) {
            Stages stages = rel.get(methodHandle).getAnnotation(Stages.class);
            if(stages != null && ExecutionStage.isEnabled() && !ExecutionStage.valid(stages.value()))
                return;
            try {
                methodHandle.invoke();
            } catch (Error error) {
                throw error;
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    @RequiredArgsConstructor
    private final class ClassLoaderScanTask implements Runnable {
        private final ConcurrentTripleHashMap<Class<?>, Type, MethodHandle> listeners;
        private final Set<ClassLoader> classLoaders;
        private final ForkJoinPool forkJoinPool;
        private final Map<MethodHandle, Method> rel;

        @Override
        public void run() {
            CompletableFuture.supplyAsync(ClassPathScannerThread.taskCount::incrementAndGet, workerPool)
                    .thenApply(integer -> new ClassGraph()
                            .enableAllInfo()
                            .ignoreMethodVisibility()
                            .ignoreFieldVisibility()
                            .ignoreClassVisibility()
                            .overrideClassLoaders(classLoaders.toArray(new ClassLoader[0]))
                            .ignoreParentClassLoaders())
                    .thenApply(scanner -> {
                        try {
                            return scanner.scanAsync(forkJoinPool, PARALLELISM).get();
                        } catch (InterruptedException | ExecutionException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .thenAccept(this::processResult)
                    .thenRun(ClassPathScannerThread.taskCount::decrementAndGet)
                    .exceptionally(throwable -> {
                        throwable.getCause().printStackTrace();
                        return null;
                    });
        }

        private void processResult(ScanResult result) {
            listeners.forEach((aClass, type, methodHandle) -> {
                Stages stages = rel.get(methodHandle).getAnnotation(Stages.class);
                if(stages != null && ExecutionStage.isEnabled() && !ExecutionStage.valid(stages.value()))
                    return;

                switch (type) {
                    case CLASS:
                        result.getSubclasses(aClass.getCanonicalName())
                                .filter(classInfo -> classInfo.hasAnnotation(Ignored.class.getCanonicalName()))
                                .forEach(classInfo -> invokeMethod(methodHandle, classInfo));
                        break;
                    case INTERFACE:
                        result.getClassesImplementing(aClass.getCanonicalName())
                                .filter(classInfo -> classInfo.hasAnnotation(Ignored.class.getCanonicalName()))
                                .forEach(classInfo -> invokeMethod(methodHandle, classInfo));
                        break;
                    case ANNOTATION:
                        result.getClassesWithAnnotation(aClass.getCanonicalName())
                                .filter(classInfo -> classInfo.hasAnnotation(Ignored.class.getCanonicalName()))
                                .forEach(classInfo -> invokeMethod(methodHandle, classInfo));
                        break;
                }
            });
        }

        private void invokeMethod(MethodHandle methodHandle, ClassInfo clazz) {
            try {
                methodHandle.invokeExact(clazz.loadClass());
            } catch (Exception e) {
                e.printStackTrace();
            } catch (Throwable e) {
                throw (Error) e;
            }
        }
    }
}
