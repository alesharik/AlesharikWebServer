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

import com.alesharik.webserver.api.agent.Agent;
import com.alesharik.webserver.exceptions.error.ConfigurationParseError;
import com.alesharik.webserver.logger.Debug;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefixes;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;

@Prefixes("[Configurator]")
public class Configurator {
    /**
     * Default value of <code>logger.listenerQueueCapacity</code>
     */
    private static final int LOGGER_LISTENER_QUEUE_CAPACITY = 200;

    private final File file;
    private final AtomicBoolean isFileCheckerRunning;
    private final Configuration configuration;
    private final Class<?> pluginManagerClass;
    private final AtomicBoolean isApiSetup;

    private FileChecker fileChecker;
    private PluginManager pluginManager;

    public Configurator(@Nonnull File file, Configuration configuration, Class<?> pluginManagerClass) {
        Objects.requireNonNull(configuration);
        Objects.requireNonNull(file);
        if(file.isDirectory() || !file.exists()) {
            throw new IllegalArgumentException("File " + file + " is a folder or does not exists!");
        }

        this.file = file;
        this.configuration = configuration;
        this.pluginManagerClass = pluginManagerClass;
        this.isFileCheckerRunning = new AtomicBoolean(false);
        this.isApiSetup = new AtomicBoolean(false);
    }

    public synchronized void parse() throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder.parse(file);

        Element config = (Element) document.getElementsByTagName("configuration").item(0);
        Element api = (Element) config.getElementsByTagName("api").item(0);
        parseApiNode(api);

