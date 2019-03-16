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

package com.alesharik.webserver.extension.module;

import com.alesharik.webserver.api.agent.Agent;
import com.alesharik.webserver.api.agent.Rescanable;
import com.alesharik.webserver.extension.module.util.SharedLibrary;
import com.alesharik.webserver.extension.module.util.SharedLibraryClassLoader;
import com.alesharik.webserver.extension.module.util.SharedLibraryManager;
import com.alesharik.webserver.extension.module.util.SharedLibraryVersion;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefixes;
import com.alesharik.webserver.logger.level.Level;
import com.alesharik.webserver.main.Main;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.collections.IteratorUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@Level("shared-library-manager")
@Prefixes("[SharedLibraryManager]")
public final class SharedLibraryManagerImpl implements SharedLibraryManager {
    static {
        Logger.getLoggingLevelManager().createLoggingLevel("shared-library-manager");
    }

    private final Map<String, SharedLibrary> sharedLibraries = new HashMap<>();
    private final List<SharedLibrary> sharedLibrariesMirror = new ArrayList<>();
    private final Map<SharedLibrary, ClassLoaderImpl> classLoaders = new HashMap<>();
    private final File folder;
    private final List<UpdateListener> listeners = new CopyOnWriteArrayList<>();

    public SharedLibraryManagerImpl(File folder) {
        this.folder = folder;
        if(folder.exists() && !folder.isDirectory())
            throw new IllegalArgumentException(folder.getAbsolutePath() + " must be a directory!");
    }

    public void load(Consumer<File> hookReload) {
        if(!folder.exists()) {
            System.err.println("Shared library folder " + folder + " doesn't exists!");
            return;
        }

        loadFolder(folder);

        System.out.println("Shared libraries loaded");

        System.out.println("Hooking reloading handler...");
        hookReload.accept(folder);
        System.out.println("Reloading handler hooked");
    }

    @SneakyThrows(MalformedURLException.class)
    private void loadFolder(File folder) {
        for(File file : folder.listFiles()) {
            if(file.isDirectory()) {
                loadFolder(file);
                continue;
            }

            if(!file.canRead()) {
                System.err.println("Ignoring " + file + " shared library: not readable!");
                continue;
            }

            System.out.println("Loading shared library " + file);

            SharedLibraryImpl sharedLibrary = new SharedLibraryImpl(file);
            sharedLibraries.put(sharedLibrary.getName(), sharedLibrary);
            sharedLibrariesMirror.add(sharedLibrary);

            ClassLoaderImpl classLoader = new ClassLoaderImpl(file, sharedLibrary);
            classLoaders.put(sharedLibrary, classLoader);

            System.out.println("Sending classloader to agent, library: " + sharedLibrary.getName());
            Agent.tryScanClassLoader(classLoader);
            System.out.println("Shared library " + sharedLibrary.getName() + " is loaded");
        }
    }

    @SneakyThrows(MalformedURLException.class)
    public void add(File lib) {
        if(!lib.canRead()) {
            System.err.println("Ignoring " + lib + " shared library: not readable!");
            return;
        }

        System.out.println("Loading shared library " + lib);

        SharedLibraryImpl sharedLibrary = new SharedLibraryImpl(lib);
        sharedLibraries.put(sharedLibrary.getName(), sharedLibrary);
        sharedLibrariesMirror.add(sharedLibrary);

        ClassLoaderImpl classLoader = new ClassLoaderImpl(lib, sharedLibrary);
        classLoaders.put(sharedLibrary, classLoader);

        System.out.println("Sending classloader to agent, library: " + sharedLibrary.getName());
        Agent.tryScanClassLoader(classLoader);

        System.out.println("Triggering update listeners...");
        for(UpdateListener listener : listeners)
            listener.onLibraryAdd(sharedLibrary);

        System.out.println("Shared library " + sharedLibrary.getName() + " loaded");
    }

    public void remove(File lib) {
        SharedLibrary library = null;
        for(SharedLibrary sharedLibrary : sharedLibrariesMirror) {
            if(sharedLibrary.getFile().equals(lib))
                library = sharedLibrary;
        }

        if(library == null) {
            System.out.println("Library " + lib + " not found => can't remove");
            return;
        }

        System.out.println("Removing shared library " + library.getName());
        sharedLibraries.remove(library.getName());
        sharedLibrariesMirror.remove(library);

        SharedLibraryClassLoader classLoader = classLoaders.get(library);
        System.out.println("Removing classloader...");
        Agent.unloadClassLoader(classLoader);
        classLoaders.remove(library);

        System.out.println("Triggering update listeners...");
        for(UpdateListener listener : listeners)
            listener.onLibraryDelete(library);

        System.out.println("Shared library " + library.getName() + " removed and unloaded");
    }

