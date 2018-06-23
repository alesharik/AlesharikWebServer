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

import com.alesharik.webserver.api.ExecutionStage;
import com.alesharik.webserver.configuration.config.lang.ApiEndpointSection;
import com.alesharik.webserver.configuration.config.lang.ConfigurationEndpoint;
import com.alesharik.webserver.configuration.config.lang.ScriptEndpointSection;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationElement;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationObject;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationPrimitive;
import com.alesharik.webserver.configuration.config.lang.parser.ConfigurationParser;
import com.alesharik.webserver.configuration.run.DirectoryWatcher;
import com.alesharik.webserver.configuration.run.Extension;
import com.alesharik.webserver.configuration.run.ExtensionManager;
import com.alesharik.webserver.configuration.run.message.Message;
import com.alesharik.webserver.configuration.run.message.MessageManager;
import com.alesharik.webserver.configuration.run.message.MessageSender;
import com.alesharik.webserver.exceptions.error.ConfigurationParseError;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefixes;
import com.alesharik.webserver.logger.level.Level;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ExecutionStage.AuthorizedImpl
@Level("configuration-runner")
@Prefixes("[ConfigurationRunner]")
final class ConfigurationRunner extends Thread implements FileWatcherThread.ConfigListener {
    static {
        Logger.getLoggingLevelManager().createLoggingLevel("configuration-runner");
    }

    private final File configFile;
    private final ConfigurationParser parser;
    private final boolean reloadConfig;
    private ConfigurationEndpoint configuration;
    private final Map<String, Extension> extensions = new HashMap<>();

    private final Object completionLock = new Object();
    private final MessageBus bus = new MessageBus();
    private final ExtensionPool extensionPool = new ExtensionPool(extensions);
    private FileWatcherThread watcherThread;
    private volatile boolean running = false;

    private volatile boolean noConfig = false;

    private final Object configUpdateTrigger = new Object();

    public ConfigurationRunner(File configFile, ConfigurationParser parser, boolean reloadConfig, ConfigurationEndpoint configuration) {
        this.configFile = configFile;
        this.parser = parser;
        this.reloadConfig = reloadConfig;
        this.configuration = configuration;
    }

    @Override
    public void run() {
        System.out.println("Starting...");
        bus.start();
        if(reloadConfig)
            watcherThread = new FileWatcherThread(configFile, this);
        else
            watcherThread = new FileWatcherThread(null, this);

        ExecutionStage.setState(ExecutionStage.LOAD_EXTENSIONS);
        System.out.println("Loading...");

        String blacklistString = System.getProperty("extension-blacklist");
        List<String> extensionBlacklist = new ArrayList<>();
        if(blacklistString != null) {
            String[] split = blacklistString.split(",");
            extensionBlacklist.addAll(Arrays.asList(split));
        }

        for(String s : ExtensionManager.getExtensions())
            if(!extensionBlacklist.contains(s))
                extensions.put(s, ExtensionManager.createExtension(s));
            else
                System.out.println("Ignoring " + s + " extension due to blacklist");

        System.out.println("Extension init complete");
        System.out.println("Starting pool...");
        extensionPool.start();

        System.out.println("Registering extensions...");
        for(Map.Entry<String, Extension> stringExtensionEntry : extensions.entrySet())
            if(stringExtensionEntry.getValue().getMessageManager() != null)
                bus.addManager(new MessageManagerWrapper(stringExtensionEntry.getValue().getMessageManager(), extensionPool, stringExtensionEntry.getKey()), stringExtensionEntry.getKey());
        for(Map.Entry<String, Extension> extension : extensions.entrySet())
            for(DirectoryWatcher directoryWatcher : extension.getValue().getFileWatchers())
                watcherThread.addDirectoryWatcher(new DirectoryWatcherWrapper(directoryWatcher, extensionPool, extension.getKey()));

        System.out.println("Loading extensions...");
        extensionPool.load(configuration, Main.getScriptEngine());
        extensionPool.waitQuiescence();

        System.out.println("Executing config...");
        ExecutionStage.setState(ExecutionStage.START);
        ScriptEndpointSection section = configuration.getScriptSection();
        System.out.println("Stage pre-init");
        ScriptEndpointSection.ScriptSection preInit = section.getSection("pre-init");
        if(preInit != null) {
            for(ScriptEndpointSection.Command command : preInit.getCommands()) {
                String one = null;
                for(Map.Entry<String, Extension> e : extensions.entrySet()) {
                    if(e.getValue().getCommandExecutor().getPredicate().test(e.getKey())) {
                        extensionPool.executeCommand(e.getKey(), command);
                        if(one != null)
                            throw new CommandRedefinitionError(command.getName(), one, e.getKey());
                        one = e.getKey();
                    }
                }
            }
        }
        extensionPool.waitQuiescence();

        System.out.println("Stage init");
        ScriptEndpointSection.ScriptSection init = section.getSection("init");
        if(preInit != null) {
            for(ScriptEndpointSection.Command command : init.getCommands()) {
                String one = null;
                for(Map.Entry<String, Extension> e : extensions.entrySet()) {
                    if(e.getValue().getCommandExecutor().getPredicate().test(e.getKey())) {
                        extensionPool.executeCommand(e.getKey(), command);
                        if(one != null)
                            throw new CommandRedefinitionError(command.getName(), one, e.getKey());
                        one = e.getKey();
                    }
                }
            }
        }
        extensionPool.waitQuiescence();

        System.out.println("Starting extensions...");
        extensionPool.execStart();
        extensionPool.waitQuiescence();

        System.out.println("Stage post-init");
        ScriptEndpointSection.ScriptSection postInit = section.getSection("post-init");
        if(postInit != null) {
            for(ScriptEndpointSection.Command command : postInit.getCommands()) {
                String one = null;
                for(Map.Entry<String, Extension> e : extensions.entrySet()) {
                    if(e.getValue().getCommandExecutor().getPredicate().test(e.getKey())) {
                        extensionPool.executeCommand(e.getKey(), command);
                        if(one != null)
                            throw new CommandRedefinitionError(command.getName(), one, e.getKey());
                        one = e.getKey();
                    }
                }
            }
        }
        extensionPool.waitQuiescence();

        System.out.println("Extensions started");
        System.out.println("Preparing for going live...");
        ExecutionStage.setState(ExecutionStage.POST_START);
        watcherThread.start();

        System.out.println("Server is live");
        ExecutionStage.setState(ExecutionStage.EXECUTE);

        synchronized (completionLock) {
            running = true;
            completionLock.notifyAll();
        }

        if(reloadConfig)
            updateLoop();
    }

