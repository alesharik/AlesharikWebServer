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
import com.alesharik.webserver.base.exception.DevError;
import com.alesharik.webserver.configuration.ApiHelper;
import com.alesharik.webserver.configuration.config.lang.ApiEndpointSection;
import com.alesharik.webserver.configuration.config.lang.ConfigurationEndpoint;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationElement;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationObject;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationObjectArray;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationPrimitive;
import com.alesharik.webserver.configuration.config.lang.parser.ConfigurationParser;
import com.alesharik.webserver.configuration.config.lang.parser.FileReader;
import com.alesharik.webserver.configuration.config.lang.parser.ParserException;
import com.alesharik.webserver.exception.error.UnexpectedBehaviorError;
import com.alesharik.webserver.extension.module.ConfigurationError;
import com.alesharik.webserver.internals.InternalHackingError;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefixes;
import com.alesharik.webserver.main.console.ConsoleCommand;
import com.alesharik.webserver.main.console.ConsoleCommandManager;
import com.alesharik.webserver.main.script.ScriptEngineImpl;
import lombok.Getter;

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
    @Getter
    static CoreModuleManagerImpl coreModuleManager;
    @Getter
    static ScriptEngineImpl scriptEngine;
    @Getter
    static ConfigurationRunner runner;

    static volatile boolean isRunning = true;

    private static short detach = 0; //bitset: 1 bit - in, 2 bit - out
    private static boolean testing = false;

    public static void main(String[] args) {
        for(String arg : args) {
            if(arg.startsWith("-detach")) {
                String a = arg.substring("-detach".length());
                if(!a.startsWith("="))
                    throw new PropertyError("-detach must have an argument (=[in|out|all])!");
                String cmd = a.substring(1);
                switch (cmd) {
                    case "in":
                        detach = 1;
                        break;
                    case "out":
                        detach = 2;
                        break;
                    case "all":
                        detach = 3;
                        break;
                    default:
                        throw new PropertyError("Unrecognized -detach option " + cmd);
                }
            } else if(arg.equals("testing"))
                testing = true;
        }

        try {
            mainImpl();
        } catch (CommandRedefinitionError e) {
            e.printStackTrace();
            System.err.println("Server misconfigured! Stopping...");
            shutdownNow();
        } catch (DevError e) {
            e.printStackTrace();
            System.err.println("Development error! Stopping...");
            shutdownNow();
        } catch (ParserException e) {
            e.printStackTrace();
            System.err.println("Parser exception occurs! Stopping...");
            shutdownNow();
        } catch (ConfigurationError e) {//TODO fix
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

    private static void mainImpl() throws IOException, InterruptedException {
        Logger.setupTemporary();
        ExecutionStage.enable();
        ExecutionStage.setState(ExecutionStage.PRE_LOAD);
        preMain();

        ExecutionStage.setState(ExecutionStage.CORE_MODULES);
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

        ExecutionStage.setState(ExecutionStage.CONFIG);

        File configuration = getConfigurationFile().getAbsoluteFile();
        ConfigurationParser parser = getConfigurationParser(configuration, FILE_READER);
        ConfigurationEndpoint endpoint = parser.parse();

        scriptEngine = new ScriptEngineImpl(FILE_READER, endpoint);
        scriptEngine.prepare();

        boolean reloadConfig;

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
        }

        runner = new ConfigurationRunner(configuration, parser, reloadConfig, endpoint);
        runner.start();
        runner.waitForCompletion();

        System.out.println("Server successfully loaded!");

        if((detach & 2) == 2) {
            System.out.println("Logger detach requested! Executing...");
            Logger.detach();
        }

        if((detach & 1) != 1) {
            System.out.println("Console disabled: detach requested");
            startConsoleInput();
        }
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
        while(isRunning) {
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
        if(!testing) {
            ServerInfo.setProvider(new ServerInfoProvider());
            Runtime.getRuntime().addShutdownHook(new ShutdownThread());
        } else {
            try {
                ServerInfo.setProvider(new ServerInfoProvider());
                Runtime.getRuntime().addShutdownHook(new ShutdownThread());
            } catch (RuntimeException ignored) {
            }
        }
    }

    private synchronized static void shutdownInternal() {
        runner.shutdown();
        if(testing)
            Logger.reset();
        else
            ApiHelper.shutdownLogger();
        if(!testing)
            Agent.shutdown();
    }

    public static void shutdown() {
        System.out.println("shutdown method invoked");
        System.out.println("Stopping...");
        shutdownInternal();
    }

    public synchronized static void shutdownNow() {
        System.out.println("Emergency stopping...");
        runner.shutdownNow();
        if(testing)
            Logger.reset();
        else
            ApiHelper.shutdownLogger();
        if(!testing) {
            Agent.shutdown();
            System.exit(1);
        }
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
            System.out.println("Signal received: stop");
            System.out.println("Stopping...");
            shutdownInternal();
        }
    }
}