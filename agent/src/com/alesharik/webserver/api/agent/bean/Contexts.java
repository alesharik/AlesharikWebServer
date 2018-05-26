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

import com.alesharik.webserver.api.agent.classPath.ClassPathScanner;
import com.alesharik.webserver.api.agent.classPath.reload.UnloadClassLoaderHandler;
import com.alesharik.webserver.base.bean.context.BeanContext;
import com.alesharik.webserver.base.bean.context.BeanContextManager;
import com.alesharik.webserver.base.bean.context.Manager;
import com.alesharik.webserver.internals.instance.ClassInstantiator;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefixes;
import com.alesharik.webserver.logger.level.Level;
import lombok.experimental.UtilityClass;
import lombok.val;

import javax.annotation.Nonnull;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@UtilityClass
@Prefixes({"[BeanManagement]", "[Contexts]"})
@Level("beans")
@ClassPathScanner
public class Contexts {
    private static final RefPooler REF_POOLER = new RefPooler();

    private static final List<BeanContext> contexts = new CopyOnWriteArrayList<>();

    private static final ReferenceQueue<BeanContext> contextRefQueue = new ReferenceQueue<>();
    private static final List<WeakReference<BeanContext>> contextRefs = new CopyOnWriteArrayList<>();

    private static final Map<Class<?>, BeanContextManager> contextManagers = new ConcurrentHashMap<>();

    private static final BeanContext DEFAULT_BEAN_CONTEXT;

    static {
        Logger.getLoggingLevelManager().createLoggingLevel("beans");
        REF_POOLER.start();

        DEFAULT_BEAN_CONTEXT = createContext(DefaultBeanContext.class);
    }

    @Nonnull
    public static BeanContext getDefaultBeanContext() {
        return DEFAULT_BEAN_CONTEXT;
    }

    @Nonnull
    public static BeanContext createContext(@Nonnull Class<? extends BeanContext> clazz) {
        BeanContextManager contextManager = contextManagers.get(clazz);
        if(contextManager == null) {
            Manager manager = clazz.getAnnotation(Manager.class);
            contextManager = (BeanContextManager) ClassInstantiator.instantiate(manager.value());
            contextManagers.put(clazz, contextManager);
        }

        BeanContext context = contextManager.createContext();

        WeakReference<BeanContext> ref = new WeakReference<>(context, contextRefQueue);
        contextRefs.add(ref);

        return context;
    }

    public static void destroyContext(@Nonnull BeanContext context) {
        for(WeakReference<BeanContext> contextRef : contextRefs) {
            if(contextRef.get() == context) {
                contextRef.enqueue();
                contextRefs.remove(contextRef);
            }
        }
        contexts.remove(context);
        context.preDestroy();
        BeanContextManager contextManager = contextManagers.get(context.getClass());
        if(contextManager == null)
            System.err.println("Context " + context.getClass().getCanonicalName() + " must have ContextManager!");
        else
            contextManager.destroyContext(context);
    }

    public static List<BeanContext> getContexts(String name) {
        List<BeanContext> list = new ArrayList<>();
        for(BeanContext context : contexts)
            if(context.getName().equals(name))
                list.add(context);
        return list;
    }

    public static BeanContextManager getContextManager(BeanContext context) {
        return contextManagers.get(context.getClass());
    }

    @UnloadClassLoaderHandler
    static void unloadClassLoader(ClassLoader classLoader) {
        for(BeanContext context : contexts) {
            if(context.isLoadedBy(classLoader))
                destroyContext(context);
        }
    }

    @Prefixes({"[BeanManagement]", "[Contexts]", "[RefPooler]"})
    @Level("beans")
    static final class RefPooler extends Thread {
        public RefPooler() {
            setName("BeanContextRefPooler");
            setDaemon(true);
            setPriority(Thread.MIN_PRIORITY);
        }

        @Override
        public void run() {
            System.out.println("Ref Pooler started");
            while(isAlive() && !isInterrupted()) {
                try {
                    val reference = contextRefQueue.remove();
                    //noinspection unchecked
                    processReference((WeakReference<BeanContext>) reference);
                } catch (InterruptedException e) {
                    System.out.println("Ref Pooler was interrupted!");
                    return;
                }
            }
            System.out.println("Ref Pooler was stopped");
        }

        static void processReference(WeakReference<BeanContext> reference) {
            contextRefs.remove(reference);
            BeanContext context = reference.get();
            contexts.remove(context);
            context.preDestroy();
            BeanContextManager contextManager = contextManagers.get(context.getClass());
            if(contextManager == null)
                System.err.println("Context " + context.getClass().getCanonicalName() + " must have ContextManager!");
            else
                contextManager.destroyContext(context);
        }
    }
}
