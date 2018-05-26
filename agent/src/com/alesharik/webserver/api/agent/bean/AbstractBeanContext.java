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

package com.alesharik.webserver.api.agent.bean;

import com.alesharik.webserver.api.reflection.ReflectUtils;
import com.alesharik.webserver.api.statistics.AtomicCounter;
import com.alesharik.webserver.api.statistics.Counter;
import com.alesharik.webserver.base.bean.Bean;
import com.alesharik.webserver.base.bean.BeanFactory;
import com.alesharik.webserver.base.bean.DefaultConstructor;
import com.alesharik.webserver.base.bean.InvocationContext;
import com.alesharik.webserver.base.bean.Wire;
import com.alesharik.webserver.base.bean.context.BeanContext;
import com.alesharik.webserver.base.bean.context.BeanContextMXBean;
import com.alesharik.webserver.base.bean.context.BeanContextManager;
import com.alesharik.webserver.base.bean.meta.BeanObject;
import com.alesharik.webserver.base.bean.meta.BeanSingleton;
import com.alesharik.webserver.internals.instance.ClassInstantiator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import sun.misc.Cleaner;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractBeanContext implements BeanContext {
    protected static final Map<Class<?>, Helper> helpers = new ConcurrentHashMap<>();

    @Getter
    protected final String name;

    protected final Map<String, Object> props = new ConcurrentHashMap<>();

    protected final Map<Class<?>, BeanSingleton> singletons = new ConcurrentHashMap<>();
    protected final List<BeanObject> objects = new CopyOnWriteArrayList<>();

    protected final Management mx = new Management(singletons, objects);
    protected final InvocationContextImpl invocationContext;

    public AbstractBeanContext(String name) {
        this.name = name;
        BeanContextManager contextManager = Contexts.getContextManager(this);
        invocationContext = new InvocationContextImpl(this, contextManager);
    }

    protected static Helper getHelper(Class<?> clazz, Bean beanOverride) {
        if(!clazz.isAnnotationPresent(Bean.class) && beanOverride == null)
            return null;
        return helpers.computeIfAbsent(clazz, claz -> new Helper(claz, beanOverride));
    }

    @Override
    public void setProperty(@Nonnull String key, @Nullable Object value) {
        if(value == null)
            props.remove(key);
        else
            props.put(key, value);
    }

    @Nullable
    @Override
    public Object getProperty(@Nonnull String key) {
        return props.get(key);
    }

    @Override
    public void removeProperty(@Nonnull String key) {
        props.remove(key);
    }

    @Nonnull
    @Override
    public List<BeanObject> storedObjects() {
        return objects;
    }

    @Nonnull
    @Override
    public Map<Class<?>, BeanSingleton> singletons() {
        return singletons;
    }

    @Override
    public <T> T getSingleton(Class<?> singleton, Bean beanOverride) {
        BeanContextManager contextManager = Contexts.getContextManager(this);
        {
            Class<?> override = contextManager.overrideBeanClass(singleton);
            if(override != null)
                singleton = override;
        }

        if(singletons.containsKey(singleton))
            //noinspection unchecked
            return (T) singletons.get(singleton).getObject();

        Helper helper = getHelper(singleton, beanOverride);
        if(helper == null)
            return null;
        Object o = helper.create(this, contextManager);
        helper.wire(o, contextManager, this);
        helper.postConstruct(o, invocationContext);
        SingletonImpl singleton1 = new SingletonImpl(o, helper, this);
        singletons.put(singleton, singleton1);
        Cleaner.create(o, () -> helper.preDestroy(o, invocationContext));
        mx.incrementCreatedObjects();
        //noinspection unchecked
        return (T) o;
    }

    @Override
    public <T> T createObject(Class<?> bean, Bean beanOverride) {
        BeanContextManager contextManager = Contexts.getContextManager(this);
        {
            Class<?> override = contextManager.overrideBeanClass(bean);
            if(override != null)
                bean = override;
        }

        Helper helper = getHelper(bean, beanOverride);
        if(helper == null)
            return null;
        Object o = helper.create(this, contextManager);
        helper.wire(o, contextManager, this);
        helper.postConstruct(o, new InvocationContextImpl(this, contextManager));
        if(!helper.getBeanInfo().canBeGarbage()) {
            BeanObject object = new ObjectImpl(o, helper, this);
            objects.add(object);
        }
        Cleaner.create(o, () -> helper.preDestroy(o, invocationContext));
        mx.incrementCreatedObjects();
        //noinspection unchecked
        return (T) o;
    }

    @Override
    public <T> T getBean(Class<?> clazz, @Nullable Bean beanOverride) {
        Helper helper = getHelper(clazz, beanOverride);
        if(helper == null)
            return null;
        if(helper.getBeanInfo().singleton())
            return getSingleton(clazz, beanOverride);
        else
            return createObject(clazz, beanOverride);
    }

    @Nonnull
    @Override
    public BeanContextMXBean getStats() {
        return mx;
    }

    @Override
    public boolean isLoadedBy(@Nonnull ClassLoader classLoader) {
        if(this.getClass().getClassLoader() == classLoader)
            return true;
        for(Class<?> aClass : singletons.keySet())
            if(aClass.getClassLoader() == classLoader)
                return true;
        for(BeanObject object : objects)
            if(object.getClass().getClassLoader() == classLoader)
                return true;
        return false;
    }

    @Override
    public void preDestroy() {
        singletons.forEach((aClass, beanSingleton) -> helpers.remove(aClass));
        objects.forEach(beanObject -> helpers.remove(beanObject.getClass()));
    }

    protected static class Helper {
        private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
        private final List<MethodWrapper> postConstruct = new ArrayList<>();
        private final List<MethodWrapper> preDestroy = new ArrayList<>();
        private final List<Field> toWire = new ArrayList<>();
        private final Constructor<?> ctor;
        @Getter
        private final Bean beanInfo;
        private final Class<?> clazz;

        public Helper(Class<?> clazz, @Nullable Bean beanOverride) {
            this.clazz = clazz;
            Bean bean = clazz.getAnnotation(Bean.class);
            if(bean == null)
                bean = beanOverride;
            if(bean == null)
                throw new IllegalArgumentException("Class" + clazz.getCanonicalName() + " is not a bean!");
            this.beanInfo = bean;
            for(Field field : ReflectUtils.getAllDeclaredFields(clazz)) {
                if(Modifier.isFinal(field.getModifiers()))
                    continue;
                field.setAccessible(true);
                Wire fieldAnnotation = field.getAnnotation(Wire.class);
                if((bean.autowire() && (fieldAnnotation == null || fieldAnnotation.value())) || (!bean.autowire() && fieldAnnotation != null && fieldAnnotation.value()))
                    toWire.add(field);
            }
            for(Method method : ReflectUtils.getAllDeclaredMethods(clazz)) {
                method.setAccessible(true);
                if(method.isAnnotationPresent(PreDestroy.class))
                    preDestroy.add(new MethodWrapper(method));
                if(method.isAnnotationPresent(PostConstruct.class))
                    postConstruct.add(new MethodWrapper(method));
            }
            Constructor<?> ctor = null;
            for(Constructor<?> constructor : clazz.getDeclaredConstructors()) {
                if(constructor.isAnnotationPresent(DefaultConstructor.class)) {
                    constructor.setAccessible(true);
                    ctor = constructor;
                    break;
                }
                if(ctor == null)
                    ctor = constructor;
                else {
                    if(constructor.getParameterCount() < ctor.getParameterCount())
                        ctor = constructor;
                }
            }
            if(ctor != null)
                ctor.setAccessible(true);
            this.ctor = ctor;
        }

        public void preDestroy(Object o, InvocationContext context) {
            for(MethodWrapper methodWrapper : preDestroy) {
                try {
                    methodWrapper.invoke(o, context);
                } catch (Error e) {
                    throw e;
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }

        public void postConstruct(Object o, InvocationContext context) {
            for(MethodWrapper methodWrapper : postConstruct) {
                try {
                    methodWrapper.invoke(o, context);
                } catch (Error e) {
                    throw e;
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }

        @SneakyThrows(IllegalAccessException.class)
        public Object create(BeanContext context, BeanContextManager contextManager) {
            if(beanInfo.factory().isInterface()) {
                if(ctor == null)
                    throw new IllegalArgumentException("Constructor not found!");
                Class<?>[] params = ctor.getParameterTypes();
                Object[] args = new Object[params.length];
                for(int i = 0; i < params.length; i++) {
                    Class<?> param = params[i];
                    Class<?> n = contextManager.overrideBeanClass(param);
                    if(n != null)
                        param = n;
                    args[i] = context.getBean(param);
                }
                try {
                    return ctor.newInstance(args);
                } catch (InstantiationException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            } else {
                BeanFactory factory = contextManager.overrideFactory(clazz);
                if(factory == null)
                    factory = (BeanFactory) ClassInstantiator.instantiate(beanInfo.factory());
                return factory.createBean();
            }
        }


        @SneakyThrows(IllegalAccessException.class)
        public void wire(Object o, BeanContextManager contextManager, BeanContext context) {
            for(Field field : toWire) {
                Class<?> type = field.getType();
                Class<?> n = contextManager.overrideBeanClass(type);
                if(n != null)
                    type = n;
                if(BeanContextManager.class.isAssignableFrom(type)) {
                    field.set(o, contextManager);
                    continue;
                }
                if(BeanContext.class.isAssignableFrom(type)) {
                    field.set(o, context);
                    continue;
                }
                if("me".equals(field.getName())) {
                    field.set(o, o);
                    continue;
                }
                Object bean = context.getBean(type);
                if(bean != null)
                    field.set(o, bean);
            }
        }


        private static final class MethodWrapper {
            private final MethodHandle methodHandle;
            private final boolean needContext;
            private final Class<?> ctxType;

            @SneakyThrows
            public MethodWrapper(Method methodHandle) {
                methodHandle.setAccessible(true);
                this.methodHandle = LOOKUP.unreflect(methodHandle);
                this.needContext = methodHandle.getParameterCount() > 0;
                this.ctxType = needContext ? methodHandle.getParameterTypes()[0] : null;
            }

            public void invoke(Object o, InvocationContext context) throws Throwable {
                if(needContext)
                    methodHandle.bindTo(o).invoke(ctxType.cast(context));
                else
                    methodHandle.bindTo(o).invoke(context);
            }
        }
    }

    @RequiredArgsConstructor
    protected static class SingletonImpl implements BeanSingleton {
        @Getter
        protected final Object object;
        protected final Helper helper;
        @Getter
        protected final BeanContext context;
    }

    @RequiredArgsConstructor
    protected static class ObjectImpl implements BeanObject {
        @Getter
        protected final Object object;
        protected final Helper helper;
        @Getter
        protected final BeanContext context;
    }

    @RequiredArgsConstructor
    @Getter
    protected static final class InvocationContextImpl implements InvocationContext {
        private final BeanContext context;
        private final BeanContextManager manager;
    }

    @RequiredArgsConstructor
    protected static class Management implements BeanContextMXBean {
        private final Map<Class<?>, BeanSingleton> singletons;
        private final List<BeanObject> objects;
        private final Counter createdObjects = new AtomicCounter();

        @Override
        public int getSingletons() {
            return singletons.size();
        }

        @Override
        public int getStoredObjects() {
            return objects.size();
        }

        @Override
        public void clearStoredObjects() {
            objects.clear();
        }

        @Override
        public long getCreatedObjects() {
            return createdObjects.getAmount();
        }

        public void incrementCreatedObjects() {
            createdObjects.add();
        }
    }
}
