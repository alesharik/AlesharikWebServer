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

import com.alesharik.webserver.api.misc.Triple;
import com.alesharik.webserver.exceptions.error.ConfigurationParseError;
import com.alesharik.webserver.hook.Hook;
import com.alesharik.webserver.hook.HookManager;
import com.alesharik.webserver.hook.UserHookManager;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefixes;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.stream.Collectors;

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
            Element hooksNode = (Element) element.getElementsByTagName("hooks").item(0);
            if(typeNode != null && nameNode != null) {
                type = typeNode.getTextContent();
                name = nameNode.getTextContent();
            } else {
                Logger.log("Node №" + i + " has invalid configuration! Skipping...");
                continue;
            }
            List<Pair<ModuleHolder.Action, Hook>> systemHooks = new ArrayList<>();
            List<Pair<String, Hook>> userHooks = new ArrayList<>();//FIXME
            if(hooksNode != null) {
                NodeList childNodes = hooksNode.getChildNodes();
                for(int j = 0; j < childNodes.getLength(); j++) {
                    if(childNodes.item(j).getNodeType() != Node.ELEMENT_NODE)
                        continue;
                    Element node = (Element) childNodes.item(j);
                    if(node.getTagName().equals("start")) {
                        Hook hook = HookManager.getHookForName(node.getTextContent());
                        if(hook == null)
                            throw new ConfigurationParseError("Hook " + node.getTextContent() + " not found!");
                        systemHooks.add(Pair.of(ModuleHolder.Action.START, hook));
                    } else if(node.getTagName().equals("reload")) {
                        Hook hook = HookManager.getHookForName(node.getTextContent());
                        if(hook == null)
                            throw new ConfigurationParseError("Hook " + node.getTextContent() + " not found!");
                        systemHooks.add(Pair.of(ModuleHolder.Action.RELOAD, hook));
                    } else if(node.getTagName().equals("shutdown")) {
                        Hook hook = HookManager.getHookForName(node.getTextContent());
                        if(hook == null)
                            throw new ConfigurationParseError("Hook " + node.getTextContent() + " not found!");
                        systemHooks.add(Pair.of(ModuleHolder.Action.SHUTDOWN, hook));
                    } else {
                        Hook hook = HookManager.getHookForName(node.getTextContent());
                        if(hook == null)
                            throw new ConfigurationParseError("Hook " + node.getTextContent() + " not found!");
                        userHooks.add(Pair.of(node.getTagName(), hook));
                    }
                }
            }

            Module module = ModuleManager.getModuleByName(type);
            if(module == null) {
                Logger.log("Module with type " + type + " not found! Skipping...");
            } else {
                if(module.getHookManager() != null)
                    module.getHookManager().loadHooks(userHooks);

                if(!isInit || !this.modules.containsKey(type)) {
                    ModuleHolder moduleHolder = new ModuleHolder(module, name, systemHooks);
                    this.modules.put(name, moduleHolder);
                    moduleHolder.parse(configNode);
                    Logger.log("Module " + name + " with type " + type + " successfully loaded!");
                } else {
                    ModuleHolder moduleHolder = this.modules.get(name);
                    moduleHolder.uncheck();
                    moduleHolder.reload(configNode);
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
        NodeList initChildNodes = initNode.getChildNodes();
        for(int i = 0; i < initChildNodes.getLength(); i++) {
            if(initChildNodes.item(i).getNodeType() != Node.ELEMENT_NODE)
                continue;
            Element item = (Element) initChildNodes.item(i);
            if(item.getTagName().equals("start-module")) {
                String name = item.getTextContent();
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
            } else if(item.getTagName().equals("hook")) {
                String hookName = item.getTextContent();
                Hook hook = HookManager.getHookForName(hookName);
                if(hook == null)
                    throw new ConfigurationParseError("Hook " + hookName + " not found!");
                else
                    hook.listen(null, new Object[0]);
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
    public void parseHook(Element hook) {
        UserHookManager.parseHook(hook);
    }

    @Override
    public Module getModuleByName(String name) {
        System.out.println("Trying to load " + name);
        return modules.get(name);
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

    public Set<Triple<Module, String, Boolean>> getModules() {
        return modules.entrySet().stream()
                .map(moduleHolder -> Triple.immutable(moduleHolder.getValue().module, moduleHolder.getKey(), moduleHolder.getValue().isRunning()))
                .collect(Collectors.toSet());
    }

    public synchronized void reloadModule(String name) {
        ModuleHolder moduleHolder = modules.get(name);
        try {
            System.out.println("Reloading module " + name);
            if(!moduleHolder.isRunning())
                moduleHolder.start();
            else
                moduleHolder.reload();
            System.out.println("Module " + name + " successfully reloaded");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @ModuleManager.Ignored
    @ToString
    @EqualsAndHashCode
    static final class ModuleHolder implements Module {
        private static final AtomicIntegerFieldUpdater<ModuleHolder> stateUpdater = AtomicIntegerFieldUpdater.newUpdater(ModuleHolder.class, "state");

        private static final int CHECKED = 1;
        private static final int MAIN_CHECKED = 2;
        private static final int STARTED = 4;

        @Getter
        private final Module module;
        @Getter
        private final String name;
        private final List<Pair<Action, Hook>> systemHooks;
        private volatile Element config;

        private volatile int state;

        public ModuleHolder(@Nonnull Module module, @Nonnull String name, List<Pair<Action, Hook>> systemHooks) {
            this.module = module;
            this.name = name;
            this.systemHooks = systemHooks;
            this.state = 0;
        }

        public void parse(Element element) {
            config = element;
            module.parse(element);

            fireEvent(Action.RELOAD);
        }

        public void reload(Element element) {
            config = element;
            module.reload(element);

            fireEvent(Action.RELOAD);
        }

        public void reload() {
            module.reload(config);
        }

        public void start() {
            int value;
            if((value = stateUpdater.get(this) & STARTED) != STARTED) {
                module.start();
                stateUpdater.set(this, value | STARTED);

                fireEvent(Action.START);
            }
        }

        public void shutdown() {
            int value;
            if((value = stateUpdater.get(this) & STARTED) == STARTED) {
                module.shutdown();
                stateUpdater.set(this, value & ~STARTED);

                fireEvent(Action.SHUTDOWN);
            }
        }

        public void shutdownNow() {
            int value;
            if((value = stateUpdater.get(this) & STARTED) == STARTED) {
                module.shutdownNow();
                stateUpdater.set(this, value & ~STARTED);
            }
        }

        @Nullable
        @Override
        public Layer getMainLayer() {
            return module.getMainLayer();
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

        private void fireEvent(Action action) {
            for(Pair<Action, Hook> hookActionEntry : systemHooks) {
                if(hookActionEntry.getKey() == action)
                    hookActionEntry.getValue().listen(module, new Object[0]);
            }
        }

        private enum Action {
            START,
            SHUTDOWN,
            RELOAD
        }
    }
}
