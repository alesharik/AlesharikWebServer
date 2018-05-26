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

package com.alesharik.webserver.configuration.module.meta;

import com.alesharik.webserver.api.ExecutionStage;
import com.alesharik.webserver.api.agent.Stages;
import com.alesharik.webserver.api.agent.bean.Beans;
import com.alesharik.webserver.api.agent.classPath.ClassPathScanner;
import com.alesharik.webserver.api.agent.classPath.ListenAnnotation;
import com.alesharik.webserver.api.agent.classPath.SuppressClassLoaderUnloadWarning;
import com.alesharik.webserver.api.reflection.ReflectUtils;
import com.alesharik.webserver.base.bean.Bean;
import com.alesharik.webserver.base.exception.DevError;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationTypedObject;
import com.alesharik.webserver.configuration.module.Configure;
import com.alesharik.webserver.configuration.module.Module;
import com.alesharik.webserver.configuration.module.Reload;
import com.alesharik.webserver.configuration.module.Shutdown;
import com.alesharik.webserver.configuration.module.ShutdownNow;
import com.alesharik.webserver.configuration.module.Start;
import com.alesharik.webserver.configuration.module.layer.Layer;
import com.alesharik.webserver.configuration.module.layer.SubModule;
import com.alesharik.webserver.configuration.module.layer.meta.LayerAdapter;
import com.alesharik.webserver.configuration.module.layer.meta.LayerMetaFactory;
import com.alesharik.webserver.configuration.module.layer.meta.SubModuleAdapter;
import com.alesharik.webserver.configuration.module.layer.meta.SubModuleMetaFactory;
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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@UtilityClass
@ClassPathScanner
@Prefixes({"[Module]", "[Meta]", "[ModuleMetaFactory]"})
@Level("meta-factory")
@SuppressClassLoaderUnloadWarning
public class ModuleMetaFactory {
    static final List<ModuleProcessor> processors = new ArrayList<>();
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final Lock writeLock = new ReentrantLock();

    static {
        Logger.getLoggingLevelManager().createLoggingLevel("meta-factory");
    }

    @Nonnull
    public static ModuleAdapter create(@Nonnull Object module, @Nonnull ModuleProvider moduleProvider) {
        Module meta = module.getClass().getAnnotation(Module.class);
        if(meta == null)
            throw new IllegalArgumentException("Class " + module.getClass().getCanonicalName() + " is not a module!");

        ConfigurationLinker linker = Beans.getBean(meta.linker(), new Bean.Builder()
                .autowire()
                .singleton()
                .build());

        ModuleAdapterImpl adapter = new ModuleAdapterImpl(meta.autoInvoke(), linker, moduleProvider, module, meta.value());
        for(Method method : ReflectUtils.getAllDeclaredMethods(module.getClass())) {
            if(Modifier.isStatic(method.getModifiers()))
                continue;
            method.setAccessible(true);
            try {
                if(method.isAnnotationPresent(Configure.class)) {
                    if(method.getParameterCount() != 1 || method.getParameterTypes()[0] == ConfigurationTypedObject.class)
                        throw new DevError("Unexpected method parameters in method " + method.getName(), "@Configure method in module MUST contains only one parameter - ConfigurationTypedObject. Method: " + method.getName(), module.getClass());
                    adapter.configure.add(LOOKUP.unreflect(method).bindTo(module));
                }
                if(method.isAnnotationPresent(Reload.class)) {
                    if(method.getParameterCount() != 1 || method.getParameterTypes()[0] == ConfigurationTypedObject.class)
                        throw new DevError("Unexpected method parameters in method " + method.getName(), "@Reload method in module MUST contains only one parameter - ConfigurationTypedObject. Method: " + method.getName(), module.getClass());
                    adapter.reload.add(LOOKUP.unreflect(method).bindTo(module));
                }
                if(method.isAnnotationPresent(Start.class)) {
                    if(method.getParameterCount() != 0)
                        throw new DevError("Unexpected method parameters in method " + method.getName(), "@Start method in module MUST contains only 0 parameters. Method: " + method.getName(), module.getClass());
                    adapter.start.add(LOOKUP.unreflect(method).bindTo(module));
                }
                if(method.isAnnotationPresent(Shutdown.class)) {
                    if(method.getParameterCount() != 0)
                        throw new DevError("Unexpected method parameters in method " + method.getName(), "@Shutdown method in module MUST contains only 0 parameters. Method: " + method.getName(), module.getClass());
                    adapter.shutdown.add(LOOKUP.unreflect(method).bindTo(module));
                }
                if(method.isAnnotationPresent(ShutdownNow.class)) {
                    if(method.getParameterCount() != 0)
                        throw new DevError("Unexpected method parameters in method " + method.getName(), "@ShutdownNow method in module MUST contains only 0 parameters. Method: " + method.getName(), module.getClass());
                    adapter.shutdownNow.add(LOOKUP.unreflect(method).bindTo(module));
                }
            } catch (IllegalAccessException e) {
                throw new UnexpectedBehaviorError(e);
            }
        }
        for(Field field : ReflectUtils.getAllDeclaredFields(module.getClass())) {
            if(Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers()))
                continue;
            field.setAccessible(true);
            try {
                if(field.getClass().isAnnotationPresent(Layer.class)) {
                    Object layer = field.get(module);
                    if(layer == null)
                        throw new DevError("Unexpected field value in field " + field.getName(), "@Layer field in module cannot be null. Field: " + field.getName(), module.getClass());
                    adapter.layers.add(LayerMetaFactory.create(layer));
                }
                if(field.getClass().isAnnotationPresent(SubModule.class)) {
                    Object subModule = field.get(module);
                    if(subModule == null)
                        throw new DevError("Unexpected field value in field " + field.getName(), "@Submodule field in module cannot be null. Field: " + field.getName(), module.getClass());
                    adapter.subModules.add(SubModuleMetaFactory.create(subModule));
                }
            } catch (IllegalAccessException e) {
                throw new UnexpectedBehaviorError(e);
            }
        }

