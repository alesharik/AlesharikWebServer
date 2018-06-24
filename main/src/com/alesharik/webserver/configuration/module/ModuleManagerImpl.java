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

package com.alesharik.webserver.configuration.module;

import com.alesharik.webserver.api.ExecutionStage;
import com.alesharik.webserver.api.agent.Agent;
import com.alesharik.webserver.api.agent.Rescanable;
import com.alesharik.webserver.api.agent.Stages;
import com.alesharik.webserver.api.agent.bean.Contexts;
import com.alesharik.webserver.api.agent.classPath.ClassPathScanner;
import com.alesharik.webserver.api.agent.classPath.ListenAnnotation;
import com.alesharik.webserver.api.agent.classPath.SuppressClassLoaderUnloadWarning;
import com.alesharik.webserver.base.bean.Bean;
import com.alesharik.webserver.base.bean.context.BeanContext;
import com.alesharik.webserver.configuration.config.lang.ConfigurationEndpoint;
import com.alesharik.webserver.configuration.config.lang.CustomEndpointSection;
import com.alesharik.webserver.configuration.module.meta.CustomData;
import com.alesharik.webserver.configuration.module.meta.ModuleAdapter;
import com.alesharik.webserver.configuration.module.meta.ModuleMetaFactory;
import com.alesharik.webserver.configuration.module.meta.ScriptElementConverter;
import com.alesharik.webserver.configuration.utils.Module;
import com.alesharik.webserver.configuration.utils.ModuleClassLoader;
import com.alesharik.webserver.configuration.utils.ModuleManager;
import com.alesharik.webserver.configuration.utils.SharedLibrary;
import com.alesharik.webserver.configuration.utils.SharedLibraryManager;
import com.alesharik.webserver.exception.error.UnexpectedBehaviorError;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefixes;
import com.alesharik.webserver.logger.level.Level;
import com.alesharik.webserver.main.Main;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

@RequiredArgsConstructor
@SuppressClassLoaderUnloadWarning
@Level("module-manager")
@Prefixes("[ModuleManager]")
@ClassPathScanner
public final class ModuleManagerImpl implements ModuleManager {
    private static final List<Class<?>> baseModules = new CopyOnWriteArrayList<>();
    @SuppressFBWarnings("DMI_HARDCODED_ABSOLUTE_FILENAME")
    private static final File BASE = new File("/base");
    private static final boolean runtimeDepsAreOptional;

    static {
        Logger.getLoggingLevelManager().createLoggingLevel("module-manager");
        boolean rdepsOptional = false;
        if(System.getProperty("treat-runtime-dependencies-as-optional") != null)
            rdepsOptional = Boolean.parseBoolean(System.getProperty("treat-runtime-dependencies-as-optional"));
        runtimeDepsAreOptional = rdepsOptional;
    }

    private final SharedLibraryManager sharedLibraryManager;
    private final ScriptElementConverter scriptElementConverter;
    private final File directory;
    private final List<UpdateListener> listeners = new CopyOnWriteArrayList<>();

    private final Map<String, ModuleImpl> modules = new HashMap<>();
    private final List<Module> modulesMirror = new ArrayList<>();
    private final Map<ModuleImpl, ClassLoaderImpl> classLoaders = new HashMap<>();

    private final MultiValuedMap<File, ModuleImpl> fileMap = new ArrayListValuedHashMap<>();

    private final MultiValuedMap<SharedLibrary, Pair<File, ModuleImpl>> unmetDeps = new ArrayListValuedHashMap<>();
    private final Map<String, Pair<File, String>> types = new HashMap<>();

    private ConfigurationEndpoint endpoint;

    @ListenAnnotation(com.alesharik.webserver.configuration.module.Module.class)
    @Stages({ExecutionStage.AGENT, ExecutionStage.CORE_MODULES, ExecutionStage.PRE_LOAD})
    static void listen(Class<?> clazz) {
        baseModules.add(clazz);
    }

