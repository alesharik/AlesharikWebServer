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

package com.alesharik.webserver.daemon.impl;

import com.alesharik.webserver.api.mx.bean.MXBeanManager;
import com.alesharik.webserver.api.reflection.ReflectUtils;
import com.alesharik.webserver.daemon.Daemon;
import com.alesharik.webserver.daemon.DaemonApi;
import com.alesharik.webserver.daemon.DaemonManagementBean;
import com.alesharik.webserver.daemon.HookProvider;
import com.alesharik.webserver.daemon.annotation.Api;
import com.alesharik.webserver.daemon.annotation.EventManager;
import com.alesharik.webserver.daemon.annotation.HookManager;
import com.alesharik.webserver.daemon.annotation.ManagementBean;
import com.alesharik.webserver.daemon.annotation.Parse;
import com.alesharik.webserver.daemon.annotation.Priority;
import com.alesharik.webserver.daemon.annotation.Reload;
import com.alesharik.webserver.daemon.annotation.Run;
import com.alesharik.webserver.daemon.annotation.Setup;
import com.alesharik.webserver.daemon.annotation.Shutdown;
import com.alesharik.webserver.daemon.hook.DaemonHookManager;
import com.alesharik.webserver.exceptions.error.UnexpectedBehaviorError;
import com.alesharik.webserver.hook.Hook;
import com.alesharik.webserver.internals.ClassInstantiator;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.NamedLogger;
import com.alesharik.webserver.logger.storing.DisabledStoringStrategy;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import org.w3c.dom.Element;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Priority: field -> method -> class
 */
@UtilityClass
class DaemonReflectionFactory {
    private static final int DEFAULT_PRIORITY = Thread.NORM_PRIORITY - 2;
    private static final Map<String, ObjectReflectionWrapper> wrappers = new ConcurrentHashMap<>();

