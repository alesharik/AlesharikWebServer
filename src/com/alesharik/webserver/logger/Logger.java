package com.alesharik.webserver.logger;

import com.alesharik.webserver.logger.handlers.InfoConsoleHandler;
import com.alesharik.webserver.logger.handlers.WarningConsoleHandler;
import com.alesharik.webserver.main.Main;
import sun.misc.SharedSecrets;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.FileHandler;
import java.util.logging.Level;

/**
 * This class used for log messages in AlesharikWebServer
 */
@Prefix("[LOGGER]")
public class Logger {
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger("AlesharikWebServerMainLogger");
    private static boolean isConfigured = false;

    /**
     * WARNING! DON'T WORKS IN JDK 9!<br>
     * Use {@link sun.misc.SharedSecrets} for get {@link StackTraceElement}
     *
     * @param i number of {@link StackTraceElement}
     * @return [ + file name + : + line number + ]
     */
    public static String getPrefixLocation(int i) {
        StackTraceElement element = SharedSecrets.getJavaLangAccess().getStackTraceElement(new Exception(), i);
        return "[" + element.getFileName() + ":" + element.getLineNumber() + "]";
    }

    public static void log(String message, String... prefixes) {
        StringBuilder sb = new StringBuilder();
        Arrays.asList(prefixes).forEach(sb::append);
        log(sb.toString(), message);
    }

    public static void log(Throwable throwable, String... prefixes) {
        StringBuilder sb = new StringBuilder();
        Arrays.asList(prefixes).forEach(sb::append);
        log(sb.toString(), throwable);
    }

    public static void log(String prefix, String message) {
        LOGGER.log(Level.INFO, prefix + ": " + message);
    }

    public static void log(String prefix, Throwable throwable) {
        LOGGER.log(Level.WARNING, prefix + ": " + throwable.toString());
        Arrays.asList(throwable.getStackTrace()).forEach(stackTraceElement -> LOGGER.log(Level.WARNING, stackTraceElement.toString()));
    }

    //WARNING! Don't works in JDK9
    public static void log(String message) {
        String prefix = getPrefixFromClass(new CallingClass().getCallingClasses()[2]);
        if(prefix.isEmpty()) {
            log(getPrefixLocation(2), message);
        } else {
            log(prefix, message);
        }
    }

    public static void log(Throwable throwable) {
        log(getPrefixLocation(2), throwable);
    }

    public static void setupLogger(File log) {
        if(!isConfigured) {
            try {
                if(log.exists()) {
                    log = new File(log.getPath() + 1);
                }

                LoggerFormatter formatter = new LoggerFormatter();
                FileHandler fileHandler = new FileHandler(log.getPath());
                fileHandler.setFormatter(formatter);
                LOGGER.addHandler(fileHandler);
                LOGGER.addHandler(new InfoConsoleHandler());
                LOGGER.addHandler(new WarningConsoleHandler());

                log("Logger successfully setup!");
                isConfigured = true;
            } catch (SecurityException | IOException e) {
                e.printStackTrace(System.out);
                Main.shutdown();
            }
        }
    }

    private static String getPrefixFromClass(Class<?> clazz) {
        Prefix prefix = clazz.getAnnotation(Prefix.class);
        if(prefix != null) {
            return prefix.value();
        } else {
            return "";
        }
    }

    private static class CallingClass extends SecurityManager {
        public static final CallingClass INSTANCE = new CallingClass();

        private CallingClass() {
        }

        public Class[] getCallingClasses() {
            return getClassContext();
        }
    }
}

