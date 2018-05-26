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
import com.alesharik.webserver.base.exception.DevError;
import com.alesharik.webserver.configuration.module.Shutdown;
import com.alesharik.webserver.configuration.module.ShutdownNow;
import com.alesharik.webserver.configuration.module.Start;
import com.alesharik.webserver.configuration.module.layer.SubModule;
import com.alesharik.webserver.configuration.module.meta.CustomData;
import com.alesharik.webserver.configuration.module.meta.MetaInvokeException;
import com.alesharik.webserver.exception.error.UnexpectedBehaviorError;
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
import java.lang.reflect.Modifier;
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
@Level("module-meta-factory")
@SuppressClassLoaderUnloadWarning //can't reload SubModuleProcessors because they are loaded at CORE_MODULES stage
public class SubModuleMetaFactory {
    static final List<SubModuleProcessor> processors = new ArrayList<>();
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final Lock writeLock = new ReentrantLock();

    static {
        Logger.getLoggingLevelManager().createLoggingLevel("module-meta-factory");
    }

    /**
     * Wrap SubModule object into {@link SubModuleAdapter}
     *
     * @param o the object
     * @return the adapter
     * @throws IllegalArgumentException if object doesn't have {@link SubModule} annotation
     */
    @Nonnull
    public static SubModuleAdapter create(@Nonnull Object o) {
        Class<?> clazz = o.getClass();
        SubModule annotation = clazz.getAnnotation(SubModule.class);
        if(annotation == null)
            throw new IllegalArgumentException("Class " + clazz.getCanonicalName() + " is not a SubModule!");

        SubModuleAdapterImpl adapter = new SubModuleAdapterImpl(annotation.value());
        for(Method method : ReflectUtils.getAllDeclaredMethods(o.getClass())) {
            if(Modifier.isStatic(method.getModifiers()))
                continue;
            try {
                method.setAccessible(true);
                if(method.isAnnotationPresent(Start.class)) {
                    if(method.getParameterCount() != 0)
                        throw new DevError("Unexpected method parameters in method " + method.getName(), "@Start method in submodule MUST contains only 0 parameters. Method: " + method.getName(), o.getClass());

                    adapter.start.add(LOOKUP.unreflect(method).bindTo(o));
                }
                if(method.isAnnotationPresent(Shutdown.class)) {
                    if(method.getParameterCount() != 0)
                        throw new DevError("Unexpected method parameters in method " + method.getName(), "@Shutdown method in submodule MUST contains only 0 parameters. Method: " + method.getName(), o.getClass());
                    adapter.shutdown.add(LOOKUP.unreflect(method).bindTo(o));
                }
                if(method.isAnnotationPresent(ShutdownNow.class)) {
                    if(method.getParameterCount() != 0)
                        throw new DevError("Unexpected method parameters in method " + method.getName(), "@ShutdownNow method in submodule MUST contains only 0 parameters. Method: " + method.getName(), o.getClass());
                    adapter.shutdownNow.add(LOOKUP.unreflect(method).bindTo(o));
                }
            } catch (IllegalAccessException e) {
                throw new UnexpectedBehaviorError(e);
            }
        }

        for(SubModuleProcessor processor : processors) {
            try {
                processor.processSubModule(adapter, o);
            } catch (RuntimeException e) {
                System.err.println("Exception in SubModuleProcessor!");
                e.printStackTrace(System.err);
            }
        }

        return adapter;
    }

    @ListenInterface(SubModuleProcessor.class)
    @Stages({ExecutionStage.AGENT, ExecutionStage.PRE_LOAD, ExecutionStage.CORE_MODULES})
    static void listenClass(Class<?> clazz) {
        System.out.println("Processing " + clazz.getCanonicalName());

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

        @Getter
        private volatile boolean running;

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
            if(running)
                throw new IllegalStateException("SubModule is already running!");
            invoke(start);
            running = true;
        }

        @Override
        public void shutdown() throws MetaInvokeException {
            if(!running)
                throw new IllegalStateException("SubModule is not running!");
            invoke(shutdown);
            running = false;
        }

        @Override
        public void shutdownNow() throws MetaInvokeException {
            if(!running)
                throw new IllegalStateException("SubModule is not running!");
            invoke(shutdownNow);
            running = false;
        }
    }
}
