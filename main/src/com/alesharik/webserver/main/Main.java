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
import com.alesharik.webserver.api.ServerInfo;
import com.alesharik.webserver.api.agent.Agent;
import com.alesharik.webserver.configuration.ApiHelper;
import com.alesharik.webserver.configuration.config.lang.ApiEndpointSection;
import com.alesharik.webserver.configuration.config.lang.ConfigurationEndpoint;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationElement;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationObject;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationObjectArray;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationPrimitive;
import com.alesharik.webserver.configuration.config.lang.parser.ConfigurationParser;
import com.alesharik.webserver.configuration.config.lang.parser.FileReader;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefixes;
import com.alesharik.webserver.main.console.ConsoleCommand;
import com.alesharik.webserver.main.console.ConsoleCommandManager;
import com.alesharik.webserver.main.script.ScriptEngineImpl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;

@Prefixes("[MAIN]")
@ExecutionStage.AuthorizedImpl
public final class Main {
    private static final FileReader FILE_READER = new FileReaderImpl();
    static CoreModuleManagerImpl coreModuleManager;
    static ScriptEngineImpl scriptEngine;//TODO add to context 2 interfaces

    public static void main(String[] args) throws IOException, InterruptedException {
        Logger.setupTemporary();
        ExecutionStage.enable();
        ExecutionStage.setState(ExecutionStage.PRE_LOAD);
        preMain();

        coreModuleManager = new CoreModuleManagerImpl(getCoreModulesFolder());
        coreModuleManager.load();

        long coreModulesScanStart = System.nanoTime();
        long coreModulesScanTime = 0;
        long coreModulesScanUpd = 0;
        while(Agent.isScanning()) {
            Thread.sleep(1);
            coreModulesScanTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - coreModulesScanStart);
            if((coreModulesScanTime - coreModulesScanUpd) / 1000 >= 1) {
                coreModulesScanUpd = coreModulesScanTime;
                System.out.println("Agent is scanning core modules for " + coreModulesScanTime + " milliseconds");
            }
        }


        File configuration = getConfigurationFile().getAbsoluteFile();
        ConfigurationParser parser = getConfigurationParser(configuration, FILE_READER);
        ConfigurationEndpoint endpoint = parser.parse();

        scriptEngine = new ScriptEngineImpl(FILE_READER, endpoint);
        scriptEngine.prepare();

        File sharedLibsDirectory;
        File moduleDirectory;
        boolean reloadConfig;
        boolean reloadModules;

        {
            ApiEndpointSection api = endpoint.getApiSection();

            ConfigurationObject logger = (ConfigurationObject) api.getElement("logger");

            ConfigurationElement fileElement = logger.getElement("file");
            File logFile = new File(scriptEngine.isExecutable(fileElement)
                    ? scriptEngine.execute(fileElement, String.class)
                    : ((ConfigurationPrimitive.String) fileElement).value());

            int queueCapacity;
            if(logger.hasKey("queue-capacity")) {
                ConfigurationElement queueCapacityElement = logger.getElement("queue-capacity");
                queueCapacity = scriptEngine.isExecutable(queueCapacityElement)
                        ? scriptEngine.execute(queueCapacityElement, Integer.class)
                        : ((ConfigurationPrimitive.Int) queueCapacityElement).value();
            } else
                queueCapacity = 1024;

            boolean debug;
            if(logger.hasKey("debug")) {
                ConfigurationElement debugElement = logger.getElement("debug");
                debug = scriptEngine.isExecutable(debugElement)
                        ? scriptEngine.execute(debugElement, Boolean.class)
                        : ((ConfigurationPrimitive.Boolean) debugElement).value();
            } else
                debug = false;

            ApiHelper.setupLogger(queueCapacity, logFile, debug);

            ConfigurationElement levelsElement = logger.getElement("levels");
            if(levelsElement != null) {
                if(scriptEngine.isExecutable(levelsElement)) {
                    String[] levels = scriptEngine.execute(levelsElement, String[].class);
                    for(String level : levels)
                        ApiHelper.enableLoggingLevelOutput(level);
                } else {
                    ConfigurationObjectArray levels = (ConfigurationObjectArray) levelsElement;
                    for(String s : levels.toStringArray())
                        ApiHelper.enableLoggingLevelOutput(s);
                }
            }

            //TODO mode

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

            ConfigurationObject configConfig = (ConfigurationObject) api.getElement("configuration");
            if(configConfig != null)
                if(configConfig.hasKey("auto-reload")) {
                    ConfigurationElement element = configConfig.getElement("auto-reload");
                    reloadConfig = scriptEngine.isExecutable(element)
                            ? scriptEngine.execute(element, Boolean.class)
                            : ((ConfigurationPrimitive.Boolean) element).value();
                } else
                    reloadConfig = false;
            else
                reloadConfig = false;

            sharedLibsDirectory = sharedLibsDirectory.getAbsoluteFile();
            moduleDirectory = moduleDirectory.getAbsoluteFile();
        }