        for(ModuleProcessor processor : processors) {
            try {
                processor.processModule(adapter, module);
            } catch (Exception e) {
                System.err.println("Exception in processor");
                e.printStackTrace(System.err);
            }
        }

        return adapter;
    }

    @ListenAnnotation(ModuleProcessor.class)
    @Stages({ExecutionStage.AGENT, ExecutionStage.PRE_LOAD, ExecutionStage.CORE_MODULES})
    static void listenClass(Class<?> clazz) {
        System.out.println("Processing " + clazz.getCanonicalName());

        ModuleProcessor processor = (ModuleProcessor) Beans.getBean(clazz);
        if(processor == null)
            processor = (ModuleProcessor) ClassInstantiator.instantiate(clazz);

        writeLock.lock();
        try {
            for(ModuleProcessor moduleProcessor : processors) {
                if(moduleProcessor.getClass() == processor.getClass()) {
                    System.out.println("ModuleProcessor " + processor.getClass().getCanonicalName() + " already registered! Skipping...");
                    return;
                }
            }
            processors.add(processor);
        } finally {
            writeLock.unlock();
        }
    }

    @RequiredArgsConstructor
    private static final class ModuleAdapterImpl implements ModuleAdapter {
        private final boolean autoInvoke;
        private final ConfigurationLinker linker;
        private final ModuleProvider moduleProvider;
        private final Object module;

        @Getter
        private final String name;
        @Getter
        private final CustomData data = new CustomData();

        private final List<MethodHandle> start = new ArrayList<>();
        private final List<MethodHandle> shutdown = new ArrayList<>();
        private final List<MethodHandle> shutdownNow = new ArrayList<>();
        private final List<MethodHandle> reload = new ArrayList<>();
        private final List<MethodHandle> configure = new ArrayList<>();

        private final List<LayerAdapter> layers = new ArrayList<>();
        private final List<SubModuleAdapter> subModules = new ArrayList<>();

        private volatile boolean running = false;
        private volatile boolean configured = false;

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

        private static void invoke(List<MethodHandle> handles, ConfigurationTypedObject object) {
            for(MethodHandle methodHandle : handles) {
                try {
                    methodHandle.invokeExact(object);
                } catch (Error e) {
                    throw e;
                } catch (Throwable throwable) {
                    throw new MetaInvokeException(throwable);
                }
            }
        }

        @Override
        public void start() {
            if(running)
                throw new IllegalStateException("Module is already running!");
            if(!autoInvoke) {
                invoke(start);
                return;
            }

            for(LayerAdapter layer : layers) layer.start();
            for(SubModuleAdapter subModule : subModules) subModule.start();
            invoke(start);
            running = true;
        }

        @Override
        public void shutdown() {
            if(!running)
                throw new IllegalStateException("Module is not running!");
            if(!autoInvoke) {
                invoke(shutdown);
                return;
            }

            for(LayerAdapter layer : layers) layer.shutdown();
            for(SubModuleAdapter subModule : subModules) subModule.shutdown();
            invoke(start);
            running = false;
        }

        @Override
        public void shutdownNow() {
            if(!running)
                throw new IllegalStateException("Module is not running!");
            if(!autoInvoke) {
                invoke(shutdownNow);
                return;
            }

            for(LayerAdapter layer : layers) layer.shutdownNow();
            for(SubModuleAdapter subModule : subModules) subModule.shutdownNow();
            invoke(shutdownNow);
            running = false;
        }

        @Override
        public void reload(ConfigurationTypedObject object) {
            if(!configured)
                throw new IllegalStateException("Module is not configured!");
            if(!running)
                throw new IllegalStateException("Module is not running!");
            if(reload.isEmpty()) {
                shutdown();
                configured = false;
                configure(object);
                start();
            } else
                invoke(reload, object);
        }

        @Override
        public void configure(ConfigurationTypedObject object) {
            if(running)
                throw new IllegalStateException("Module is running!");
            if(configured)
                throw new IllegalStateException("Module already configured!");
            linker.link(object, module, moduleProvider);
            invoke(configure, object);
            configured = true;
        }

        @Override
        public boolean isRunning() {
            if(!running)
                return false;
            for(LayerAdapter layer : layers)
                if(!layer.isRunning())
                    return false;
            for(SubModuleAdapter subModule : subModules)
                if(!subModule.isRunning())
                    return false;
            return true;
        }
    }
}
