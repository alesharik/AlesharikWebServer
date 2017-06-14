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

package com.alesharik.webserver.configuration;

import com.alesharik.webserver.api.ThreadFactories;
import com.alesharik.webserver.api.agent.Agent;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefixes;
import one.nio.mgt.Management;
import sun.misc.Cleaner;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

@Prefixes("[PluginManager]")
public final class PluginManagerImpl extends PluginManager {
    private final Map<File, ClassLoader> classLoaders;
    private final ExecutorService executor;
    private final ThreadGroup pluginManagerThreadGroup;
    private final AtomicBoolean isFree;

    public PluginManagerImpl(File workingFolder, boolean hotReloadEnabled) {
        super(workingFolder, hotReloadEnabled);

        isFree = new AtomicBoolean(false);
        pluginManagerThreadGroup = new ThreadGroup("PluginManager");
        classLoaders = new ConcurrentHashMap<>();
        Supplier<String> nameSupplier = ThreadFactories.incrementalSupplier("PluginManagerWorker-");
        executor = Executors.newCachedThreadPool(ThreadFactories.newThreadFactory(pluginManagerThreadGroup, nameSupplier));

        Management.registerMXBean(this, PluginManagerMXBean.class, "com.alesharik.webserver.configuration:type=PluginManager");
        Cleaner.create(this, () -> Management.unregisterMXBean("com.alesharik.webserver.configuration:type=PluginManager"));

        setDaemon(true);
        setName("PluginManager");
    }

    @Override
    public int getLoadedFileCount() {
        return classLoaders.size();
    }

    @Override
    public boolean isHotReloadEnabled() {
        return hotReloadEnabled;
    }

    @Override
    public Map<File, ClassLoader> getClassLoaders() {
        return Collections.unmodifiableMap(classLoaders);
    }

    @Override
    public ThreadGroup getPluginThreadGroup() {
        return pluginManagerThreadGroup;
    }

    @Override
    public boolean isFree() {
        return isFree.get();
    }

    @Override
    public void run() {
        if(!workingFolder.exists()) {
            if(!workingFolder.mkdirs()) {
                Logger.log("Can't create module folder " + workingFolder);
            }
        }
        scanFolder(workingFolder);
        isFree.set(true);
        if(!hotReloadEnabled) {
            return;
        }

        try {
            WatchService watchService = workingFolder.toPath().getFileSystem().newWatchService();
            workingFolder.toPath().register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.OVERFLOW);

            while(isAlive()) {
                WatchKey watchKey = watchService.take();
                isFree.set(false);
                List<WatchEvent<?>> events = watchKey.pollEvents();
                for(WatchEvent<?> event : events) {
                    WatchEvent.Kind<?> kind = event.kind();
                    File file = new File(event.context().toString());
                    if(kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        Logger.log("File " + file + " has been added to plugins folder");
                        if(file.getName().endsWith(".jar")) {
                            Logger.log("Loading file " + file);
                            loadFile(file);
                        }
                    } else if(kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                        Logger.log("File " + file + " has been been modified");
                        Logger.log("Current realisation don't support file reload!");
                    } else if(kind == StandardWatchEventKinds.ENTRY_DELETE) {
                        Logger.log("File " + file + " has been deleted");
                        Logger.log("Current realisation don't support file reload!");
                    } else if(kind == StandardWatchEventKinds.OVERFLOW) {
                        Logger.log("Overflow on file " + file + " !");
                    }
                }
                isFree.set(true);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void scanFolder(File folder) {
        File[] files = folder.listFiles();
        if(files != null) {
            for(File file : files) {
                if(file.isDirectory()) {
                    scanFolder(file);
                } else if(file.getName().endsWith(".jar")) {
                    loadFile(file);
                }
            }
        }
    }

    private void loadFile(File jar) {
        executor.submit(() -> {
            try {
                ClassLoader classLoader = new URLClassLoader(new URL[]{jar.toURI().toURL()}, this.getClass().getClassLoader());
                classLoaders.put(jar, classLoader);

                Agent.tryScanClassLoader(classLoader);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        });
    }
}