        System.out.println("Test");
//        try {
//
//
//
//            System.out.println("Server successfully loaded!");
//
//            Runtime.getRuntime().addShutdownHook(new ShutdownThread());
//
//            startConsoleInput();
//            return;
//        } catch (ConfigurationParseError e) {
//            e.printStackTrace();
//            System.err.println("Configuration error occurs! Stopping...");
//            shutdown();
//        } catch (UnexpectedBehaviorError e) {
//            e.printStackTrace();
//            System.err.println("Unexpected behavior error occurs! Now server is in undefined state. Please check your java installation!");
//            System.err.println("Stopping...");
//            shutdownNow();
//        } catch (InternalHackingError e) {
//            e.printStackTrace();
//            System.err.println("Internal hacking error occurs! Server can't access JVM internals! Stopping...");
//            shutdownNow();
//        } catch (Error e) {
//            e.printStackTrace();
//            System.err.println("Critical error detected! Stopping...");
//            shutdownNow();
//        } catch (Throwable e) {
//            e.printStackTrace();
//            shutdown();
//        }
    }

    static File getConfigurationFile() {
        File file = System.getProperty("config") == null ? new File("main.endpoint") : new File(System.getProperty("config"));
        if(!file.exists())
            throw new PropertyError("Configuration file not found! File: " + file.getAbsolutePath());
        if(file.isDirectory())
            throw new PropertyError("Configuration file is a folder! File: " + file.getAbsolutePath());
        if(!file.canRead())
            throw new PropertyError("Can't read configuration file! File: " + file.getAbsolutePath());
        if(!file.canExecute())
            throw new PropertyError("Can't execute configuration file! File: " + file.getAbsolutePath());
        return file;
    }

    static File getCoreModulesFolder() {
        File file = System.getProperty("core-modules-folder") == null ? new File("core-modules/") : new File(System.getProperty("core-modules-folder"));
        if(file.isFile())
            throw new PropertyError("Core modules folder can't be a file!");
        return file;
    }

    static ConfigurationParser getConfigurationParser(File config, FileReader reader) {
        Class<?> clazz = ConfigurationParser.class;
        if(System.getProperty("parser") != null) {
            clazz = coreModuleManager.getClass(System.getProperty("parser"));
            if(clazz == null)
                throw new PropertyError("Parser " + System.getProperty("parser") + " not found!");
        }

        try {
            Constructor constructor = clazz.getConstructor(File.class, FileReader.class);
            constructor.setAccessible(true);
            return (ConfigurationParser) constructor.newInstance(config, reader);
        } catch (NoSuchMethodException e) {
            throw new PropertyError("Parser " + clazz.getCanonicalName() + " doesn't have (File, FileReader) constructor");
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new PropertyError(e);
        }
    }

    private static void startConsoleInput() throws UnsupportedEncodingException {
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
    }

    private static void preMain() {
        ServerInfo.setProvider(new ServerInfoProvider());
    }

    private static void shutdownInternal() {
//        Logger.log("Stopping...");
//        configurator.shutdown();
//        Agent.shutdown();
//        Logger.shutdown();
    }

    public synchronized static void shutdown() {
//        Logger.log("Stopping...");
//        configurator.shutdown();
//        Agent.shutdown();
//        Logger.shutdown();
//        System.exit(0);
    }

    public synchronized static void shutdownNow() {
//        Logger.log("Emergency stopping...");
//        configurator.shutdownNow();
//        Agent.shutdown();
//        Logger.shutdown();
//        System.exit(0);
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