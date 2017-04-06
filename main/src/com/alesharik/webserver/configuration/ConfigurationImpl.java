package com.alesharik.webserver.configuration;

import com.alesharik.webserver.logger.Logger;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

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
                    Logger.log("Module " + stringModuleHolderEntry.getKey() + " with type " + stringModuleHolderEntry.getValue().getType() + " shutdown!");
                });
    }

    @Override
    public Module getModuleByName(String name) {
        modules.forEach((s, moduleHolder) -> System.out.println("Module " + s + " at " + moduleHolder));
        System.out.println("Trying to load " + name);
        return modules.get(name).getModule();
    }

    @ToString
    @EqualsAndHashCode
    private static final class ModuleHolder {
        @Getter
        private final Module module;
        private final AtomicBoolean isStarted;
        private final AtomicBoolean isChecked;
        private final AtomicBoolean isMainChecked;
        @Getter
        private final String name;

        public ModuleHolder(Module module, String name) {
            this.module = module;
            isStarted = new AtomicBoolean(false);
            isChecked = new AtomicBoolean(false);
            isMainChecked = new AtomicBoolean(false);
            this.name = name;
        }

        public void start() {
            if(!isStarted.get()) {
                isStarted.set(true);
                module.start();
            }
        }

        public void shutdown() {
            if(isStarted.get()) {
                isStarted.set(false);
                module.shutdown();
            }
        }

        public boolean isRunning() {
            return isStarted.get();
        }

        public void check() {
            isChecked.set(true);
        }

        public void uncheck() {
            isChecked.set(false);
        }

        public boolean isChecked() {
            return isChecked.get();
        }

        public String getType() {
            return module.getName();
        }

        public void mainCheck() {
            isMainChecked.set(true);
        }

        public void mainUncheck() {
            isMainChecked.set(false);
        }

        public boolean mainIsChecked() {
            return isMainChecked.get();
        }
    }
}
