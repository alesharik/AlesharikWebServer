package com.alesharik.webserver.main;

import com.alesharik.webserver.configuration.Configuration;
import com.alesharik.webserver.configuration.ConfigurationImpl;
import com.alesharik.webserver.configuration.Configurator;
import com.alesharik.webserver.configuration.PluginManagerImpl;
import com.alesharik.webserver.configuration.XmlHelper;
import com.alesharik.webserver.exceptions.error.ConfigurationParseError;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.module.server.ControlServerModule;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Scanner;

//TODO rewrite site as green terminal
//TODO add more prefixes to java
//TODO написать профилирование базы данных и реквестов

public class Main {
    private static final MainLoggerConfiguration c = new MainLoggerConfiguration();

    public static final File USER_DIR = new File(System.getProperty("user.dir"));
    @Deprecated
    public static final File WWW = new File(USER_DIR + "/www");
    @Deprecated
    public static final File SERVER_DASHBOARD = new File(USER_DIR + "/serverDashboard");

    private static final File CONFIG = new File("./configuration.xml");
    private static Configurator configurator;
    private static Configuration configuration = new ConfigurationImpl();

    public static void main(String[] args) throws InterruptedException {
        try {
            configuration = new ConfigurationImpl();
            XmlHelper.setConfiguration(configuration);
            configurator = new Configurator(CONFIG, configuration, PluginManagerImpl.class);
            configurator.parse();

            Runtime.getRuntime().addShutdownHook(new Thread(Main::shutdown));

            Scanner scanner = new Scanner(System.in, "UTF-8");
            while(true) {
                if(!scanner.hasNextLine()) {
                    Thread.sleep(1);
                    continue;
                }
                String command = scanner.nextLine();
                switch (command) {
                    case "exit":
                        shutdown();
                        return;
                    default:
                        System.out.println("WTF");
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            shutdown();
        }
    }


    @Nullable //TODO remove
    public static ControlServerModule getControlServer(String nodeName, Element config, boolean required) {
        Node nameNode = config.getElementsByTagName(nodeName).item(0);
        if(nameNode == null) {
            if(required) {
                throw new ConfigurationParseError("Node " + nodeName + " not found!");
            } else {
                return null;
            }
        } else {
            try {
                return (ControlServerModule) configuration.getModuleByName(nameNode.getTextContent());
            } catch (ClassCastException e) {
                throw new ConfigurationParseError("Node " + nodeName + " type not expected!", e);
            }
        }
    }

    public static File getConfigurationFile() {
        return CONFIG;
    }

    public synchronized static void shutdown() {
        Logger.log("[MAIN]", "Stopping...");
        configurator.shutdown();
        System.exit(0);
    }
}