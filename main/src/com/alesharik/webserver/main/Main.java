package com.alesharik.webserver.main;

import com.alesharik.webserver.configuration.Configuration;
import com.alesharik.webserver.configuration.ConfigurationImpl;
import com.alesharik.webserver.configuration.Configurator;
import com.alesharik.webserver.configuration.PluginManagerImpl;
import com.alesharik.webserver.configuration.XmlHelper;
import com.alesharik.webserver.exceptions.error.ConfigurationParseError;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefixes;
import com.alesharik.webserver.module.server.ControlServerModule;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

@Prefixes("[MAIN]")
public class Main {
    public static final File USER_DIR = new File(System.getProperty("user.dir"));

    private static final File CONFIG = new File("./configuration.xml");

    private static Configurator configurator;
    private static Configuration configuration;

    public static void main(String[] args) throws InterruptedException {
        try {
            configuration = new ConfigurationImpl();
            XmlHelper.setConfiguration(configuration);

            configurator = new Configurator(CONFIG, configuration, PluginManagerImpl.class);
            configurator.parse();

            System.out.println("Server successfully loaded!");

            Runtime.getRuntime().addShutdownHook(new Thread(Main::shutdownNow));

            ConsoleOrBufferedReader consoleReader;

            Console console = System.console();
            if(console == null) {
                System.out.println("Server has no input console! Falling to input stream...");
                consoleReader = new ConsoleOrBufferedReader.BufferedReaderWrapper(new BufferedReader(new InputStreamReader(System.in)));
            } else {
                consoleReader = new ConsoleOrBufferedReader.ConsoleWrapper(console);
            }

            System.out.println("Server is listening terminal commands...");
            while(true) {
                String command = consoleReader.nextLine();
                if(command == null) {
                    System.out.println("Console listener was reached end of stream! Stopping console listening...");
                    return;
                }

                switch (command) {
                    case "exit":
                        shutdown();
                        return;
                    default:
                        System.out.println("Command " + command + " not found!");
                }
            }
        } catch (ConfigurationParseError e) {
            System.err.println("Configuration error occurs! Stopping...");
            shutdown();
        } catch (Error e) {
            e.printStackTrace();
            shutdownNow();
        } catch (Throwable e) {
            e.printStackTrace();
            shutdown();
        }
    }

    private abstract static class ConsoleOrBufferedReader {
        /**
         * Null means that we reached end of stream
         */
        @Nullable
        public abstract String nextLine();

        public abstract boolean supportPasswordEnter();

        @Nonnull
        public abstract char[] readPassword();

        private static final class ConsoleWrapper extends ConsoleOrBufferedReader {
            private final Console console;

            public ConsoleWrapper(@Nonnull Console console) {
                this.console = console;
            }

            @Override
            public String nextLine() {
                return console.readLine();
            }

            @Override
            public boolean supportPasswordEnter() {
                return true;
            }

            @Override
            public char[] readPassword() {
                return console.readPassword();
            }
        }

        private static final class BufferedReaderWrapper extends ConsoleOrBufferedReader {
            private final BufferedReader stream;

            public BufferedReaderWrapper(@Nonnull BufferedReader stream) {
                this.stream = stream;
            }

            @Override
            public String nextLine() {
                try {
                    return stream.readLine();
                } catch (IOException e) {
                    return null;
                }
            }

            @Override
            public boolean supportPasswordEnter() {
                return false;
            }

            @Override
            public char[] readPassword() {
                return new char[0];
            }
        }
    }

    @Deprecated
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

    /**
     * Return server configuration file
     */
    @Nonnull
    public static File getConfigurationFile() {
        return CONFIG;
    }

    public synchronized static void shutdown() {
        Logger.log("Stopping...");
        configurator.shutdown();
        System.exit(0);
    }

    private synchronized static void shutdownNow() {
        System.err.println("Critical error detected! Stopping...");
        configurator.shutdownNow();
        System.exit(0);
    }
}