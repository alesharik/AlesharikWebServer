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

import com.alesharik.webserver.api.agent.classPath.ListenAnnotation;
import com.alesharik.webserver.api.collections.CachedHashMap;
import com.alesharik.webserver.api.reflection.ReflectUtils;
import com.alesharik.webserver.base.bean.Bean;
import com.alesharik.webserver.base.bean.BeanFactory;
import com.alesharik.webserver.base.bean.Context;
import com.alesharik.webserver.base.bean.DefaultConstructor;
import com.alesharik.webserver.base.bean.InvocationContext;
import com.alesharik.webserver.base.bean.MemoryLeakSafetyException;
import com.alesharik.webserver.base.bean.Wire;
import com.alesharik.webserver.base.bean.context.BeanContext;
import com.alesharik.webserver.base.bean.context.BeanContextManager;
import com.alesharik.webserver.base.bean.context.Manager;
import com.alesharik.webserver.base.bean.context.SuppressMemoryLeakSafety;
import com.alesharik.webserver.base.bean.context.impl.DefaultBeanContext;
import com.alesharik.webserver.internals.instance.ClassInstantiator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import sun.misc.Cleaner;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.ref.WeakReference;
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

/**
 * Implementation for base.beans
 * @see BeanCreator
 * @see Wire
 * @see javax.annotation.PreDestroy
 * @see javax.annotation.PostConstruct
 */
@UtilityClass//TODO tests
public class Beans {//TODO recursion detection
    private static final List<WeakReference<Ctx>> contexts = new CopyOnWriteArrayList<>();
    private static final Map<Class<?>, Singleton> singletons = new ConcurrentHashMap<>();
    private static final Map<Class<?>, BeanCreator> beanCreators = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Helper> helpers = new ConcurrentHashMap<>();
    private static final List<Object> beanRefs = new CopyOnWriteArrayList<>();

    private static final CachedHashMap<Class<?>, BeanFactory> factoryCache = new CachedHashMap<>();

    public static int getRefsCount() {
        return beanRefs.size();
    }

    public static void clearBeanRefs() {
        beanRefs.clear();
    }

    @Nullable
    public static <T> T getBean(Class<T> clazz) {
        if(singletons.containsKey(clazz))
            return clazz.cast(singletons.get(clazz).getInstance());
        if(beanCreators.containsKey(clazz))
            return clazz.cast(beanCreators.get(clazz).getInstance());
        if(isBean(clazz)) {
            listenBean(clazz);
            return getBean(clazz);
        } else
            return null;
    }

    public static BeanContext getContext(Object bean) {
        return getContext(bean.getClass(), true).context;
    }

    private static Ctx getContext(Class<?> bean, boolean useSafety) {
        boolean singleton = isSingleton(bean);
        for(WeakReference<Ctx> context : contexts) {
            if(context.isEnqueued())
                contexts.remove(context);
            else {
                Ctx ctx = context.get();
                if(ctx.isContextOf(bean)) {
                    if(useSafety && singleton && ctx.isMemoryLeakSafetyEnabled())
                        throw new MemoryLeakSafetyException();
                    return ctx;
                }
            }
        }
        Context context = bean.getAnnotation(Context.class);
        Ctx ctx;
        if(context == null)
            ctx = createContext(DefaultBeanContext.class);
        else
            ctx = createContext(context.value());
        contexts.add(new WeakReference<>(ctx));
        return ctx;
    }

    private static Ctx createContext(Class<?> c) {
        Manager manager = c.getAnnotation(Manager.class);
        BeanContextManager mgr = (BeanContextManager) ClassInstantiator.instantiate(manager.value());
        return new Ctx(mgr);
    }

    public static boolean isBean(Class<?> bean) {
        return bean.isAnnotationPresent(Bean.class);
    }

    public static boolean isSingleton(Class<?> bean) {
        return bean.getAnnotation(Bean.class).singleton();
    }

    @ListenAnnotation(BeanCreator.class)
    static void listenBean(Class<?> bean) {
        Bean a = bean.getAnnotation(Bean.class);
        if(a.singleton())
            singletons.put(bean, new Singleton(bean));
        else
            beanCreators.put(bean, new BeanCreator(bean));
    }

    private static Object create(Class<?> cls, boolean singleton, BeanContextManager contextManager) {
        Helper helper = singleton ? new Helper(cls) : helpers.computeIfAbsent(cls, Helper::new);
        return helper.create(contextManager);
    }

