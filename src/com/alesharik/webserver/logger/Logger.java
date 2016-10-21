package com.alesharik.webserver.logger;

import com.alesharik.webserver.logger.configuration.LoggerConfiguration;
import com.alesharik.webserver.logger.configuration.LoggerConfigurationBasePackage;
import com.alesharik.webserver.logger.configuration.LoggerConfigurationPrefix;
import com.alesharik.webserver.logger.handlers.InfoConsoleHandler;
import com.alesharik.webserver.logger.handlers.WarningConsoleHandler;
import lombok.SneakyThrows;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import sun.misc.SharedSecrets;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.stream.Stream;

/**
 * This class used for log messages in AlesharikWebServer.<br>
 * You can set prefix by:<ul>
 *     <li>{@link Prefix} - used for setup one prefix</li>
 *     <li>{@link Prefixes} - used for setup multiple prefixes</li>
 *     <li>Configuration - used for setup prefixes</li>
 * </ul>
 * Configuration:<br>
 * To create configuration you need to mark class with @{@link LoggerConfiguration}.
 * The class must have a public no-args constructor!<br>
 * The config value is public static final string, annotated with @{@link LoggerConfigurationPrefix}.<br>
 * The config can have a base package({@link LoggerConfigurationBasePackage}). This package need to not write it all the time.
 * In config must be 1 base package annotation! Base package symbol is *.:<br>
 * basePackage = "test"<br>
 * mainPackage = "*.main" = "test.main"<br>
 */
@Prefix("[LOGGER]")
public class Logger {
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger("AlesharikWebServerMainLogger");
    private static boolean isConfigured = false;

    /**
     * Class : Prefixes
     */
    private static final HashMap<String, ArrayList<String>> configuredPrefixes = new HashMap<>();
    private static final ArrayList<WeakReference<ClassLoader>> classLoaders = new ArrayList<>();
    static final HashMap<String, WeakReference<NamedLogger>> loggers = new HashMap<>();

