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

import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefixes;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

@Prefixes("[Configuration]")
public final class ConfigurationImpl implements Configuration {
    private final AtomicBoolean isInitialized;
    /**
     * Name: module, started
     */
    private final Map<String, ModuleHolder> modules;

    public ConfigurationImpl() {
        isInitialized = new AtomicBoolean(false);
        modules = new ConcurrentHashMap<>();
    }

    @Override
    public void parseModules(Element modules) {
        this.modules.forEach((s, moduleHolder) -> moduleHolder.check());

        final boolean isInit = isInitialized.getAndSet(true);
        NodeList moduleList = modules.getElementsByTagName("module");
        for(int i = 0; i < moduleList.getLength(); i++) {
            Element element = (Element) moduleList.item(i);
            String type;
            String name;
            Node typeNode = element.getElementsByTagName("type").item(0);
            Node nameNode = element.getElementsByTagName("name").item(0);
            Element configNode = (Element) element.getElementsByTagName("configuration").item(0);
            if(typeNode != null && nameNode != null) {
                type = typeNode.getTextContent();
                name = nameNode.getTextContent();
            } else {
                Logger.log("Node №" + i + " has invalid configuration! Skipping...");
                continue;
            }

            Module module = ModuleManager.getModuleByName(type);
            if(module == null) {
                Logger.log("Module with type " + type + " not found! Skipping...");
            } else {
                if(!isInit || !this.modules.containsKey(type)) {
                    ModuleHolder moduleHolder = new ModuleHolder(module, name);
                    this.modules.put(name, moduleHolder);
                    moduleHolder.getModule().parse(configNode);
                    Logger.log("Module " + name + " with type " + type + " successfully loaded!");
                } else {
                    ModuleHolder moduleHolder = this.modules.get(name);
                    moduleHolder.uncheck();
                    moduleHolder.getModule().reload(configNode);
                    Logger.log("Module " + name + " with type " + type + " successfully reloaded!");
                }
            }
        }

        this.modules.entrySet().stream()
                .filter(entry -> entry.getValue().isChecked())
                .forEach(entry -> {
                    entry.getValue().shutdown();
                    this.modules.remove(entry.getKey());
                    Logger.log("Module " + entry.getValue().getName() + " with type " + entry.getValue().getType() + " deleted!");
                });
    }

    @Override
    public void parseMain(Element main) {
        modules.forEach((s, moduleHolder) -> moduleHolder.mainCheck());
        Element initNode = (Element) main.getElementsByTagName("init").item(0);
        if(initNode == null) {
            Logger.log("Init node not found! Server won't start any plugin!");
            return;
        }
        NodeList startModuleNodes = initNode.getElementsByTagName("startModule");
        for(int i = 0; i < startModuleNodes.getLength(); i++) {
            String name = startModuleNodes.item(i).getTextContent();
            if(!modules.containsKey(name)) {
                Logger.log("Server doesn't have " + name + " module! Skipping...");
                continue;
            }
            ModuleHolder moduleHolder = modules.get(name);
            if(moduleHolder.isRunning()) {
                moduleHolder.mainUncheck();
            } else {
                moduleHolder.start();
                moduleHolder.mainUncheck();
                Logger.log("Module " + moduleHolder.getName() + " with type " + moduleHolder.getType() + " successfully started!");
            }
        }

        modules.entrySet().stream()
                .filter(stringModuleHolderEntry -> stringModuleHolderEntry.getValue().isRunning())
                .filter(stringModuleHolderEntry -> stringModuleHolderEntry.getValue().mainIsChecked())
                .forEach(stringModuleHolderEntry -> {
                    stringModuleHolderEntry.getValue().shutdown();
                    Logger.log("Module " + stringModuleHolderEntry.getKey() + " with type " + stringModuleHolderEntry.getValue().getType() + " was shutdown!");
                });
    }

    @Override
    public Module getModuleByName(String name) {
        System.out.println("Trying to load " + name);
        return modules.get(name).getModule();
    }

    @Override
    public void shutdownNow() {
        modules.values().forEach(moduleHolder -> {
            try {
                moduleHolder.shutdownNow();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    @Override
    public void shutdown() {
        modules.values().forEach(moduleHolder -> {
            try {
                moduleHolder.shutdown();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    @ToString
    @EqualsAndHashCode
    static final class ModuleHolder {
        private static final AtomicIntegerFieldUpdater<ModuleHolder> stateUpdater = AtomicIntegerFieldUpdater.newUpdater(ModuleHolder.class, "state");

        private static final int CHECKED = 1;
        private static final int MAIN_CHECKED = 2;
        private static final int STARTED = 4;

        @Getter
        private final Module module;
        @Getter
        private final String name;

        private volatile int state;

        public ModuleHolder(@Nonnull Module module, @Nonnull String name) {
            this.module = module;
            this.name = name;
            this.state = 0;
        }

        public void start() {
            int value;
            if((value = stateUpdater.get(this) & STARTED) != STARTED) {
                module.start();
                stateUpdater.set(this, value | STARTED);
            }
        }

        public void shutdown() {
            int value;
            if((value = stateUpdater.get(this) & STARTED) == STARTED) {
                module.shutdown();
                stateUpdater.set(this, value & ~STARTED);
            }
        }

        public void shutdownNow() {
            int value;
            if((value = stateUpdater.get(this) & STARTED) == STARTED) {
                module.shutdownNow();
                stateUpdater.set(this, value & ~STARTED);
            }
        }

        public boolean isRunning() {
            return (stateUpdater.get(this) & STARTED) == STARTED;
        }

        public void check() {
            int value = stateUpdater.get(this);
            while(!stateUpdater.compareAndSet(this, value, value | CHECKED)) {
                value = stateUpdater.get(this);
            }
        }

        public void uncheck() {
            int value = stateUpdater.get(this);
            while(!stateUpdater.compareAndSet(this, value, value & ~CHECKED)) {
                value = stateUpdater.get(this);
            }
        }

        public boolean isChecked() {
            return (stateUpdater.get(this) & CHECKED) == CHECKED;
        }

        public String getType() {
            return module.getName();
        }

        public void mainCheck() {
            int value = stateUpdater.get(this);
            while(!stateUpdater.compareAndSet(this, value, value | MAIN_CHECKED)) {
                value = stateUpdater.get(this);
            }
        }

        public void mainUncheck() {
            int value = stateUpdater.get(this);
            while(!stateUpdater.compareAndSet(this, value, value & ~MAIN_CHECKED)) {
                value = stateUpdater.get(this);
            }
        }

        public boolean mainIsChecked() {
            return (stateUpdater.get(this) & MAIN_CHECKED) == MAIN_CHECKED;
        }
    }
}