    private void updateLoop() {
        while(isAlive() && !isInterrupted()) {
            synchronized (configUpdateTrigger) {
                try {
                    configUpdateTrigger.wait();
                } catch (InterruptedException e) {
                    return;
                }
            }
            System.out.println("Reloading configuration...");
            ConfigurationEndpoint n;
            try {
                n = parser.parse();
            } catch (ConfigurationParseError e) {
                e.printStackTrace();
                continue;
            }

            System.out.println("Reloading API section...");
            ApiEndpointSection apiEndpointSection = n.getApiSection();
            ConfigurationObject object = (ConfigurationObject) apiEndpointSection.getElement("configuration");
            if(object != null) {
                ConfigurationElement element = object.getElement("auto-reload");
                boolean reloadConfig = Main.getScriptEngine().isExecutable(element)
                        ? Main.getScriptEngine().execute(element, Boolean.class)
                        : ((ConfigurationPrimitive.Boolean) element).value();
                if(!reloadConfig) {
                    System.err.println("Configuration reloading disabled!");
                    break;
                }
            }

            System.out.println("Sending new configuration to extensions...");
            extensionPool.reload(configuration, n, Main.getScriptEngine());

            System.out.println("Updating local configuration instance...");
            configuration = n;
            System.out.println("Configuration reloaded");
        }
    }

    public void shutdown() {//TODO shutdown sequence!
        watcherThread.shutdown();
        extensionPool.shutdown();
    }

    public void shutdownNow() {
        watcherThread.shutdown();
        extensionPool.shutdownNow();
    }

    public void waitForCompletion() {
        synchronized (completionLock) {
            if(running)
                return;
            try {
                completionLock.wait();
            } catch (InterruptedException ignored) {
            }
        }
    }

    @Override
    public void configUpdated(@Nonnull File config) {
        if(noConfig)
            return;
        synchronized (configUpdateTrigger) {
            configUpdateTrigger.notifyAll();
        }
    }

    @Override
    public void configDeleted(@Nonnull File config) {
        System.err.println("Config was deleted! Switching to no-config mode...");
        noConfig = true;
    }

    @RequiredArgsConstructor
    private static final class DirectoryWatcherWrapper implements DirectoryWatcher {
        private final DirectoryWatcher watcher;
        private final ExtensionPool pool;
        private final String name;

        @Nonnull
        @Override
        public Path toWatch() {
            return watcher.toWatch();
        }

        @Override
        public void fileChanged(@Nonnull Path file, @Nonnull WatchEvent.Kind<Path> kind) {
            pool.addTask(name, () -> watcher.fileChanged(file, kind));
        }
    }

    @RequiredArgsConstructor
    private static final class MessageManagerWrapper implements MessageManager {
        private final MessageManager messageManager;
        private final ExtensionPool extensionPool;
        private final String name;

        @Override
        public void init(MessageSender messageSender) {
            messageManager.init(messageSender);
        }

        @Override
        public void listen(Message message, String sender) {
            extensionPool.addTask(name, () -> messageManager.listen(message, sender));
        }
    }
}
