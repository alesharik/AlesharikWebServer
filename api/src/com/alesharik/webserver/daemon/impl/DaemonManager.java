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

import com.alesharik.webserver.api.agent.classPath.ClassPathScanner;
import com.alesharik.webserver.api.agent.classPath.ListenAnnotation;
import com.alesharik.webserver.daemon.Daemon;
import com.alesharik.webserver.daemon.DaemonApiWrapper;
import com.alesharik.webserver.daemon.DaemonLifecycleManager;
import com.alesharik.webserver.daemon.HookProvider;
import lombok.experimental.UtilityClass;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@ClassPathScanner
@UtilityClass
public class DaemonManager {
    private static final Map<String, Class<?>> daemonClasses = new ConcurrentHashMap<>();
    private static final Map<String, DaemonThread> daemons = new ConcurrentHashMap<>();
    private static final ThreadGroup THREAD_GROUP = new ThreadGroup("daemons");
    private static volatile ClassLoader parent;

    public static void setParent(ClassLoader classLoader) {
        if(parent == null)
            parent = classLoader;
        else
            throw new IllegalStateException("Classloader already initialized!");
    }

    public static List<Daemon> getDaemons() {
        return daemons.values().stream()
                .map(daemonThread -> ((DaemonClassLoader) daemonThread.getContextClassLoader()))
                .map(DaemonClassLoader::getDaemon)
                .collect(Collectors.toList());
    }

    public static List<DaemonClassLoader> getClassLoaders() {
        return daemons.values().stream()
                .map(daemonThread -> ((DaemonClassLoader) daemonThread.getContextClassLoader()))
                .collect(Collectors.toList());
    }

    public static DaemonLifecycleManager loadDaemon(@Nonnull String name, @Nonnull Element config, @Nonnull String type, boolean restart, HookProvider provider) {
        if(!daemonClasses.containsKey(type))
            throw new IllegalArgumentException("Daemon not found");

        DaemonClassLoader daemonClassLoader = new DaemonClassLoader(parent);
        Daemon daemon = DaemonReflectionFactory.createDaemon(daemonClasses.get(type), daemonClassLoader, name, provider);
        daemonClassLoader.setDaemon(daemon);

        DaemonThread daemonThread = new DaemonThread(THREAD_GROUP, daemon);
        daemonThread.setAutoRestart(restart);
        daemons.put(name, daemonThread);
        return new DaemonLifecycleManagerImpl(daemonThread, config);
    }

    public static void reloadDaemon(String name, Element config) {
        if(!daemons.containsKey(name))
            throw new IllegalArgumentException("Daemon not found");
        daemons.get(name).reload(config);
    }

    public static void shutdownAll() {
        for(DaemonThread daemonThread : daemons.values()) {
            daemonThread.shutdown();
        }
    }

    public static DaemonApiWrapper getApi(String name) {
        if(!daemons.containsKey(name))
            throw new IllegalArgumentException("Daemon not found!");
        return ((DaemonClassLoader) daemons.get(name).getContextClassLoader()).getApi();
    }

    @ListenAnnotation(com.alesharik.webserver.daemon.annotation.Daemon.class)
    private static void listen(Class<?> clazz) {
        com.alesharik.webserver.daemon.annotation.Daemon daemon = clazz.getAnnotation(com.alesharik.webserver.daemon.annotation.Daemon.class);
        if(daemonClasses.containsKey(daemon.value()))
            return;
        daemonClasses.put(daemon.value(), clazz);
    }

    private static final class DaemonLifecycleManagerImpl implements DaemonLifecycleManager {
        private final DaemonThread daemonThread;
        private final Element config;

        public DaemonLifecycleManagerImpl(DaemonThread daemonThread, Element config) {
            this.daemonThread = daemonThread;
            this.config = config;
        }

        @Override
        public void start() {
            daemonThread.start(config);
        }

        @Override
        public void shutdown() {
            daemonThread.shutdown();
        }

        @Override
        public boolean isRunning() {
            return daemonThread.isAlive();
        }
    }
}