    public void update(File lib) {
        SharedLibrary library = null;
        for(SharedLibrary sharedLibrary : sharedLibrariesMirror) {
            if(sharedLibrary.getFile().equals(lib))
                library = sharedLibrary;
        }

        if(library == null) {
            System.out.println("Library " + lib + " not found => can't update");
            return;
        }

        System.out.println("Updating shared library " + library.getName());

        SharedLibraryClassLoader classLoader = classLoaders.get(library);
        System.out.println("Rescanning classloader...");
        Agent.tryScanClassLoader(classLoader);

        System.out.println("Triggering update listeners...");
        for(UpdateListener listener : listeners)
            listener.onLibraryUpdate(library);

        System.out.println("Shared library " + library.getName() + " updated");
    }

    @Nonnull
    @Override
    public List<SharedLibrary> getSharedLibs() {
        return sharedLibrariesMirror;
    }

    @Nullable
    @Override
    public SharedLibrary getLibrary(@Nonnull String name) {
        return sharedLibraries.get(name);
    }

    @Override
    public boolean hasLibrary(@Nonnull String name, @Nullable SharedLibraryVersion minimumVersion) {
        return sharedLibraries.containsKey(name) && (minimumVersion == null || sharedLibraries.get(name).getVersion().compareTo(minimumVersion) >= 0);
    }

    @Nonnull
    @Override
    public SharedLibraryClassLoader getClassLoader(@Nonnull SharedLibrary sharedLibrary) {
        if(!classLoaders.containsKey(sharedLibrary))
            throw new IllegalArgumentException("SharedLibrary not found!");
        return classLoaders.get(sharedLibrary);
    }

    @Override
    public void addListener(@Nonnull UpdateListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(@Nonnull UpdateListener listener) {
        listeners.remove(listener);
    }

    @Getter
    @EqualsAndHashCode(exclude = {"version", "file"})
    private static final class SharedLibraryImpl implements SharedLibrary {
        private final String name;
        private final SharedLibraryVersion version;
        private final File file;

        public SharedLibraryImpl(File file) {
            this.file = file;
            String name = file.getName();
            int splitI = name.lastIndexOf('-');
            if(splitI == -1)
                throw new IllegalArgumentException("Shared library " + file.getName() + " has no version!");
            this.name = name.substring(0, splitI);
            this.version = new SharedLibraryVersion(name.substring(splitI + 1));
        }
    }

    @Rescanable
    final class ClassLoaderImpl extends SharedLibraryClassLoader {
        public ClassLoaderImpl(File url, @Nonnull SharedLibrary sharedLibrary) throws MalformedURLException {
            super(url.getAbsoluteFile().toURI().toURL(), Main.class.getClassLoader(), sharedLibrary);
        }

        @Override
        public Class<?> findClass(String name) throws ClassNotFoundException {
            try {
                return super.findClass(name);//Scan current classloader and parent classloader
            } catch (ClassNotFoundException e) {
                for(ClassLoaderImpl classLoader : classLoaders.values()) {
                    try {
                        return classLoader.findClass(name);
                    } catch (ClassNotFoundException ignored) {
                    }
                }
                throw e;
            }
        }

        @Override
        public URL findResource(String name) {
            URL resource = super.findResource(name);
            if(resource == null) {
                for(ClassLoaderImpl classLoader : classLoaders.values()) {
                    resource = classLoader.findResource(name);
                    if(resource != null)
                        return resource;
                }
            }
            return resource;
        }

        @Override
        public Enumeration<URL> findResources(String name) throws IOException {
            Set<URL> list = new HashSet<>();

            Enumeration<URL> resources = super.findResources(name);
            while(resources.hasMoreElements())
                list.add(resources.nextElement());

            for(ClassLoaderImpl classLoader : classLoaders.values()) {
                Enumeration<URL> res = classLoader.findResources(name);
                while(res.hasMoreElements())
                    list.add(res.nextElement());
            }

            //noinspection unchecked
            return IteratorUtils.asEnumeration(list.iterator());
        }
    }
}