        Element modules = (Element) config.getElementsByTagName("modules").item(0);
        configuration.parseModules(modules);
        Element main = (Element) config.getElementsByTagName("main").item(0);
        configuration.parseMain(main);
    }

    private void parseApiNode(Element node) {
        if(isApiSetup.get()) {
            return;
        }

        //====================Logger setup====================\\
        int listenerThreadQueueCapacity = LOGGER_LISTENER_QUEUE_CAPACITY;
        File logFile = null;
        Element logger = (Element) node.getElementsByTagName("logger").item(0);
        if(logger != null) {
            Node listenerThreadQueueSize = logger.getElementsByTagName("listenerQueueCapacity").item(0);
            if(listenerThreadQueueSize != null) {
                listenerThreadQueueCapacity = Integer.parseInt(listenerThreadQueueSize.getTextContent());
            }

            Node logFileNode = logger.getElementsByTagName("logFile").item(0);
            if(logFileNode == null) {
                throw new ConfigurationParseError("logger.logFile node not found");
            }
            Date date = new Date();
            date.setTime(System.currentTimeMillis());
            logFile = new File(logFileNode.getTextContent().replace("{$time}", date.toString().replace(" ", "_")));

            if(!logFile.getParentFile().mkdirs() && !logFile.getParentFile().exists()) {
                throw new ConfigurationParseError("Can't create directories for log file");
            }
            try {
                if(!logFile.createNewFile()) {
                    throw new ConfigurationParseError("Can't create log file");
                }
            } catch (IOException e) {
                throw new ConfigurationParseError(e);
            }

            Node debugNode = logger.getElementsByTagName("debug").item(0);
            if(debugNode != null && Boolean.parseBoolean(debugNode.getTextContent()))
                Debug.enable();
        }
        Logger.setupLogger(logFile, listenerThreadQueueCapacity);
        //====================End logger setup====================\\

        //====================Configurator setup====================\\
        Element config = (Element) node.getElementsByTagName("config").item(0);
        Node fileCheckEnabled = config.getElementsByTagName("fileCheckEnabled").item(0);
        boolean isConfigFileCheckEnabled = Boolean.valueOf(fileCheckEnabled.getTextContent());
        if(isConfigFileCheckEnabled && !isFileCheckerRunning.get() && fileChecker == null) {
            try {
                startFileChecker();
                Logger.log("Configuration file checker successfully started at file " + file.toPath().toString() + "!");
            } catch (IOException e) {
                Logger.log("Oops! We can't setup configuration file checker on file " + file.toPath().toString() + "!");
                Logger.log(e);
            }
        }
        //====================End configurator setup====================\\

        //====================Plugin manager setup====================\\
        Element pluginManagerNode = (Element) node.getElementsByTagName("pluginManager").item(0);
        if(pluginManagerNode != null) {
            if(pluginManagerNode == null) {
                throw new ConfigurationParseError("plugin.folder node not found");
            }

            Node pluginHotReloadNode = pluginManagerNode.getElementsByTagName("hotReload").item(0);
            boolean hotReload = true;
            if(pluginHotReloadNode != null) {
                hotReload = Boolean.valueOf(pluginHotReloadNode.getTextContent());
            }

            Node pluginFolderNode = pluginManagerNode.getElementsByTagName("folder").item(0);
            File pluginFolder = new File(pluginFolderNode.getTextContent());
            if(pluginFolder.isFile()) {
                throw new ConfigurationParseError("Selected plugin folder is file");
            }
            if(!pluginFolder.exists() && !pluginFolder.mkdirs()) {
                throw new ConfigurationParseError("Can`t create plugin folder");
            }

            System.out.println("Folder " + pluginFolder + " used as plugin folder");

            try {
                pluginManager = (PluginManager) pluginManagerClass.getConstructor(File.class, boolean.class).newInstance(pluginFolder, hotReload);
                pluginManager.start();

                PluginManagerFreeSignaller signaller = new PluginManagerFreeSignaller(pluginManager);
                if(signaller.thread != null) {
                    ForkJoinPool.managedBlock(signaller);
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
        //====================End plugin manager setup====================\\

        //====================Hooks====================\\

        Element hooksNode = (Element) node.getElementsByTagName("hooks").item(0);
        if(hooksNode != null) {
            System.out.println("Loading user-defined hooks");
            configuration.clearHooks();
            NodeList hooks = hooksNode.getElementsByTagName("hook");
            for(int i = 0; i < hooks.getLength(); i++) {
                configuration.parseHook((Element) hooks.item(i));
            }
            System.out.println("User-defined hooks successfully loaded");
        }

        //====================End hooks====================\\

        System.out.println("API successfully configured!");
        isApiSetup.set(true);
    }

    private void startFileChecker() throws IOException {
        if(!isFileCheckerRunning.get() || fileChecker == null) {
            isFileCheckerRunning.set(true);
            fileChecker = new FileChecker();
            fileChecker.start();
        }
    }

    public void shutdownFileChecker() {
        if(isFileCheckerRunning.get() && fileChecker != null) {
            try {
                isFileCheckerRunning.set(false);
                fileChecker.shutdownWatcher();

                fileChecker = null;
            } catch (IOException e) {
                Logger.log("Oops! We catch an exception while stopping file checker!");
                Logger.log(e);
            }
        }
    }

    public void shutdown() {
        shutdownFileChecker();
        configuration.shutdown();
    }

    public void shutdownNow() {
        if(fileChecker != null)
            fileChecker.interrupt();
        configuration.shutdownNow();
    }

    private static final class PluginManagerFreeSignaller implements ForkJoinPool.ManagedBlocker {
        private final PluginManager pluginManager;
        volatile Thread thread;

        private PluginManagerFreeSignaller(PluginManager pluginManager) {
            this.pluginManager = pluginManager;
            this.thread = Thread.currentThread();
        }

        @Override
        public boolean block() throws InterruptedException {
            if(isReleasable()) {
                return true;
            } else {
                Thread.sleep(1);
            }
            return isReleasable();
        }

        @Override
        public boolean isReleasable() {
            boolean realisable = pluginManager.isFree() && !Agent.isScanning();
            if(realisable) {
                thread = null;
            }
            return realisable;
        }
    }

    /**
     * This class check config file for changes
     */
    @Prefixes({"[Configurator]", "[FileChecker]"})
    private final class FileChecker extends Thread {
        private final Path path;
        private final WatchService watchService;

        public FileChecker() throws IOException {
            path = file.toPath();
            Path parent = path.getParent();
            if(parent == null) {
                throw new IOException("File " + file + " has no parent");
            }
            watchService = parent.getFileSystem().newWatchService();
            parent.register(watchService, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.OVERFLOW);
            setName("ConfigurationFileChecker");
            setDaemon(true);
        }

        public synchronized void shutdownWatcher() throws IOException {
            watchService.close();
        }

        @Override
        public void run() {
            try {
                while(isFileCheckerRunning.get()) {
                    WatchKey watchKey = watchService.take();

                    List<WatchEvent<?>> events = watchKey.pollEvents();
                    for(WatchEvent<?> event : events) {

                        Path filename = (Path) event.context();
                        if(!path.equals(filename)) {
                            continue;
                        }
                        WatchEvent.Kind<?> kind = event.kind();
                        if(kind == StandardWatchEventKinds.ENTRY_DELETE) {
                            Logger.log("Configuration file" + filename.toString() + " was deleted! Stopping file checker...");
                            shutdownWatcher();
                        } else if(kind == StandardWatchEventKinds.OVERFLOW) {
                            Logger.log("Oops! WatchService has overflow on file " + filename.toString() + "! Please reduce amount of writings in this file!");
                        } else if(kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                            Logger.log("Configuration file " + filename.toString() + " change detected! Reloading configuration...");
                            parse();
                        }
                    }
                }
            } catch (ClosedWatchServiceException e) {
                //Ok, we receive exit signal
            } catch (InterruptedException | IOException | SAXException | ParserConfigurationException e) {
                Logger.log(e);
            }
        }
    }
}
