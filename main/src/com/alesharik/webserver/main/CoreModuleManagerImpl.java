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

package com.alesharik.webserver.main;

import com.alesharik.webserver.api.agent.Agent;
import com.alesharik.webserver.configuration.util.CoreModule;
import com.alesharik.webserver.configuration.util.CoreModuleClassLoader;
import com.alesharik.webserver.configuration.util.CoreModuleManager;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefixes;
import com.alesharik.webserver.logger.level.Level;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Prefixes({"[CoreModule]", "[Manager]"})
@Level("core-module-manager")
final class CoreModuleManagerImpl implements CoreModuleManager {
    static {
        Logger.getLoggingLevelManager().createLoggingLevel("core-module-manager");
    }

    private final File folder;
    private final Map<String, CoreModule> modules = new HashMap<>();
    private final Map<CoreModule, CoreModuleClassLoader> classLoaders = new HashMap<>();
    private final List<CoreModule> modulesMirror = new ArrayList<>();

    public CoreModuleManagerImpl(File folder) {
        this.folder = folder;
        if(folder.exists() && !folder.isDirectory())
            throw new IllegalArgumentException(folder.getAbsolutePath() + " must be a directory!");
    }

    @SneakyThrows(MalformedURLException.class)
    public void load() {
        if(!folder.exists()) {
            System.err.println("Core modules folder " + folder + " doesn't exists!");
            return;
        }

        for(File file : folder.listFiles()) {
            if(file.isDirectory()) {
                System.err.println("Ignoring " + file + " directory!");
                continue;
            }
            if(!file.canRead()) {
                System.err.println("Ignoring " + file + " core module: not readable!");
                continue;
            }

            System.out.println("Loading core module " + file);

            CoreModule coreModule = new CoreModuleImpl(file);
            modules.put(coreModule.getName(), coreModule);
            modulesMirror.add(coreModule);

            ClassLoaderImpl classLoader = new ClassLoaderImpl(file, Main.class.getClassLoader(), coreModule);
            classLoaders.put(coreModule, classLoader);

            System.out.println("Sending classloader to agent, module: " + coreModule.getName());
            Agent.tryScanClassLoader(classLoader);
            System.out.println("Core module " + coreModule.getName() + " is loaded");
        }
        System.out.println("Core modules loaded");
    }

    @Nullable
    public Class<?> getClass(String name) {
        if(classLoaders.isEmpty()) {
            try {
                return this.getClass().getClassLoader().loadClass(name);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }

        for(CoreModuleClassLoader coreModuleClassLoader : classLoaders.values()) {
            try {
                return coreModuleClassLoader.loadClass(name);
            } catch (ClassNotFoundException ignored) {
            }
        }
        return null;
    }

    @Nonnull
    @Override
    public List<CoreModule> getModules() {
        return modulesMirror;
    }

    @Nullable
    @Override
    public CoreModule getModuleByName(@Nonnull String name) {
        return modules.get(name);
    }

    @Nonnull
    @Override
    public CoreModuleClassLoader getClassLoader(@Nonnull CoreModule module) {
        return classLoaders.get(module);
    }

    private static final class ClassLoaderImpl extends CoreModuleClassLoader {
        public ClassLoaderImpl(File jar, ClassLoader parent, @Nonnull CoreModule module) throws MalformedURLException {
            super(new URL[]{jar.getAbsoluteFile().toURI().toURL()}, parent, module);
        }
    }

    @RequiredArgsConstructor
    @Getter
    @EqualsAndHashCode
    @ToString
    private static final class CoreModuleImpl implements CoreModule {
        private final File file;

        @Nonnull
        @Override
        public String getName() {
            return file.getName();
        }
    }
}
