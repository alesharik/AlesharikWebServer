package com.alesharik.webserver.logger;

import com.alesharik.webserver.logger.configuration.LoggerConfiguration;
import com.alesharik.webserver.logger.configuration.LoggerConfigurationBasePackage;
import com.alesharik.webserver.logger.configuration.LoggerConfigurationPrefix;
import com.alesharik.webserver.logger.handlers.InfoConsoleHandler;
import com.alesharik.webserver.logger.handlers.WarningConsoleHandler;
import lombok.SneakyThrows;
import sun.misc.SharedSecrets;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.stream.Stream;

/**
 * This class used for log messages in AlesharikWebServer.
 * Configuration:
 * Base package: *.
 */
@Prefix("[LOGGER]")
public class Logger {
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger("AlesharikWebServerMainLogger");
    private static boolean isConfigured = false;

    /**
     * Class : Prefix
     */
    private static final HashMap<String, String> configuredPrefixes = new HashMap<>();

    /**
     * WARNING! DON'T WORKS IN JDK 9!<br>
     * Use {@link sun.misc.SharedSecrets} for get {@link StackTraceElement}
     *
     * @param i number of {@link StackTraceElement}
     * @return [ + file name + : + line number + ]
     */
    private static String getPrefixLocation(int i) {
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
        String prefix = getPrefixFromClass(CallingClass.INSTANCE.getCallingClasses()[2]);
        if(prefix.isEmpty()) {
            log(getPrefixLocation(2), message);
        } else {
            log(message, prefix);
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

                loadConfigurations();
                log("Logger successfully setup!");
                isConfigured = true;
            } catch (SecurityException | IOException e) {
                e.printStackTrace(System.out);
            }
        }
    }

    @SneakyThrows
    private static void loadConfigurations() {
        Class<?> classLoader = ClassLoader.getSystemClassLoader().getClass();
        Field fldClasses = classLoader.getDeclaredField("classes");
        fldClasses.setAccessible(true);
        Vector<Class<?>> classes = (Vector<Class<?>>) fldClasses.get(ClassLoader.getSystemClassLoader());
        classes.stream().filter(aClass -> aClass.isAnnotationPresent(LoggerConfiguration.class)).forEach(Logger::parseConfigurationClass);
    }

    @SneakyThrows
    private static void parseConfigurationClass(Class<?> clazz) {
        Object configurationInstance = clazz.newInstance();

        if(clazz.isInterface() || clazz.isEnum() || clazz.isAnnotation()) {
            log("Can't use class " + clazz.getCanonicalName() + " as configuration! This class must not be interface, enum or annotation!");
        }
        String basePackage = "";
        for(Field field : clazz.getFields()) {
            field.setAccessible(true);

            LoggerConfigurationPrefix configurationPrefix = field.getAnnotation(LoggerConfigurationPrefix.class);
            if(configurationPrefix != null && field.getType().isAssignableFrom(String.class)) {
                String value = configurationPrefix.value();
                if(value.isEmpty()) {
                    Logger.log("Class " + clazz.getCanonicalName() + " has annotation on field field: " + field.getName() + ". The value of annotation must not be empty!");
                }
                String configurationValueWithBasePackage = parseConfigurationValueWithBasePackage((String) field.get(configurationInstance), basePackage);
//                if(!configurationValueWithBasePackage.endsWith(".class"))
                configuredPrefixes.put(value, configurationValueWithBasePackage);
                continue;
            }

            LoggerConfigurationBasePackage basePackageAnnotation = field.getAnnotation(LoggerConfigurationBasePackage.class);
            if(basePackageAnnotation != null && field.getType().isAssignableFrom(String.class)) {
                if(basePackage.isEmpty()) {
                    basePackage = (String) field.get(configurationInstance);
                } else {
                    Logger.log("Class " + clazz.getCanonicalName() + " have more than 1 base package annotation!");
                }
            }
        }
    }

    private static String parseConfigurationValueWithBasePackage(String fieldValue, String basePackage) {
        if(basePackage == null || basePackage.isEmpty()) {
            return fieldValue;
        }

        if(fieldValue.startsWith("*.")) {
            return fieldValue.replace("*.", basePackage + ".");
        } else {
            return fieldValue;
        }
    }

    private static String getPrefixFromClass(Class<?> clazz) {
        String configuredPrefixes = getConfiguredPrefixes(clazz);
        Prefixes prefixes = clazz.getAnnotation(Prefixes.class);
        if(prefixes != null) {
            return Stream.of(prefixes.value()).reduce(configuredPrefixes, String::concat);
        }

        Prefix prefix = clazz.getAnnotation(Prefix.class);
        if(prefix != null) {
            return configuredPrefixes.concat(prefix.value());
        } else {
            return "";
        }
    }

    private static String getConfiguredPrefixes(Class<?> clazz) {
        log(clazz.getCanonicalName());
        return "";
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

