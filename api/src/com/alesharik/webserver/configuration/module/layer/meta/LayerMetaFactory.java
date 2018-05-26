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
import com.alesharik.webserver.configuration.module.layer.Layer;
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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@UtilityClass
@ClassPathScanner
@Prefixes({"[Module]", "[Meta]", "[LayerMetaFactory]"})
@Level("meta-factory")
@SuppressClassLoaderUnloadWarning
public class LayerMetaFactory {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    static final List<LayerProcessor> processors = new ArrayList<>();
    private static final Lock writeLock = new ReentrantLock();

    static {
        Logger.getLoggingLevelManager().createLoggingLevel("meta-factory");
    }

    /**
     * Convert layer object to {@link LayerAdapter}
     *
     * @param layer the layer object
     * @return layer adapter
     * @throws IllegalArgumentException if object doesn't have {@link SubModule} annotation
     */
    @Nonnull
    public static LayerAdapter create(@Nonnull Object layer) {
        Layer annotation = layer.getClass().getAnnotation(Layer.class);
        if(annotation == null)
            throw new IllegalArgumentException("Class " + layer.getClass().getCanonicalName() + " is not a Layer!");

        LayerAdapterImpl layerAdapter = new LayerAdapterImpl(annotation.value(), annotation.autoInvoke());

        for(Field field : ReflectUtils.getAllDeclaredFields(layer.getClass())) {
            if(Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers()))
                continue;

            field.setAccessible(true);
            try {
                if(field.getType().isAnnotationPresent(SubModule.class)) {
                    Object o = field.get(layer);
                    if(o == null)
                        throw new DevError("Unexpected field value in field " + field.getName(), "@SubModule field in layer cannot be null. Field: " + field.getName(), layer.getClass());
                    layerAdapter.subModules.add(SubModuleMetaFactory.create(o));
                }
                if(field.getType().isAnnotationPresent(Layer.class)) {
                    Object o = field.get(layer);
                    if(o == null)
                        throw new DevError("Unexpected field value in field " + field.getName(), "@Layer field in layer cannot be null. Field: " + field.getName(), layer.getClass());
                    layerAdapter.subLayers.add(LayerMetaFactory.create(o));
                }
            } catch (IllegalAccessException e) {
                throw new UnexpectedBehaviorError(e);
            }
        }

        for(Method method : ReflectUtils.getAllDeclaredMethods(layer.getClass())) {
            if(Modifier.isStatic(method.getModifiers()))
                continue;
            method.setAccessible(true);
            try {
                if(method.isAnnotationPresent(Start.class)) {
                    if(method.getParameterCount() != 0)
                        throw new DevError("Unexpected method parameters in method " + method.getName(), "@Start method in layer MUST contains only 0 parameters. Method: " + method.getName(), layer.getClass());
                    layerAdapter.startHandle.add(LOOKUP.unreflect(method).bindTo(layer));
                }
                if(method.isAnnotationPresent(Shutdown.class)) {
                    if(method.getParameterCount() != 0)
                        throw new DevError("Unexpected method parameters in method " + method.getName(), "@Shutdown method in layer MUST contains only 0 parameters. Method: " + method.getName(), layer.getClass());
                    layerAdapter.shutdownHandle.add(LOOKUP.unreflect(method).bindTo(layer));
                }
                if(method.isAnnotationPresent(ShutdownNow.class)) {
                    if(method.getParameterCount() != 0)
                        throw new DevError("Unexpected method parameters in method " + method.getName(), "@ShutdownNow method in layer MUST contains only 0 parameters. Method: " + method.getName(), layer.getClass());
                    layerAdapter.shutdownNowHandle.add(LOOKUP.unreflect(method).bindTo(layer));
                }
            } catch (IllegalAccessException e) {
                throw new UnexpectedBehaviorError(e);
            }
        }

        for(LayerProcessor processor : processors) {
            try {
                processor.processLayer(layerAdapter, layer);
            } catch (Exception e) {
                System.err.println("Exception in LayerProcessor!");
                e.printStackTrace();
            }
        }

        return layerAdapter;
    }

    @ListenInterface(LayerAdapter.class)
    @Stages({ExecutionStage.AGENT, ExecutionStage.PRE_LOAD, ExecutionStage.CORE_MODULES})
    static void listenClass(Class<?> clazz) {
        System.out.println("Processing " + clazz.getCanonicalName());

        LayerProcessor processor = (LayerProcessor) Beans.getBean(clazz);
        if(processor == null)
            processor = (LayerProcessor) ClassInstantiator.instantiate(clazz);

        writeLock.lock();
        try {
            for(LayerProcessor layerProcessor : processors) {
                if(layerProcessor.getClass() == processor.getClass()) {
                    System.out.println("LayerProcessor " + processor.getClass().getCanonicalName() + " already registered! Skipping...");
                    return;
                }
            }
            processors.add(processor);
        } finally {
            writeLock.unlock();
        }
    }

    @RequiredArgsConstructor
    private static final class LayerAdapterImpl implements LayerAdapter {
        @Getter
        private final String name;
        @Getter
        private final CustomData customData = new CustomData();
        private final boolean autoInvoke;

        @Getter
        private final List<SubModuleAdapter> subModules = new ArrayList<>();
        @Getter
        private final List<LayerAdapter> subLayers = new ArrayList<>();

        private final List<MethodHandle> startHandle = new ArrayList<>();
        private final List<MethodHandle> shutdownHandle = new ArrayList<>();
        private final List<MethodHandle> shutdownNowHandle = new ArrayList<>();

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
            if(isRunning())
                throw new IllegalStateException("Already running!");
            if(!autoInvoke) {
                invoke(startHandle);
                return;
            }

            for(SubModuleAdapter subModule : subModules)
                subModule.start();
            for(LayerAdapter subLayer : subLayers)
                subLayer.start();
            invoke(startHandle);
        }

        @Override
        public void shutdown() throws MetaInvokeException {
            if(!isRunning())
                throw new IllegalStateException("Not running yet!");
            if(!autoInvoke) {
                invoke(shutdownHandle);
                return;
            }
            for(SubModuleAdapter subModule : subModules)
                subModule.shutdown();
            for(LayerAdapter subLayer : subLayers)
                subLayer.shutdown();
            invoke(shutdownHandle);
        }

        @Override
        public void shutdownNow() throws MetaInvokeException {
            if(!isRunning())
                throw new IllegalStateException("Not running yet!");
            if(!autoInvoke) {
                invoke(shutdownNowHandle);
                return;
            }
            for(SubModuleAdapter subModule : subModules)
                subModule.shutdownNow();
            for(LayerAdapter subLayer : subLayers)
                subLayer.shutdownNow();
            invoke(shutdownNowHandle);
        }

        @Override
        public boolean isRunning() {
            for(SubModuleAdapter subModule : subModules) {
                if(!subModule.isRunning())
                    return false;
            }
            for(LayerAdapter subLayer : subLayers) {
                if(!subLayer.isRunning())
                    return false;
            }
            return true;
        }
    }
}