    static Daemon<?> createDaemon(Class<?> clazz, ClassLoader classLoader, String name, HookProvider provider) {
        if(clazz.isPrimitive() || clazz.isArray())
            throw new IllegalArgumentException("Class must be reference type!");

        try {
            Class<?> wrap = classLoader.loadClass(clazz.getName());
            if(!wrap.isAnnotationPresent(com.alesharik.webserver.daemon.annotation.Daemon.class))
                throw new IllegalArgumentException("Class is not a daemon!");

            com.alesharik.webserver.daemon.annotation.Daemon daemonAnnotation = wrap.getAnnotation(com.alesharik.webserver.daemon.annotation.Daemon.class);
            int priority = wrap.isAnnotationPresent(Priority.class) ? wrap.getAnnotation(Priority.class).value() : DEFAULT_PRIORITY;

            ObjectReflectionWrapper reflect = getWrapper(clazz, wrap);
            Object instance = ClassInstantiator.instantiateNullConstructor(wrap);
            NamedLogger logger = Logger.createNewNamedLogger("Daemon][" + daemonAnnotation.value(), null);
            logger.setStoringStrategyFactory(DisabledStoringStrategy::new);

            return new DaemonImpl(instance, daemonAnnotation.value(), name, reflect, logger, priority, provider);
        } catch (ClassNotFoundException e) {
            throw new UnexpectedBehaviorError(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static ObjectReflectionWrapper getWrapper(Class<?> clazz, Class<?> wrap) throws IllegalAccessException {
        if(wrappers.containsKey(clazz.getName()))
            return wrappers.get(clazz.getName());
        else {
            ObjectReflectionWrapper reflect = new ObjectReflectionWrapper(wrap);
            wrappers.put(clazz.getName(), reflect);
            return reflect;
        }
    }

    static final class ObjectReflectionWrapper {
        private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

        private final List<MethodHandle> parseConfigMethodHandle;
        private final List<MethodHandle> setupMethodHandle;
        private MethodHandle runMethodHandle;
        private final List<MethodHandle> shutdownMethodHandle;
        private final List<MethodHandle> reloadMethodHandle;
        private final List<Field> loggers;
        private final List<Field> hookManagers;

        private volatile ObjectContainer apiContainer;
        private volatile ObjectContainer hookContainer;
        private volatile ObjectContainer managementContainer;

        public ObjectReflectionWrapper(Class<?> clazz) throws IllegalAccessException {
            this.parseConfigMethodHandle = new CopyOnWriteArrayList<>();
            this.setupMethodHandle = new CopyOnWriteArrayList<>();
            this.runMethodHandle = null;
            this.shutdownMethodHandle = new CopyOnWriteArrayList<>();
            this.reloadMethodHandle = new CopyOnWriteArrayList<>();

            this.loggers = new CopyOnWriteArrayList<>();
            this.hookManagers = new CopyOnWriteArrayList<>();

            for(Class<?> aClass : ReflectUtils.getAllInnerClasses(clazz)) {
                if(aClass.isAnnotationPresent(Api.class) && aClass.isAssignableFrom(DaemonApi.class))
                    apiContainer = new ObjectContainer.TypeContainer(clazz, DaemonApi.class);
                if(aClass.isAnnotationPresent(HookManager.class) && aClass.isAssignableFrom(DaemonHookManager.class))
                    hookContainer = new ObjectContainer.TypeContainer(clazz, DaemonHookManager.class);
                if(aClass.isAnnotationPresent(ManagementBean.class) && aClass.isAssignableFrom(DaemonManagementBean.class))
                    managementContainer = new ObjectContainer.TypeContainer(clazz, DaemonManagementBean.class);
            }

            for(Method method : ReflectUtils.getAllDeclaredMethods(clazz)) {
                if(method.isAnnotationPresent(Parse.class)) {
                    if(method.getParameterCount() == 1 && method.getParameterTypes()[0].equals(Element.class))
                        parseConfigMethodHandle.add(LOOKUP.unreflect(method));
                    else
                        System.err.println("Method " + method.getName() + " in class " + clazz.getName() + " must have 1 argument - org.w3c.dom.Element");
                }
                if(method.isAnnotationPresent(Reload.class)) {
                    if(method.getParameterCount() == 1 && method.getParameterTypes()[0].equals(Element.class))
                        reloadMethodHandle.add(LOOKUP.unreflect(method));
                    else
                        System.err.println("Method " + method.getName() + " in class " + clazz.getName() + " must have 1 argument - org.w3c.dom.Element");
                }
                if(method.isAnnotationPresent(Setup.class)) {
                    if(method.getParameterCount() == 0)
                        setupMethodHandle.add(LOOKUP.unreflect(method));
                    else
                        System.err.println("Method " + method.getName() + " in class " + clazz.getName() + " must have 0 arguments");
                }
                if(method.isAnnotationPresent(Run.class)) {
                    if(method.getParameterCount() == 0) {
                        if(runMethodHandle != null)
                            System.err.println("Method run already defined in class " + clazz.getName());
                        else
                            runMethodHandle = LOOKUP.unreflect(method);
                    } else
                        System.err.println("Method " + method.getName() + " in class " + clazz.getName() + " must have 0 arguments");
                }
                if(method.isAnnotationPresent(Shutdown.class)) {
                    if(method.getParameterCount() == 0)
                        shutdownMethodHandle.add(LOOKUP.unreflect(method));
                    else
                        System.err.println("Method " + method.getName() + " in class " + clazz.getName() + " must have 0 arguments");
                }
                if(method.isAnnotationPresent(Api.class) && method.getReturnType().isAssignableFrom(DaemonApi.class)) {
                    if(apiContainer == null || apiContainer.getType() == ObjectContainer.Type.TYPE)
                        apiContainer = new ObjectContainer.MethodContainer(LOOKUP.unreflect(method));
                }
                if(method.isAnnotationPresent(HookManager.class) && method.getReturnType().isAssignableFrom(DaemonHookManager.class)) {
                    if(hookContainer == null || hookContainer.getType() == ObjectContainer.Type.TYPE)
                        hookContainer = new ObjectContainer.MethodContainer(LOOKUP.unreflect(method));
                }
                if(method.isAnnotationPresent(ManagementBean.class) && method.getReturnType().isAssignableFrom(DaemonManagementBean.class)) {
                    if(managementContainer == null || managementContainer.getType() == ObjectContainer.Type.TYPE)
                        managementContainer = new ObjectContainer.MethodContainer(LOOKUP.unreflect(method));
                }
            }

            for(Field field : ReflectUtils.getAllDeclaredFields(clazz)) {
                if(field.isAnnotationPresent(EventManager.class)) {
                    if(field.getType().isAssignableFrom(EventManager.class))
                        hookManagers.add(field);
                    else
                        System.err.println("Field " + field.getName() + " in class " + clazz.getName() + " must have EventManager type!");
                }
                if(field.isAnnotationPresent(com.alesharik.webserver.daemon.annotation.Logger.class)) {
                    if(field.getType().isAssignableFrom(NamedLogger.class))
                        hookManagers.add(field);
                    else
                        System.err.println("Field " + field.getName() + " in class " + clazz.getName() + " must have NamedLogger type!");
                }
                if(field.isAnnotationPresent(Api.class) && field.getType().isAssignableFrom(DaemonApi.class))
                    apiContainer = new ObjectContainer.FieldContainer(field);
                if(field.isAnnotationPresent(HookManager.class) && field.getType().isAssignableFrom(DaemonHookManager.class))
                    hookContainer = new ObjectContainer.FieldContainer(field);
                if(field.isAnnotationPresent(ManagementBean.class) && field.getType().isAssignableFrom(DaemonManagementBean.class))
                    managementContainer = new ObjectContainer.FieldContainer(field);
            }
        }

        public void parseConfig(Object instance, Element element) {
            for(MethodHandle methodHandle : parseConfigMethodHandle) {
                try {
                    methodHandle.invokeExact(instance, element);
                } catch (Throwable throwable) {
                    if(throwable instanceof Error)
                        throw ((Error) throwable);
                    throwable.printStackTrace();
                }
            }
        }

        public void reloadConfig(Object instance, Element element) {
            if(reloadMethodHandle.size() == 0) {
                shutdown(instance);
                reloadConfig(instance, element);
                setup(instance);
                return;
            }

            for(MethodHandle methodHandle : reloadMethodHandle) {
                try {
                    methodHandle.invokeExact(instance, element);
                } catch (Throwable throwable) {
                    if(throwable instanceof Error)
                        throw ((Error) throwable);
                    throwable.printStackTrace();
                }
            }
        }

        public void setup(Object instance) {
            for(MethodHandle methodHandle : setupMethodHandle) {
                try {
                    methodHandle.invokeExact(instance);
                } catch (Throwable throwable) {
                    if(throwable instanceof Error)
                        throw ((Error) throwable);
                    throwable.printStackTrace();
                }
            }
        }

        public void shutdown(Object instance) {
            for(MethodHandle methodHandle : shutdownMethodHandle) {
                try {
                    methodHandle.invokeExact(instance);
                } catch (Throwable throwable) {
                    if(throwable instanceof Error)
                        throw ((Error) throwable);
                    throwable.printStackTrace();
                }
            }
        }

        public void run(Object instance) {
            if(runMethodHandle == null)
                return;
            try {
                runMethodHandle.invokeExact(instance);
            } catch (Throwable throwable) {
                if(throwable instanceof Error)
                    throw ((Error) throwable);
                throwable.printStackTrace();
            }
        }

        public void setupClass(Object instance, com.alesharik.webserver.daemon.hook.EventManager manager, NamedLogger logger) {
            for(Field field : loggers) {
                try {
                    field.set(instance, logger);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            for(Field hookManager : hookManagers) {
                try {
                    hookManager.set(instance, manager);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        @Nullable
        public DaemonApi getApi(Object instance) {
            if(apiContainer == null)
                return null;
            try {
                return apiContainer.get(instance, DaemonApi.class);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        @Nullable
        public DaemonHookManager getHookManager(Object instance) {
            if(hookContainer == null)
                return null;
            try {
                return hookContainer.get(instance, DaemonHookManager.class);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        @Nullable
        public DaemonManagementBean getManagementBean(Object instance) {
            if(managementContainer == null)
                return null;
            try {
                return managementContainer.get(instance, DaemonManagementBean.class);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        private interface ObjectContainer {
            <T> T get(Object instance, Class<T> clazz) throws IllegalAccessException;

            Type getType();

            enum Type {
                FIELD,
                METHOD,
                TYPE
            }

            final class FieldContainer implements ObjectContainer {
                private final Field field;

                public FieldContainer(Field field) {
                    this.field = field;
                }

                @Override
                public <T> T get(Object instance, Class<T> clazz) throws IllegalAccessException {
                    return clazz.cast(field.get(instance));
                }

                @Override
                public Type getType() {
                    return Type.FIELD;
                }
            }

            final class MethodContainer implements ObjectContainer {
                private final MethodHandle methodHandle;

                /**
                 * @param methodHandle must be with no-args
                 */
                public MethodContainer(MethodHandle methodHandle) {
                    this.methodHandle = methodHandle;
                }

                @Override
                public <T> T get(Object instance, Class<T> clazz) throws IllegalAccessException {
                    try {
                        return clazz.cast(methodHandle.invokeExact(instance));
                    } catch (Throwable throwable) {
                        throw new RuntimeException(throwable);
                    }
                }

                @Override
                public Type getType() {
                    return Type.METHOD;
                }
            }

            final class TypeContainer implements ObjectContainer {
                private final Constructor<?> constructor;

                public TypeContainer(Class<?> clazz, Class<?> type) {
                    Constructor<?> c = null;
                    for(Constructor<?> constructor1 : clazz.getDeclaredConstructors()) {
                        if(constructor1.getParameterCount() == 0) {
                            c = constructor1;
                            break;
                        }
                        if(constructor1.getParameterCount() == 1 && constructor1.getParameterTypes()[0].isAssignableFrom(type)) {
                            c = constructor1;
                            break;
                        }
                    }
                    if(c == null)
                        throw new IllegalArgumentException();
                    this.constructor = c;
                }

                @Override
                public <T> T get(Object instance, Class<T> clazz) throws IllegalAccessException {
                    if(constructor.getParameterCount() == 0)
                        try {
                            return clazz.cast(constructor.newInstance());
                        } catch (InstantiationException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    else if(constructor.getParameterCount() == 1 && instance.getClass().isAssignableFrom(constructor.getParameterTypes()[0]))
                        try {
                            return clazz.cast(constructor.newInstance(instance));
                        } catch (InstantiationException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    else
                        throw new IllegalArgumentException();
                }

                @Override
                public Type getType() {
                    return Type.TYPE;
                }
            }
        }
    }

    static final class DaemonImpl implements Daemon<DaemonApi> {
        private final Object instance;
        private final String type;
        private final String name;
        private final ObjectReflectionWrapper reflect;
        private final NamedLogger logger;
        private final HookMgr hookMgr;
        private final DaemonManagementBeanImpl managementBeanImpl;
        private final int priority;

        private volatile DaemonApi api;
        private volatile DaemonManagementBean managementBean;
        private volatile DaemonHookManager hookManager;

        public DaemonImpl(Object instance, String type, String name, ObjectReflectionWrapper reflect, NamedLogger logger, int priority, HookProvider provider) {
            this.instance = instance;
            this.type = type;
            this.name = name;
            this.reflect = reflect;
            this.logger = logger;
            this.priority = priority;
            this.hookMgr = new HookMgr(this);
            this.managementBeanImpl = new DaemonManagementBeanImpl();
            DaemonHookManager hookManager = reflect.getHookManager(instance);
            if(hookManager == null) {
                reflect.setupClass(instance, hookMgr, logger);
                this.hookManager = hookMgr;
            } else {
                DelegatingEventManager eventManager = new DelegatingEventManager(hookManager, this);
                reflect.setupClass(instance, eventManager, logger);
                this.hookManager = hookManager;
            }
            provider.provide(hookManager::registerHook);
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void parseConfig(Element c) {
            reflect.parseConfig(instance, c);
        }

        @Override
        public void setup() {
            reflect.setup(instance);
        }

        @Override
        public void run() {
            managementBeanImpl.alive = true;
            managementBeanImpl.thread = Thread.currentThread();
            MXBeanManager.registerMXBean(managementBean, DaemonManagementBean.class, name);
            reflect.run(instance);
        }

        @Override
        public void reload(Element element) {
            reflect.reloadConfig(instance, element);
        }

        @Override
        public void shutdown() {
            managementBeanImpl.alive = false;
            managementBeanImpl.thread = null;
            MXBeanManager.unregisterMXBean(name);
            reflect.shutdown(instance);
        }

        @Override
        public DaemonApi getApi() {
            if(this.api != null)
                return this.api;

            DaemonApi api = reflect.getApi(instance);
            if(api == null)
                return DaemonApiStub.STUB;
            this.api = api;
            return api;
        }

        @Override
        public DaemonHookManager getHookManager() {
            return hookManager;
        }

        @Override
        public DaemonManagementBean getManagementBean() {
            if(this.managementBean != null)
                return this.managementBean;

            DaemonManagementBean daemonManagementBean = reflect.getManagementBean(instance);
            if(daemonManagementBean == null)
                daemonManagementBean = managementBeanImpl;
            this.managementBean = daemonManagementBean;
            return daemonManagementBean;
        }

        @Override
        public NamedLogger getNamedLogger() {
            return logger;
        }

        @Override
        public int getPriority() {
            return priority;
        }

        static final class DelegatingEventManager implements com.alesharik.webserver.daemon.hook.EventManager {
            private final DaemonHookManager daemonHookManager;
            private final Object sender;

            public DelegatingEventManager(DaemonHookManager daemonHookManager, Object sender) {
                this.daemonHookManager = daemonHookManager;
                this.sender = sender;
            }

            @Override
            public void fireEvent(String name, Object... args) {
                for(Hook hook : daemonHookManager.getHooks(name)) {
                    try {
                        hook.listen(sender, args);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        static final class HookMgr implements DaemonHookManager, com.alesharik.webserver.daemon.hook.EventManager {
            private final Map<String, List<Hook>> hooks;
            private final Object sender;

            public HookMgr(Object sender) {
                this.sender = sender;
                hooks = new ConcurrentHashMap<>();
            }

            void put(String event, Hook hook) {
                if(hooks.containsKey(event))
                    hooks.get(event).add(hook);
                else {
                    List<Hook> hooks = new ArrayList<>();
                    hooks.add(hook);
                    this.hooks.put(event, hooks);
                }
            }

            void fireStartHook() {
                if(hooks.containsKey("start"))
                    for(Hook hook : hooks.get("start")) {
                        try {
                            hook.listen(sender, new Object[0]);
                        } catch (Exception e) {
                            System.err.println("Exception in hook " + hook.getName() + ", group " + hook.getGroup());
                            e.printStackTrace();
                        }
                    }
            }

            void fireShutdownHook() {
                if(hooks.containsKey("shutdown"))
                    for(Hook hook : hooks.get("shutdown")) {
                        try {
                            hook.listen(sender, new Object[0]);
                        } catch (Exception e) {
                            System.err.println("Exception in hook " + hook.getName() + ", group " + hook.getGroup());
                            e.printStackTrace();
                        }
                    }
            }

            @Override
            public void registerHook(String event, Hook hook) {
                put(event, hook);
            }

            @Override
            public String[] getEvents() {
                return hooks.keySet().toArray(new String[0]);
            }

            @Override
            public int getHookCount(String event) {
                return hooks.containsKey(event) ? hooks.get(event).size() : 0;
            }

            @Override
            public List<Hook> getHooks(String event) {
                return hooks.get(event);
            }

            @Override
            public void fireEvent(String name, Object... args) {
                if(!hooks.containsKey(name))
                    throw new IllegalArgumentException("Event not found!");
                for(Hook hook : hooks.get(name)) {
                    try {
                        hook.listen(sender, args);
                    } catch (Exception e) {
                        System.err.println("Exception in hook " + hook.getName() + ", group " + hook.getGroup());
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static final class DaemonApiStub implements DaemonApi {
        static final DaemonApi STUB = new DaemonApiStub();
    }

    @Setter
    private static final class DaemonManagementBeanImpl implements DaemonManagementBean {
        private volatile Thread thread;
        private volatile boolean alive;

        @Override
        public boolean isAlive() {
            return alive;
        }

        @Override
        public Thread getThread() {
            return thread;
        }
    }
}