    /**
     * WARNING! DON'T WORKS IN JDK 9!<br>
     * Use {@link sun.misc.SharedSecrets} for get {@link StackTraceElement}
     *
     * @param i number of {@link StackTraceElement}
     * @return [ + file name + : + line number + ]
     */
    static String getPrefixLocation(int i) {
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

                loadConfigurations(ClassLoader.getSystemClassLoader());
                log("Logger successfully setup!");
                isConfigured = true;
            } catch (SecurityException | IOException e) {
                e.printStackTrace(System.out);
            }
        }
    }

    public static NamedLogger createNewNamedLogger(String name, File file) {
        if(loggers.containsKey(name) && !loggers.get(name).isEnqueued()) {
            return loggers.get(name).get();
        } else {
            NamedLogger namedLogger = new NamedLogger(name, file);
            loggers.put(name, new WeakReference<>(namedLogger));
            return namedLogger;
        }
    }

    @SneakyThrows
    private static void loadConfigurations(ClassLoader classLoader) {
        Class<?> classLoaderClass = ClassLoader.class;
        Field fldClasses = classLoaderClass.getDeclaredField("classes");
        fldClasses.setAccessible(true);
        Vector<Class<?>> classes = (Vector<Class<?>>) fldClasses.get(classLoader);
        ((Vector<Class<?>>) classes.clone()).stream()
                .filter(aClass -> aClass.isAnnotationPresent(LoggerConfiguration.class))
                .forEach(Logger::parseConfiguration);
        classLoaders.add(new WeakReference<>(classLoader));
    }

    @SneakyThrows
    private static void parseConfiguration(Class<?> clazz) {
        if(clazz.isInterface() || clazz.isEnum() || clazz.isAnnotation()) {
            log("Can't use class " + clazz.getCanonicalName() + " as configuration! This class must not be interface, enum or annotation!");
            return;
        }
        try {
            parseConfigurationClass(clazz);
        } catch (InstantiationException e) {
            log("Can't create instance of class" + clazz.getCanonicalName() + "!");
        }
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings("UC_USELESS_OBJECT")
    private static void parseConfigurationClass(Class<?> clazz) throws InstantiationException, IllegalAccessException, NoSuchFieldException {
        Mutable<String> basePackage = new MutableObject<>("");

        for(Field field : clazz.getFields()) {
            field.setAccessible(true);

            LoggerConfigurationPrefix configurationPrefix = field.getAnnotation(LoggerConfigurationPrefix.class);
            String prefix = (String) field.get(clazz.newInstance());
            if(configurationPrefix != null && field.getType().isAssignableFrom(String.class)) {
                String value = configurationPrefix.value();
                if(value.isEmpty()) {
                    Logger.log("Class " + clazz.getCanonicalName() + " has annotation on field field: " + field.getName() + ". The value of annotation must not be empty!");
                }
                String configurationValueWithBasePackage = parseConfigurationValueWithBasePackage(configurationPrefix.value(), basePackage.getValue());
                ClassLoader classLoader = clazz.getClassLoader();
                try {
                    classLoader.loadClass(configurationValueWithBasePackage);
                    addPrefix(configurationValueWithBasePackage, prefix);
                } catch (ClassNotFoundException e) {
                    URL url = classLoader.getResource(configurationValueWithBasePackage.replace(".", "/"));
                    if(url == null) {
                        continue;
                    }
                    File dir = new File(url.getFile());
                    List<Class<?>> classes = new ArrayList<>();
                    File[] files = dir.listFiles();
                    if(files == null) {
                        continue;
                    }
                    for(File file : files) {
                        classes.addAll(findClassesFromFile(file, configurationValueWithBasePackage, classLoader));
                    }
                    classes.forEach(aClass -> addPrefix(aClass.getCanonicalName(), prefix));
                }
                continue;
            }

            LoggerConfigurationBasePackage basePackageAnnotation = field.getAnnotation(LoggerConfigurationBasePackage.class);
            if(basePackageAnnotation != null && field.getType().isAssignableFrom(String.class)) {
                if(basePackage.getValue().isEmpty()) {
                    basePackage.setValue(prefix);
                } else {
                    log("Class " + clazz.getCanonicalName() + " have more than 1 base package annotation!");
                }
            }
        }
    }

    @SneakyThrows
    private static List<Class<?>> findClassesFromFile(File file, String scannedPackage, ClassLoader classLoader) {
        List<Class<?>> classes = new ArrayList<>();

        String resource = scannedPackage + "." + file.getName();
        if(file.isDirectory()) {
            File[] files = file.listFiles();
            if(files != null) {
                for(File child : files) {
                    classes.addAll(findClassesFromFile(child, resource, classLoader));
                }
            }
        } else if(resource.endsWith(".class")) {
            int endIndex = resource.length() - ".class".length();
            String className = resource.substring(0, endIndex);
            classes.add(classLoader.loadClass(className));
        }
        return classes;
    }

    private static void addPrefix(String className, String prefix) {
        if(configuredPrefixes.containsKey(className)) {
            configuredPrefixes.get(className).add(prefix);
        } else {
            ArrayList<String> prefixes = new ArrayList<>();
            prefixes.add(prefix);
            configuredPrefixes.put(className, prefixes);
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
            return configuredPrefixes;
        }
    }

    private static String getConfiguredPrefixes(Class<?> clazz) {
        ClassLoader classLoader = clazz.getClassLoader();
        if(!classLoaders.stream().anyMatch(classLoaderWeakReference -> classLoader.equals(classLoaderWeakReference.get()))) {
            loadConfigurations(classLoader);
        }
        if(configuredPrefixes.containsKey(clazz.getCanonicalName())) {
            return configuredPrefixes.get(clazz.getCanonicalName()).stream().reduce("", String::concat);
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

