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

package com.alesharik.webserver.configuration.module.layer.meta;

import com.alesharik.webserver.api.ExecutionStage;
import com.alesharik.webserver.api.agent.Stages;
import com.alesharik.webserver.api.agent.bean.Beans;
import com.alesharik.webserver.api.agent.classPath.ClassPathScanner;
import com.alesharik.webserver.api.agent.classPath.ListenInterface;
import com.alesharik.webserver.api.agent.classPath.SuppressClassLoaderUnloadWarning;
import com.alesharik.webserver.api.reflection.ReflectUtils;
import com.alesharik.webserver.configuration.module.Shutdown;
import com.alesharik.webserver.configuration.module.ShutdownNow;
import com.alesharik.webserver.configuration.module.Start;
import com.alesharik.webserver.configuration.module.layer.SubModule;
import com.alesharik.webserver.configuration.module.meta.CustomData;
import com.alesharik.webserver.configuration.module.meta.MetaInvokeException;
import com.alesharik.webserver.internals.instance.ClassInstantiator;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefixes;
import com.alesharik.webserver.logger.level.Level;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class builds {@link SubModuleAdapter}s from SubModule objects
 *
 * @see SubModuleAdapter
 * @see SubModuleProcessor
 */
@UtilityClass
@ClassPathScanner
@Prefixes({"[Module]", "[Meta]", "[SubModuleMetaFactory]"})
@Level("sub-module-meta-factory")
@SuppressClassLoaderUnloadWarning //can't reload SubModuleProcessors because they are loaded at CORE_MODULES stage
public class SubModuleMetaFactory {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final Lock writeLock = new ReentrantLock();
    static final List<SubModuleProcessor> processors = new ArrayList<>();

    static {
        Logger.getLoggingLevelManager().createLoggingLevel("sub-module-meta-factory");
    }

    /**
     * Wrap SubModule object into {@link SubModuleAdapter}
     *
     * @param o the object
     * @return the adapter
     * @throws IllegalArgumentException if object doesn't have {@link SubModule} annotation
     */
    public static SubModuleAdapter create(@Nonnull Object o) {
        Class<?> clazz = o.getClass();
        SubModule annotation = clazz.getAnnotation(SubModule.class);
        if(annotation == null)
            throw new IllegalArgumentException("Class " + clazz.getCanonicalName() + " is not a SubModule!");

        SubModuleAdapterImpl adapter = new SubModuleAdapterImpl(annotation.value());
        for(Method method : ReflectUtils.getAllDeclaredMethods(o.getClass())) {
            try {
                method.setAccessible(true);
                if(method.isAnnotationPresent(Start.class))
                    adapter.start.add(LOOKUP.unreflect(method).bindTo(o));
                if(method.isAnnotationPresent(Shutdown.class))
                    adapter.shutdown.add(LOOKUP.unreflect(method).bindTo(o));
                if(method.isAnnotationPresent(ShutdownNow.class))
                    adapter.shutdownNow.add(LOOKUP.unreflect(method).bindTo(o));
            } catch (IllegalAccessException e) {
                System.err.println("WAT");
                e.printStackTrace();
            }
        }

        for(SubModuleProcessor processor : processors) {
            try {
                processor.processSubModule(adapter, o);
            } catch (RuntimeException e) {
                System.err.println("Exception in SubModuleProcessor!");
                e.printStackTrace();
            }
        }

        return adapter;
    }

    @ListenInterface(SubModuleProcessor.class)
    @Stages(ExecutionStage.CORE_MODULES)
    static void listenClass(Class<?> clazz) {
        SubModuleProcessor processor = (SubModuleProcessor) Beans.getBean(clazz);
        if(processor == null)
            processor = (SubModuleProcessor) ClassInstantiator.instantiate(clazz);

        writeLock.lock();
        try {
            for(SubModuleProcessor subModuleProcessor : processors) {
                if(subModuleProcessor.getClass() == processor.getClass()) {
                    System.out.println("SubModuleProcessor " + processor.getClass().getCanonicalName() + " already registered! Skipping...");
                    return;
                }
            }
            processors.add(processor);
        } finally {
            writeLock.unlock();
        }
    }

    @RequiredArgsConstructor
    private static final class SubModuleAdapterImpl implements SubModuleAdapter {
        @Getter
        private final String name;
        @Getter
        private final CustomData customData = new CustomData();
        private final List<MethodHandle> start = new ArrayList<>();
        private final List<MethodHandle> shutdown = new ArrayList<>();
        private final List<MethodHandle> shutdownNow = new ArrayList<>();

        private volatile boolean isRunning;

        private static void invoke(List<MethodHandle> handles) {
            for(MethodHandle methodHandle : handles) {
                try {
                    methodHandle.invokeExact();
                } catch (Error e) {
                    throw e;
                } catch (Throwable throwable) {
                    throw new MetaInvokeException(throwable);
                }
            }
        }

        @Override
        public void start() throws MetaInvokeException {
            if(isRunning)
                return;
            invoke(start);
            isRunning = true;
        }

        @Override
        public void shutdown() throws MetaInvokeException {
            if(!isRunning)
                return;
            invoke(shutdown);
            isRunning = false;
        }

        @Override
        public void shutdownNow() throws MetaInvokeException {
            if(!isRunning)
                return;
            invoke(shutdownNow);
            isRunning = false;
        }

        @Override
        public boolean isRunning() {
            return isRunning;
        }
    }
}
