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

package com.alesharik.webserver.configuration.extension;

import com.alesharik.webserver.api.ExecutionStage;
import com.alesharik.webserver.api.agent.Stages;
import com.alesharik.webserver.api.agent.bean.Beans;
import com.alesharik.webserver.api.agent.classPath.ClassPathScanner;
import com.alesharik.webserver.api.agent.classPath.ListenInterface;
import com.alesharik.webserver.api.agent.classPath.reload.UnloadClassLoaderHandler;
import com.alesharik.webserver.api.documentation.PrivateApi;
import com.alesharik.webserver.configuration.extension.message.MessageManager;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefixes;
import com.alesharik.webserver.logger.level.Level;
import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;

@UtilityClass
@Prefixes({"[Extensions]", "[ExtensionManager]"})
@Level("extension-manager")
@ClassPathScanner
public class ExtensionManager {
    private static final Map<String, Class<?>> extensions = new ConcurrentHashMap<>();
    private static final List<MessageManager> global = new CopyOnWriteArrayList<>();
    private static final List<Listener> listeners = new CopyOnWriteArrayList<>();

    static {
        Logger.getLoggingLevelManager().createLoggingLevel("extension-manager");
    }

    @ListenInterface(Extension.class)
    @Stages({ExecutionStage.AGENT, ExecutionStage.PRE_LOAD, ExecutionStage.CORE_MODULES})
    static void listen(Class<?> clazz) {
        System.out.println("Listening " + clazz.getCanonicalName());

        if(clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers()))
            return;

        if(!clazz.isAnnotationPresent(Extension.Name.class)) {
            System.err.println("Class " + clazz.getCanonicalName() + " doesn't have @Name annotation! Ignoring...");
            return;
        }

        extensions.put(clazz.getAnnotation(Extension.Name.class).value(), clazz);
    }

    @UnloadClassLoaderHandler
    static void unload(ClassLoader classLoader) {
        for(MessageManager messageManager : global) {
            if(messageManager.getClass().getClassLoader() == classLoader)
                global.remove(messageManager);
        }
        for(Listener listener : listeners) {//be safe from users
            if(listener.getClass().getClassLoader() == classLoader)
                listeners.remove(listener);
        }
    }

    @Nonnull
    public static Set<String> getExtensions() {
        return extensions.keySet();
    }

    @Nullable
    public static Extension createExtension(@Nonnull String name) {
        return extensions.containsKey(name) ? (Extension) Beans.create(extensions.get(name)) : null;
    }

    /**
     * Register global message managers. Global message managers execute on {@link ForkJoinPool#commonPool()}
     *
     * @param messageManager the manager
     */
    public static void registerMessageManager(@Nonnull MessageManager messageManager) {
        global.add(messageManager);
        for(Listener listener : listeners)
            listener.messageManagerAdded(messageManager);
    }

    public static void unregisterMessageManager(@Nonnull MessageManager messageManager) {
        global.remove(messageManager);
    }

    @Nonnull
    public static List<MessageManager> getGlobalMessageManagers() {
        return global;
    }

    @PrivateApi
    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    @PrivateApi
    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    @PrivateApi
    public interface Listener {
        void messageManagerAdded(MessageManager messageManager);
    }
}
