package com.alesharik.webserver.configuration;

import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefix;
import com.alesharik.webserver.logger.Prefixes;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

@Prefix("[Configurator]")
public class Configurator {
    private final File file;
    private final AtomicBoolean isFileCheckerRunning;
    private final Configuration configuration;

    private FileChecker fileChecker;

    public Configurator(File file, Configuration configuration) {
        Objects.requireNonNull(configuration);
        Objects.requireNonNull(file);
        if(file.isDirectory() || !file.exists()) {
            throw new IllegalArgumentException("File is a folder or does not exists!");
        }

        this.file = file;
        this.configuration = configuration;
        this.isFileCheckerRunning = new AtomicBoolean(false);
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
    }

    private void startFileChecker() throws IOException {
        if(!isFileCheckerRunning.get() || fileChecker == null) {
            isFileCheckerRunning.set(true);
            fileChecker = new FileChecker();
            fileChecker.start();
        }
    }

    public void shutdown() {
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

    /**
     * This class check config file for changes
     */
    @Prefixes({"[Configurator]", "[FileChecker]"})
    private final class FileChecker extends Thread {
        private final Path path;
        private final WatchService watchService;

        public FileChecker() throws IOException {
            path = file.toPath();
            watchService = path.getFileSystem().newWatchService();
            path.register(watchService, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.OVERFLOW);
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
                        WatchEvent.Kind<?> kind = event.kind();
                        if(kind == StandardWatchEventKinds.ENTRY_DELETE) {
                            Logger.log("Configuration file" + event.context().toString() + " was deleted! Stopping file checker...");
                            shutdownWatcher();
                        } else if(kind == StandardWatchEventKinds.OVERFLOW) {
                            Logger.log("Oops! WatchService has overflow on file " + event.context().toString() + "! Please reduce amount of writings in this file!");
                        } else if(kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                            Logger.log("Configuration file " + event.context().toString() + " change detected! Reloading configuration...");
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
