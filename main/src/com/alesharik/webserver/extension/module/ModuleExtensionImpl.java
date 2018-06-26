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

import com.alesharik.webserver.configuration.config.lang.ApiEndpointSection;
import com.alesharik.webserver.configuration.config.lang.ConfigurationEndpoint;
import com.alesharik.webserver.configuration.config.lang.CustomEndpointSection;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationElement;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationObject;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationPrimitive;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationTypedObject;
import com.alesharik.webserver.configuration.extension.DirectoryWatcher;
import com.alesharik.webserver.configuration.extension.Extension;
import com.alesharik.webserver.extension.module.extension.ModuleExtension;
import com.alesharik.webserver.extension.module.meta.ModuleAdapter;
import com.alesharik.webserver.extension.module.meta.ScriptElementConverter;
import com.alesharik.webserver.extension.module.util.Module;
import com.alesharik.webserver.extension.module.util.ModuleManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.ArrayList;
import java.util.List;

@Extension.Name(ModuleExtension.NAME)
public final class ModuleExtensionImpl extends ModuleExtension {
    private final List<DirectoryWatcher> watchers = new ArrayList<>();
    private final List<String> startedModules = new ArrayList<>();
    private ModuleManagerImpl moduleManager;
    private SharedLibraryManagerImpl sharedLibraryManager;
    @Getter
    private CommandExecutorImpl commandExecutor;
    @Getter
    private MessageManagerImpl messageManager;

    @Override
    public void load(@Nonnull ConfigurationEndpoint endpoint, ScriptElementConverter scriptEngine) {
        watchers.clear();

        ApiEndpointSection api = endpoint.getApiSection();
        File sharedLibsDirectory;
        File moduleDirectory;
        boolean reloadModules;
        ConfigurationObject moduleManagerConfig = (ConfigurationObject) api.getElement("module-manager");
        if(moduleManagerConfig != null) {
            if(moduleManagerConfig.hasKey("shared-libraries-directory")) {
                ConfigurationElement element = moduleManagerConfig.getElement("shared-libraries-directory");
                sharedLibsDirectory = new File(scriptEngine.isExecutable(element)
                        ? scriptEngine.execute(element, String.class)
                        : ((ConfigurationPrimitive.String) element).value());
            } else
                sharedLibsDirectory = new File("libs/");

            if(moduleManagerConfig.hasKey("module-directory")) {
                ConfigurationElement element = moduleManagerConfig.getElement("module-directory");
                moduleDirectory = new File(scriptEngine.isExecutable(element)
                        ? scriptEngine.execute(element, String.class)
                        : ((ConfigurationPrimitive.String) element).value());
            } else
                moduleDirectory = new File("modules/");

            if(moduleManagerConfig.hasKey("auto-reload")) {
                ConfigurationElement element = moduleManagerConfig.getElement("auto-reload");
                reloadModules = scriptEngine.isExecutable(element)
                        ? scriptEngine.execute(element, Boolean.class)
                        : ((ConfigurationPrimitive.Boolean) element).value();
            } else
                reloadModules = false;
        } else {
            sharedLibsDirectory = new File("libs/");
            moduleDirectory = new File("modules/");
            reloadModules = false;
        }
        sharedLibsDirectory = sharedLibsDirectory.getAbsoluteFile();
        moduleDirectory = moduleDirectory.getAbsoluteFile();

        sharedLibraryManager = new SharedLibraryManagerImpl(sharedLibsDirectory);
        moduleManager = new ModuleManagerImpl(sharedLibraryManager, scriptEngine, moduleDirectory);
        sharedLibraryManager.load(file -> {
            if(reloadModules)
                watchers.add(new SharedLibrariesWatcher(file.getAbsoluteFile().toPath()));
        });
        moduleManager.load(file -> {
            if(reloadModules)
                watchers.add(new ModuleWatcher(file.getAbsoluteFile().toPath()));
        }, endpoint);

        CustomEndpointSection modulesSection = endpoint.getCustomSection("modules");
        if(modulesSection == null)
            throw new ConfigurationError("Modules section not found!");
        for(Module module : moduleManager.getModules()) {
            ModuleAdapter adapter = ((ModuleManagerImpl.ModuleImpl) module).getModuleAdapter();
            ConfigurationTypedObject object = null;
            for(CustomEndpointSection.UseDirective useDirective : modulesSection.getUseDirectives()) {
                if(useDirective.getName().equals(module.getName()))
                    object = useDirective.getConfiguration();
            }
            if(object == null)
                throw new ConfigurationError("Config for module " + module.getName() + " not found!");
            adapter.configure(object, scriptEngine);
        }

        moduleManager.addListener(new ModuleListener());

        messageManager = new MessageManagerImpl(moduleManager, sharedLibraryManager, startedModules, scriptEngine);
        commandExecutor = new CommandExecutorImpl(moduleManager, messageManager, startedModules, scriptEngine);
        messageManager.setConfigurationEndpoint(endpoint);
        commandExecutor.setConfigurationEndpoint(endpoint);
    }

