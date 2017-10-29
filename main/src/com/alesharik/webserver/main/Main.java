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

import com.alesharik.webserver.api.ServerInfo;
import com.alesharik.webserver.api.agent.Agent;
import com.alesharik.webserver.configuration.Configuration;
import com.alesharik.webserver.configuration.ConfigurationImpl;
import com.alesharik.webserver.configuration.Configurator;
import com.alesharik.webserver.configuration.PluginManagerImpl;
import com.alesharik.webserver.configuration.XmlHelper;
import com.alesharik.webserver.exceptions.error.ConfigurationParseError;
import com.alesharik.webserver.exceptions.error.UnexpectedBehaviorError;
import com.alesharik.webserver.internals.InternalHackingError;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefixes;
import com.alesharik.webserver.main.console.ConsoleCommand;
import com.alesharik.webserver.main.console.ConsoleCommandManager;
import com.alesharik.webserver.main.console.impl.PluginConsoleCommand;
import com.alesharik.webserver.module.security.ControlServerModule;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

@Prefixes("[MAIN]")
public class Main {
    public static final File USER_DIR = new File(System.getProperty("user.dir"));

    private static final File CONFIG = new File("./configuration.xml");

    private static Configurator configurator;
    private static Configuration configuration;

    public static void main(String[] args) throws InterruptedException, IOException {
        preMain();

        boolean noConfig = false;

        for(String arg : args) {
            switch (arg) {
                case "--no-config":
                    noConfig = true;
                    break;
            }
        }

        if(noConfig) {
            Logger.setupLogger(File.createTempFile("AlesharikWebServer", "AWS"), 256);
            System.err.println("Starting server in no-config mode...");
            //noinspection StatementWithEmptyBody use spinlock here
            while(Agent.isScanning()) {
            }
            System.err.println("Server running in no-config mode! Press enter to stop");
            System.in.read();
            System.err.println("Stopping server...");
            return;
        }

        try {
            configuration = new ConfigurationImpl();
            XmlHelper.setConfiguration(configuration);
            PluginConsoleCommand.init((ConfigurationImpl) configuration);

            configurator = new Configurator(CONFIG, configuration, PluginManagerImpl.class);
            configurator.parse();

            System.out.println("Server successfully loaded!");

            Runtime.getRuntime().addShutdownHook(new ShutdownThread());

            ConsoleOrBufferedReader consoleReader;

            Console console = System.console();
            if(console == null) {
                System.out.println("Server has no input console! Falling to input stream...");
                consoleReader = new ConsoleOrBufferedReader.BufferedReaderWrapper(new BufferedReader(new InputStreamReader(System.in, "UTF-8")));
            } else {
                consoleReader = new ConsoleOrBufferedReader.ConsoleWrapper(console);
            }

            System.out.println("Found " + ConsoleCommandManager.getCommands().size() + " console commands");
            System.out.println("Server is listening terminal commands...");
            PrintStream out = Logger.getSystemOut();
            while(true) {
                String command = consoleReader.readLine();
                if(command == null) {
                    System.out.println("Console listener was reached end of stream! Stopping console listening...");
                    return;
                }
                if(command.isEmpty()) {
                    out.println("Please enter correct command!");
                    continue;
                }

                if(command.equals("help")) {
                    out.println("help list - display all possible commands");
                    out.println("help <command> - display command help");
                } else if(command.equals("help list")) {
                    for(ConsoleCommand consoleCommand : ConsoleCommandManager.getCommands())
                        out.println(consoleCommand.getName() + " -- " + consoleCommand.getDescription());
                } else if(command.startsWith("help ")) {
                    String cmdName = command.substring("help ".length());
                    ConsoleCommand cmd = ConsoleCommandManager.getCommand(cmdName);
                    if(cmd != null)
                        cmd.printHelp(out);
                    else
                        out.println("Command " + cmdName + " not found!");
                } else if(ConsoleCommandManager.containsCommand(command)) {
                    ConsoleCommand consoleCommand = ConsoleCommandManager.getCommand(command);
                    consoleCommand.handle(command, out, consoleReader);
                } else {
                    out.println("Command " + command + " not found!");
                }
            }
        } catch (ConfigurationParseError e) {
            e.printStackTrace();
            System.err.println("Configuration error occurs! Stopping...");
            shutdown();
        } catch (UnexpectedBehaviorError e) {
            e.printStackTrace();
            System.err.println("Unexpected behavior error occurs! Now server is in undefined state. Please check your java installation!");
            System.err.println("Stopping...");
            shutdownNow();
        } catch (InternalHackingError e) {
            e.printStackTrace();
            System.err.println("Internal hacking error occurs! Server can't access JVM internals! Stopping...");
            shutdownNow();
        } catch (Error e) {
            e.printStackTrace();
            System.err.println("Critical error detected! Stopping...");
            shutdownNow();
        } catch (Throwable e) {
            e.printStackTrace();
            shutdown();
        }
    }

    private static void preMain() {
        ServerInfo.setProvider(new ServerInfoProvider());
    }

    private abstract static class ConsoleOrBufferedReader implements ConsoleCommand.Reader {
        /**
         * Null means that we reached end of stream
         */
        @Nonnull
        public abstract String readLine();

        public abstract boolean passwordSupported();

        @Nullable
        public abstract char[] readPassword();

        private static final class ConsoleWrapper extends ConsoleOrBufferedReader {
            private final Console console;

            public ConsoleWrapper(@Nonnull Console console) {
                this.console = console;
            }

            @Override
            public String readLine() {
                return console.readLine();
            }

            @Override
            public boolean passwordSupported() {
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
            public String readLine() {
                try {
                    return stream.readLine();
                } catch (IOException e) {
                    return null;
                }
            }

            @Override
            public boolean passwordSupported() {
                return false;
            }

            @Override
            public char[] readPassword() {
                return null;
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

    private static void shutdownInternal() {
        Logger.log("Stopping...");
        configurator.shutdown();
        Agent.shutdown();
        Logger.shutdown();
    }

    public synchronized static void shutdown() {
        Logger.log("Stopping...");
        configurator.shutdown();
        Agent.shutdown();
        Logger.shutdown();
        System.exit(0);
    }

    public synchronized static void shutdownNow() {
        Logger.log("Emergency stopping...");
        configurator.shutdownNow();
        Agent.shutdown();
        Logger.shutdown();
        System.exit(0);
    }

    private static final class ShutdownThread extends Thread {
        public ShutdownThread() {
            setDaemon(true);
            setPriority(Thread.MAX_PRIORITY);
            setName("ShutdownHandler");
        }

        @Override
        public void run() {
            shutdownInternal();
        }
    }
}