    private static void wire(Object o, Ctx context, boolean singleton) {
        Helper helper = singleton ? new Helper(o.getClass()) : helpers.computeIfAbsent(o.getClass(), Helper::new);
        helper.wire(o, context, singleton);
    }

    private static void postConstruct(Object o, InvocationContext context, boolean singleton, Ctx ctx) {
        Helper helper = singleton ? new Helper(o.getClass()) : helpers.computeIfAbsent(o.getClass(), Helper::new);
        helper.postConstruct(o, context, singleton, ctx);
    }

    private static void preDestroy(Object o, InvocationContext context, boolean singleton, Ctx ctx) {
        Helper helper = singleton ? new Helper(o.getClass()) : helpers.computeIfAbsent(o.getClass(), Helper::new);
        helper.preDestroy(o, context, singleton, ctx);
    }

    private static final class Helper {
        private static final MethodHandles.Lookup lookup = MethodHandles.lookup();
        private final List<M> postConstruct = new ArrayList<>();
        private final List<M> preDestroy = new ArrayList<>();
        private final List<Field> toWire = new ArrayList<>();
        private final Constructor<?> ctor;

        public Helper(Class<?> clazz) {
            Bean bean = clazz.getAnnotation(Bean.class);
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
                    preDestroy.add(new M(method));
                if(method.isAnnotationPresent(PostConstruct.class))
                    postConstruct.add(new M(method));
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

        public void preDestroy(Object o, InvocationContext context, boolean singleton, Ctx ctx) {
            for(M methodHandle : preDestroy) {
                if(methodHandle.needContext && singleton) {
                    if(ctx.isMemoryLeakSafetyEnabled())
                        throw new MemoryLeakSafetyException();
                    else
                        ctx.warnMemoryLeak(o.getClass());
                }

                try {
                    methodHandle.invoke(o, context);
                } catch (Error e) {
                    throw e;
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }

        public void postConstruct(Object o, InvocationContext context, boolean singleton, Ctx ctx) {
            for(M methodHandle : postConstruct) {
                if(methodHandle.needContext && singleton) {
                    if(ctx.isMemoryLeakSafetyEnabled())
                        throw new MemoryLeakSafetyException();
                    else
                        ctx.warnMemoryLeak(o.getClass());
                }

                try {
                    methodHandle.invoke(o, context);
                } catch (Error e) {
                    throw e;
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }

        @SneakyThrows(IllegalAccessException.class)
        public Object create(BeanContextManager contextManager) {
            if(ctor == null)
                throw new IllegalArgumentException("Constructor not found!");
            Class<?>[] params = ctor.getParameterTypes();
            Object[] args = new Object[params.length];
            for(int i = 0; i < params.length; i++) {
                Class<?> param = params[i];
                Class<?> n = contextManager.overrideBeanClass(param);
                if(n != null)
                    param = n;
                args[i] = getBean(param);
            }
            try {
                return ctor.newInstance(args);
            } catch (InstantiationException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        @SneakyThrows(IllegalAccessException.class)
        public void wire(Object o, Ctx context, boolean singleton) {
            for(Field field : toWire) {
                Class<?> type = field.getType();
                Class<?> n = context.contextManager.overrideBeanClass(type);
                if(n != null)
                    type = n;
                if(BeanContextManager.class.isAssignableFrom(type)) {
                    field.set(o, context.contextManager);
                    continue;
                }
                if(BeanContext.class.isAssignableFrom(type)) {
                    if(singleton) {
                        if(context.isMemoryLeakSafetyEnabled())
                            throw new MemoryLeakSafetyException();
                        else
                            context.warnMemoryLeak(o.getClass());
                    }
                    field.set(o, context.context);
                    continue;
                }
                if("me".equals(field.getName())) {
                    field.set(o, o);
                    continue;
                }
                Object bean = getBean(type);
                if(bean != null)
                    field.set(o, bean);
            }
        }

        private static final class M {
            private final MethodHandle methodHandle;
            private final boolean needContext;
            private final Class<?> contextCast;

            @SneakyThrows
            public M(Method methodHandle) {
                this.methodHandle = lookup.unreflect(methodHandle);
                this.needContext = methodHandle.getParameterCount() > 0;
                this.contextCast = needContext ? methodHandle.getParameterTypes()[0] : null;
            }

            public void invoke(Object o, InvocationContext context) throws Throwable {
                MethodHandle mh = methodHandle.bindTo(o);
                if(needContext)
                    mh.invokeExact(contextCast.cast(context));
                else
                    mh.invokeExact();
            }
        }
    }

    private static final class Ctx {
        private final BeanContext context;
        @Getter
        private final BeanContextManager contextManager;
        private final boolean memoryLeakSafety;
        private final boolean warn;

        public Ctx(BeanContextManager contextManager) {
            this.context = contextManager.createContext();
            this.contextManager = contextManager;
            Cleaner.create(context, () -> contextManager.destroyContext(context));
            SuppressMemoryLeakSafety suppressMemoryLeakSafety = context.getClass().getAnnotation(SuppressMemoryLeakSafety.class);
            memoryLeakSafety = suppressMemoryLeakSafety == null;
            warn = suppressMemoryLeakSafety == null || suppressMemoryLeakSafety.warning();
        }

        public boolean isContextOf(Class<?> bean) {
            Context c = bean.getAnnotation(Context.class);
            if(c == null)
                return context instanceof DefaultBeanContext;
            else
                return c.value().isAssignableFrom(context.getClass());
        }

        public boolean isMemoryLeakSafetyEnabled() {
            return memoryLeakSafety;
        }

        public void warnMemoryLeak(Class<?> clz) {
            if(warn)
                System.err.println("Singleton " + clz.getCanonicalName() + " tries to access context!");
        }
    }

    private static final class BeanCreator {
        private final Class<?> clazz;
        private final BeanFactory factory;
        private final boolean canBeGarbage;

        public BeanCreator(@Nonnull Class<?> clazz) {
            this.clazz = clazz;
            Bean a = clazz.getAnnotation(Bean.class);
            if(a.singleton())
                throw new IllegalArgumentException("Bean is a singleton!");
            Class<?> factoryClass = a.factory();
            BeanContextManager context = getContext(clazz, false).contextManager;
            BeanFactory factory = context.overrideFactory(factoryClass);
            BeanFactory f = null;
            if(factory == null) {
                if(factoryClass.equals(BeanFactory.class))
                    f = null;
                else {
                    BeanFactory factory1 = factoryCache.get(factoryClass);
                    if(factory1 == null) {
                        f = (BeanFactory) ClassInstantiator.instantiate(factoryClass);
                        factoryCache.put(factoryClass, f);
                    } else
                        f = factory1;
                }
            }
            this.factory = f;
            this.canBeGarbage = a.canBeGarbage();
        }

        @Nonnull
        public Object getInstance() {
            Ctx context1 = getContext(clazz, false);
            Object i = factory != null ? factory.createBean() : create(clazz, false, context1.contextManager);
            wire(i, context1, false);
            postConstruct(i, new InvocationContextImpl(context1), false, context1);
            if(!canBeGarbage)
                beanRefs.add(i);
            Cleaner.create(i, () -> preDestroy(i, new InvocationContextImpl(context1), false, context1));
            return i;
        }
    }

    private static final class Singleton {
        private final Class<?> clazz;
        private volatile Object instance;

        public Singleton(@Nonnull Class<?> clazz) {
            this.clazz = clazz;
            Bean a = clazz.getAnnotation(Bean.class);
            if(!a.singleton())
                throw new IllegalArgumentException("Bean is not a singleton!");
            if(a.instantlyInstantiated())
                instance = createInstance();
        }

        @Nonnull
        public Object getInstance() {
            if(instance == null)
                return instance = createInstance();
            else
                return instance;
        }

        private Object createInstance() {
            Bean a = clazz.getAnnotation(Bean.class);
            Class<?> factoryClass = a.factory();
            Ctx context1 = getContext(clazz, false);
            BeanContextManager context = context1.contextManager;
            BeanFactory factory = context.overrideFactory(factoryClass);
            if(factory == null) {
                if(factoryClass.equals(BeanFactory.class))
                    factory = null;
                else {
                    BeanFactory factory1 = factoryCache.get(factoryClass);
                    if(factory1 == null) {
                        factory = (BeanFactory) ClassInstantiator.instantiate(factoryClass);
                        factoryCache.put(factoryClass, factory);
                    } else
                        factory = factory1;
                }
            }
            Object i = factory != null ? factory.createBean() : create(clazz, true, context);
            wire(i, context1, true);
            postConstruct(i, new InvocationContextImpl(context1), true, context1);
            return i;
        }
    }

    @RequiredArgsConstructor
    private static final class InvocationContextImpl implements InvocationContext {
        private final Ctx context;

        @Nonnull
        @Override
        public BeanContext getContext() {
            return context.context;
        }

        @Nonnull
        @Override
        public BeanContextManager getManager() {
            return context.contextManager;
        }
    }
}