    @Override
    public void start() {

    }

    @Override
    public void reloadConfig(@Nonnull ConfigurationEndpoint last, @Nonnull ConfigurationEndpoint current, ScriptElementConverter converter) {
        moduleManager.updateConfiguration(current);
        messageManager.setConfigurationEndpoint(current);
        commandExecutor.setConfigurationEndpoint(current);
    }

    @Override
    public void shutdown() {
        for(Module module : moduleManager.getModules()) {
            ModuleManagerImpl.ModuleImpl mod = (ModuleManagerImpl.ModuleImpl) module;
            messageManager.modulePreShutdown(module, mod.getModuleAdapter(), mod.getInstance());
            mod.getModuleAdapter().shutdown();
            messageManager.moduleShutdown(module, mod.getModuleAdapter(), mod.getInstance());
        }
    }

    @Override
    public void shutdownNow() {
        for(Module module : moduleManager.getModules()) {
            ModuleManagerImpl.ModuleImpl mod = (ModuleManagerImpl.ModuleImpl) module;
            messageManager.modulePreShutdownNow(module, mod.getModuleAdapter(), mod.getInstance());
            mod.getModuleAdapter().shutdownNow();
            messageManager.moduleShutdownNow(module, mod.getModuleAdapter(), mod.getInstance());
        }
    }

    @Nonnull
    @Override
    public List<DirectoryWatcher> getFileWatchers() {
        return watchers;
    }

    @RequiredArgsConstructor
    private final class SharedLibrariesWatcher implements DirectoryWatcher {
        private final Path path;

        @Nonnull
        @Override
        public Path toWatch() {
            return path;
        }

        @Override
        public void fileChanged(@Nonnull Path file, @Nonnull WatchEvent.Kind<Path> kind) {
            if(kind == StandardWatchEventKinds.ENTRY_CREATE)
                sharedLibraryManager.add(file.toFile());
            else if(kind == StandardWatchEventKinds.ENTRY_MODIFY)
                sharedLibraryManager.update(file.toFile());
            else if(kind == StandardWatchEventKinds.ENTRY_DELETE)
                sharedLibraryManager.remove(file.toFile());
        }
    }

    @RequiredArgsConstructor
    private final class ModuleWatcher implements DirectoryWatcher {
        private final Path path;

        @Nonnull
        @Override
        public Path toWatch() {
            return path;
        }

        @Override
        public void fileChanged(@Nonnull Path file, @Nonnull WatchEvent.Kind<Path> kind) {
            if(kind == StandardWatchEventKinds.ENTRY_CREATE)
                moduleManager.add(file.toFile());
            else if(kind == StandardWatchEventKinds.ENTRY_MODIFY)
                moduleManager.update(file.toFile());
            else if(kind == StandardWatchEventKinds.ENTRY_DELETE)
                moduleManager.remove(file.toFile());
        }
    }

    private final class ModuleListener implements ModuleManager.UpdateListener {
        @Override
        public void onModuleAdd(@Nonnull Module module) {
            if(startedModules.contains(module.getName())) {
                ModuleManagerImpl.ModuleImpl module1 = (ModuleManagerImpl.ModuleImpl) module;
                messageManager.modulePreStarted(module, module1.getModuleAdapter(), module1.getInstance());
                module1.getModuleAdapter().start();
                messageManager.moduleStarted(module, module1.getModuleAdapter(), module1.getInstance());
            }
        }

        @Override
        public void onModuleUpdate(@Nonnull Module module) {
            if(!module.isRunning()) {
                if(startedModules.contains(module.getName())) {
                    ModuleManagerImpl.ModuleImpl module1 = (ModuleManagerImpl.ModuleImpl) module;
                    messageManager.modulePreStarted(module, module1.getModuleAdapter(), module1.getInstance());
                    module1.getModuleAdapter().start();
                    messageManager.moduleStarted(module, module1.getModuleAdapter(), module1.getInstance());
                }
            } else {
                if(!startedModules.contains(module.getName())) {
                    ModuleManagerImpl.ModuleImpl module1 = (ModuleManagerImpl.ModuleImpl) module;
                    messageManager.modulePreShutdown(module, module1.getModuleAdapter(), module1.getInstance());
                    module1.getModuleAdapter().shutdown();
                    messageManager.moduleShutdown(module, module1.getModuleAdapter(), module1.getInstance());
                }
            }
        }

        @Override
        public void onModuleReload(@Nonnull Module module) {
            ModuleManagerImpl.ModuleImpl module1 = (ModuleManagerImpl.ModuleImpl) module;
            messageManager.moduleReload(module, module1.getModuleAdapter(), module1.getInstance());
        }

        @Override
        public void onModuleDelete(@Nonnull Module module) {
            ModuleManagerImpl.ModuleImpl module1 = (ModuleManagerImpl.ModuleImpl) module;
            messageManager.modulePreShutdown(module, module1.getModuleAdapter(), module1.getInstance());
            messageManager.moduleShutdown(module, module1.getModuleAdapter(), module1.getInstance());
        }
    }
}