    public void load(Consumer<File> notify, ConfigurationEndpoint endpoint) {
        this.endpoint = endpoint;
        System.out.println("Loading base modules...");
        for(Class<?> baseModule : baseModules)
            registerModuleClass(BASE, baseModule, false);
        System.out.println("Loading modules...");
        long time = System.nanoTime();
        loadDir(directory);
        System.out.println("[Perf] Modules loading time: " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - time) + "ms");
        System.out.println("Notifying...");
        notify.accept(directory);

        System.out.println("Setting libs listener...");
        sharedLibraryManager.addListener(new ListenerImpl());

        System.out.println("Modules loaded");
    }

    private void loadDir(File dir) {
        if(!dir.exists()) {
            System.err.println("Can't load modules: directory " + dir + " not found!");
            return;
        }

        for(File file : dir.listFiles()) {
            if(!file.canRead()) {
                System.err.println("Ignoring " + file + ": can't read");
                continue;
            }
            if(file.isDirectory())
                loadDir(file);
            else
                loadModule(file, false);
        }
    }

    public void add(File file) {
        if(!file.canRead()) {
            System.err.println("Can't read file " + file + " . Ignoring!");
            return;
        }

        System.out.println("Loading module " + file);
        loadModule(file, true);
    }

    public void remove(File file) {
        System.out.println("Removing module " + file);
        if(!fileMap.containsKey(file)) {
            System.err.println("Module " + file + " not found");
            return;
        }

        for(ModuleImpl module : fileMap.get(file)) {
            unloadModule(module);
            types.remove(module.getType());
        }
        fileMap.remove(file);
    }

    private void unloadModule(ModuleImpl module) {
        if(module.isRunning()) {
            System.out.println("Module " + module.getName() + " is running! Shutting down...");
            module.moduleAdapter.shutdown();
        }
        System.out.println("Unloading classloader...");
        ClassLoaderImpl classLoader = classLoaders.get(module);
        Agent.unloadClassLoader(classLoader);

        System.out.println("Unregistering...");
        modules.remove(module.getName());
        modulesMirror.remove(module);
        classLoaders.remove(module);

        System.out.println("Triggering listeners...");
        for(UpdateListener listener : listeners)
            listener.onModuleDelete(module);

        System.out.println("Destroying context...");
        Contexts.destroyContext(module.context);

        System.out.println("Module " + module.getName() + " removed");
    }

    @SneakyThrows(MalformedURLException.class)
    public void update(File file) {
        if(!file.canRead()) {
            System.err.println("Can't read file " + file + " . Ignoring!");
            return;
        }

        System.out.println("Updating module " + file);
        long time = System.nanoTime();

        List<Class<?>> classes = new ArrayList<>();
        URLClassLoader scanClassLoader = new URLClassLoader(new URL[]{file.getAbsoluteFile().toURI().toURL()}, Main.class.getClassLoader());
        new FastClasspathScanner()
                .overrideClassLoaders(scanClassLoader)
                .ignoreParentClassLoaders()
                .matchClassesWithAnnotation(com.alesharik.webserver.configuration.module.Module.class, classes::add)
                .scan();
        System.out.println("[Perf] ClassPath scanning time: " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - time));

        List<ModuleImpl> modules = new ArrayList<>(fileMap.get(file));
        for(Class<?> aClass : classes) {
            com.alesharik.webserver.configuration.module.Module meta = aClass.getAnnotation(com.alesharik.webserver.configuration.module.Module.class);
            boolean found = false;
            for(ModuleImpl module : fileMap.get(file)) {
                if(module.getType().equals(meta.value())) {
                    updateModule(file, aClass, module);
                    found = true;
                    modules.remove(module);
                }
            }
            if(!found)
                registerModuleClass(file, aClass, true);
        }
        for(ModuleImpl module : modules)
            unloadModule(module);

        System.out.println("Update completed");
        System.out.println("[Perf] Update time: " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - time));
    }

    @SneakyThrows(MalformedURLException.class)
    private void updateModule(File file, Class<?> aClass, ModuleImpl module) {
        System.out.println("Updating module " + module.getName());

        ClassLoaderImpl classLoader = new ClassLoaderImpl(file, Main.class.getClassLoader(), module, sharedLibraryManager, module.dependencies, module.runtimeDependencies);

        Class<?> clazz;
        try {
            clazz = classLoader.loadClass(aClass.getName());
        } catch (ClassNotFoundException e) {
            throw new UnexpectedBehaviorError(e);
        }
        Object instance = module.context.createObject(clazz, new Bean.Builder()
                .autowire()
                .singleton(false)
                .canBeGarbage(true)
                .build());
        ModuleAdapter adapter = ModuleMetaFactory.create(instance, this, module.context);

        if(module.isRunning()) {
            System.out.println("Shutting down module " + module);
            module.moduleAdapter.shutdown();
        }

        module.init(module.context, instance, adapter);
        module.runtimeDependencies.clear();

        classLoaders.put(module, classLoader);

        System.out.println("Notifying listeners...");
        for(UpdateListener listener : listeners)
            listener.onModuleUpdate(module);

        System.out.println("Module " + module.getName() + " updated");
    }

    @SneakyThrows(MalformedURLException.class)
    private void loadModule(File file, boolean notify) {
        System.out.println("Loading module " + file);

        List<Class<?>> classes = new ArrayList<>();
        long time = System.nanoTime();

        URLClassLoader scanClassLoader = new URLClassLoader(new URL[]{file.getAbsoluteFile().toURI().toURL()}, Main.class.getClassLoader());
        new FastClasspathScanner()
                .overrideClassLoaders(scanClassLoader)
                .ignoreParentClassLoaders()
                .matchClassesWithAnnotation(com.alesharik.webserver.configuration.module.Module.class, classes::add)
                .scan();
        System.out.println("[Perf] ClassPath scanning time: " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - time) + "ms");

        for(Class<?> aClass : classes)
            registerModuleClass(file, aClass, notify);
        System.out.println("[Perf] Module load time: " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - time) + "ms");
    }

    @SneakyThrows(MalformedURLException.class)
    private void registerModuleClass(File file, Class<?> aClass, boolean notify) {
        com.alesharik.webserver.configuration.module.Module meta = aClass.getAnnotation(com.alesharik.webserver.configuration.module.Module.class);
        String type = meta.value();
        types.put(type, Pair.of(file, aClass.getName()));

        CustomEndpointSection modules = endpoint.getCustomSection("modules");
        if(modules != null) {
            for(CustomEndpointSection.UseDirective useDirective : modules.getUseDirectives())
                if(useDirective.getConfiguration().getType().equals(type))
                    registerModule(file, aClass, notify, useDirective.getName(), type);
        } else
            System.out.println("Modules section not found");
    }

    private void registerModule(File file, Class<?> aClass, boolean notify, String name, String type) throws MalformedURLException {
        if(modules.containsKey(name)) {
            System.out.println("Ignoring module " + name + ": already exists");
            return;
        }

        ModuleImpl module = new ModuleImpl(aClass, sharedLibraryManager, name, type);
        fileMap.put(file, module);

        ClassLoaderImpl classLoader = new ClassLoaderImpl(file, Main.class.getClassLoader(), module, sharedLibraryManager, module.dependencies, module.runtimeDependencies);

        Class<?> clazz;
        try {
            clazz = classLoader.loadClass(aClass.getName());
        } catch (ClassNotFoundException e) {
            throw new UnexpectedBehaviorError(e);
        }
        ModuleBeanContext context = (ModuleBeanContext) Contexts.createContext(ModuleBeanContext.class);
        context.linkSingleton(SharedLibraryManager.class, sharedLibraryManager);
        context.linkSingleton(ModuleManager.class, this);
        Object instance = context.createObject(clazz, new Bean.Builder()
                .autowire()
                .singleton(false)
                .canBeGarbage(true)
                .build());
        ModuleAdapter adapter = ModuleMetaFactory.create(instance, this, context);
        module.init(context, instance, adapter);

        classLoaders.put(module, classLoader);
        modules.put(name, module);
        modulesMirror.add(module);

        System.out.println("Module " + name + " registered");

        if(notify) {
            System.out.println("Notifying listeners...");
            for(UpdateListener listener : listeners)
                listener.onModuleAdd(module);
        }
    }

    @Override
    public List<Module> getModules() {
        return modulesMirror;
    }

    @Override
    public Module getModule(String name) {
        return modules.get(name);
    }

    @Override
    public ModuleClassLoader getClassLoader(Module module) {
        //noinspection SuspiciousMethodCalls
        ClassLoaderImpl classLoader = classLoaders.get(module);
        if(classLoader == null)
            throw new IllegalArgumentException("Module " + module.getName() + " not found!");
        return classLoader;
    }

    @Nonnull
    @Override
    public MultiValuedMap<SharedLibrary, Module> getModulesWithUnmetDependencies() {
        MultiValuedMap<SharedLibrary, Module> map = new ArrayListValuedHashMap<>();
        for(Map.Entry<SharedLibrary, Pair<File, ModuleImpl>> sharedLibraryPairEntry : unmetDeps.entries())
            map.put(sharedLibraryPairEntry.getKey(), sharedLibraryPairEntry.getValue().getValue());
        return map;
    }

    @Override
    public void addListener(UpdateListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void removeListener(UpdateListener listener) {
        listeners.add(listener);
    }

    @Nullable
    @Override
    public Object provideModule(@Nonnull String name, @Nonnull Class<?> clazz) {
        ModuleImpl module = modules.get(name);
        if(module == null)
            return null;
        return clazz.cast(module.instance);
    }

    @SneakyThrows(MalformedURLException.class)
    public void updateConfiguration(ConfigurationEndpoint endpoint) {
        CustomEndpointSection modules = endpoint.getCustomSection("modules");
        List<CustomEndpointSection.UseDirective> baseDirectives = new ArrayList<>();
        CustomEndpointSection base = this.endpoint.getCustomSection("modules");
        if(base != null) {
            baseDirectives.addAll(base.getUseDirectives());
        }
        if(modules != null) {
            main:
            for(CustomEndpointSection.UseDirective useDirective : modules.getUseDirectives()) {
                if(base != null) {
                    for(Iterator<CustomEndpointSection.UseDirective> iterator = baseDirectives.iterator(); iterator.hasNext(); ) {
                        CustomEndpointSection.UseDirective baseDirective = iterator.next();
                        if(baseDirective.getName().equals(useDirective.getName())) {
                            if(!baseDirective.getConfiguration().equals(useDirective.getConfiguration())) { //Config updated
                                ModuleImpl module = this.modules.get(useDirective.getName());
                                module.moduleAdapter.reload(useDirective.getConfiguration(), scriptElementConverter);

                                for(UpdateListener listener : listeners)
                                    listener.onModuleReload(module);
                            }
                            continue main;
                        }
                        iterator.remove();
                    }
                    //not found
                    findAndLoadModuleByType(useDirective);
                } else {
                    findAndLoadModuleByType(useDirective);
                }
            }

            for(CustomEndpointSection.UseDirective baseDirective : baseDirectives)
                unloadModule(this.modules.get(baseDirective.getName()));
        } else {
            for(ModuleImpl module : this.modules.values())
                unloadModule(module);
        }
    }

    private void findAndLoadModuleByType(CustomEndpointSection.UseDirective useDirective) throws MalformedURLException {
        Pair<File, String> mapping = types.get(useDirective.getConfiguration().getType());
        URLClassLoader temp = new URLClassLoader(new URL[]{mapping.getKey().toURI().toURL()});
        Class<?> clazz;
        try {
            clazz = temp.loadClass(mapping.getRight());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        registerModule(mapping.getKey(), clazz, true, useDirective.getName(), useDirective.getConfiguration().getType());
    }

    @Rescanable
    private static final class ClassLoaderImpl extends ModuleClassLoader {
        private final SharedLibraryManager sharedLibraryManager;
        private final List<SharedLibrary> deps;
        private final List<SharedLibrary> rdeps;

        protected ClassLoaderImpl(File u, ClassLoader parent, @Nonnull Module module, SharedLibraryManager sharedLibraryManager, List<SharedLibrary> deps, List<SharedLibrary> rdeps) throws MalformedURLException {
            super(u == BASE ? null : u.getAbsoluteFile().toURI().toURL(), parent, module);
            this.sharedLibraryManager = sharedLibraryManager;
            this.deps = deps;
            this.rdeps = rdeps;
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            try {
                return super.findClass(name);
            } catch (ClassNotFoundException e) {
                for(SharedLibrary dep : deps) {
                    try {
                        return ((SharedLibraryManagerImpl.ClassLoaderImpl) sharedLibraryManager.getClassLoader(dep)).findClass(name);
                    } catch (ClassNotFoundException ignored) {
                    }
                }
                for(SharedLibrary dep : rdeps) {
                    try {
                        return ((SharedLibraryManagerImpl.ClassLoaderImpl) sharedLibraryManager.getClassLoader(dep)).findClass(name);
                    } catch (ClassNotFoundException ignored) {
                    }
                }
                for(SharedLibrary library : sharedLibraryManager.getSharedLibs()) {
                    if(deps.contains(library) || rdeps.contains(library))
                        continue;
                    try {
                        Class<?> c = ((SharedLibraryManagerImpl.ClassLoaderImpl) sharedLibraryManager.getClassLoader(library)).findClass(name);
                        rdeps.add(library);
                        return c;
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
                for(SharedLibrary dep : deps) {
                    resource = sharedLibraryManager.getClassLoader(dep).findResource(name);
                    if(resource != null)
                        return resource;
                }
                for(SharedLibrary dep : rdeps) {
                    resource = sharedLibraryManager.getClassLoader(dep).findResource(name);
                    if(resource != null)
                        return resource;
                }
                for(SharedLibrary library : sharedLibraryManager.getSharedLibs()) {
                    if(deps.contains(library) || rdeps.contains(library))
                        continue;
                    resource = sharedLibraryManager.getClassLoader(library).findResource(name);
                    if(resource != null) {
                        rdeps.add(library);
                        return resource;
                    }
                }
            }
            return resource;
        }

        @Override
        public Enumeration<URL> findResources(String name) throws IOException {
            Set<URL> resources = new HashSet<>();
            Enumeration<URL> resource = super.findResources(name);
            while(resource.hasMoreElements())
                resources.add(resource.nextElement());

            for(SharedLibrary dep : deps) {
                Enumeration<URL> res = sharedLibraryManager.getClassLoader(dep).findResources(name);
                while(res.hasMoreElements())
                    resources.add(res.nextElement());
            }

            for(SharedLibrary dep : rdeps) {
                Enumeration<URL> res = sharedLibraryManager.getClassLoader(dep).findResources(name);
                while(res.hasMoreElements())
                    resources.add(res.nextElement());
            }
            if(resources.isEmpty()) {
                for(SharedLibrary library : sharedLibraryManager.getSharedLibs()) {
                    if(deps.contains(library) || rdeps.contains(library))
                        continue;
                    Enumeration<URL> res = sharedLibraryManager.getClassLoader(library).findResources(name);
                    while(res.hasMoreElements())
                        resources.add(res.nextElement());
                }
            }

            return IteratorUtils.asEnumeration(resources.iterator());
        }
    }

    @Getter
    public static final class ModuleImpl implements Module {
        private final List<SharedLibrary> dependencies = new ArrayList<>();
        private final List<SharedLibrary> runtimeDependencies = new ArrayList<>();
        private final List<SharedLibrary> optionalDeps = new ArrayList<>();
        private final Map<SharedLibrary, Predicate<SharedLibrary>> versions = new HashMap<>();
        private final String name;
        private final String type;
        private ModuleAdapter moduleAdapter;
        private Object instance;
        private BeanContext context;

        public ModuleImpl(Class<?> clazz, SharedLibraryManager sharedLibraryManager, String name, String type) {
            this.name = name;
            this.type = type;
            for(Dependency dependency : clazz.getAnnotationsByType(Dependency.class)) {
                SharedLibrary library = sharedLibraryManager.getLibrary(dependency.value());
                if(library == null) {
                    if(dependency.optional())
                        continue;
                    else
                        throw new DependencyError("Library " + dependency.value() + " is absent for module " + clazz.getCanonicalName());
                }
                if(dependency.version() != null) {
                    SharedLibrary.SharedLibraryPredicate predicate = new SharedLibrary.SharedLibraryPredicate(dependency.version());
                    if(!predicate.test(library)) {
                        if(dependency.optional())
                            continue;
                        else
                            throw new DependencyError("Library " + library.getName() + " doesn't met version " + dependency.version());
                    }
                    versions.put(library, predicate);
                }
                if(dependency.optional())
                    optionalDeps.add(library);
                dependencies.add(library);
            }
        }

        void init(BeanContext context, Object instance, ModuleAdapter adapter) {
            this.instance = instance;
            this.moduleAdapter = adapter;
            this.context = context;
        }

        @Nonnull
        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isRunning() {
            return moduleAdapter.isRunning();
        }

        @Nonnull
        @Override
        public CustomData getData() {
            return moduleAdapter.getData();
        }
    }

    @Prefixes("[ModuleManager]")
    @Level("module-manager")
    private final class ListenerImpl implements SharedLibraryManager.UpdateListener {
        @Override
        public void onLibraryDelete(@Nonnull SharedLibrary library) {
            for(ModuleImpl module : modules.values()) {
                if(module.runtimeDependencies.contains(library)) {
                    if(runtimeDepsAreOptional)
                        continue;

                    System.err.println("Unloading module due to unmet dependencies: " + library.getName() + " runtime dependency");
                    unloadModule(module);

                    main:
                    for(File file : fileMap.keys()) {
                        for(ModuleImpl module1 : fileMap.get(file)) {
                            if(module1 == module) {
                                fileMap.removeMapping(file, module);
                                unmetDeps.put(library, Pair.of(file, module));
                                break main;
                            }
                        }
                    }

                    continue;
                }
                if(module.dependencies.contains(library) && !module.optionalDeps.contains(library)) {
                    System.err.println("Unloading module due to unmet dependencies: " + library.getName() + " dependency");
                    unloadModule(module);

                    main:
                    for(File file : fileMap.keySet()) {
                        for(ModuleImpl module1 : fileMap.get(file)) {
                            if(module1 == module) {
                                fileMap.removeMapping(file, module);
                                unmetDeps.put(library, Pair.of(file, module));
                                break main;
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void onLibraryAdd(@Nonnull SharedLibrary library) {
            for(Iterator<Pair<File, ModuleImpl>> iterator = unmetDeps.get(library).iterator(); iterator.hasNext(); ) {
                Pair<File, ModuleImpl> fileModulePair = iterator.next();

                ModuleImpl module = fileModulePair.getRight();
                if(module.dependencies.contains(library)) {
                    Predicate<SharedLibrary> check = module.versions.get(library);
                    if(check != null && !check.test(library)) {
                        System.err.println("Library " + library.getName() + " failed version check on module " + module.getName() + " . Skipping module...");
                        continue;
                    }
                }

                System.out.println("Module's " + module.getName() + " dependency met: " + library);
                System.out.println("Loading module " + module.getName());
                registerModuleClass(fileModulePair.getKey(), module.instance.getClass(), true);
                iterator.remove();
            }
        }
    }
}
