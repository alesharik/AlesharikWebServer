package com.alesharik.webserver.logger;

import com.alesharik.webserver.logger.configuration.LoggerConfiguration;
import com.alesharik.webserver.logger.configuration.LoggerConfigurationBasePackage;
import com.alesharik.webserver.logger.configuration.LoggerConfigurationPrefix;
import com.alesharik.webserver.logger.handlers.InfoConsoleHandler;
import com.alesharik.webserver.logger.handlers.WarningConsoleHandler;
import lombok.SneakyThrows;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jctools.queues.atomic.MpscAtomicArrayQueue;
import sun.misc.SharedSecrets;

import javax.annotation.concurrent.ThreadSafe;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.stream.Stream;

/**
 * This class used for log messages in AlesharikWebServer.<br>
 * You can set prefix by:<ul>
 * <li>{@link Prefix} - used for setup one prefix</li>
 * <li>{@link Prefixes} - used for setup multiple prefixes</li>
 * <li>Configuration - used for setup prefixes</li>
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
public final class Logger {
    static final HashMap<String, WeakReference<NamedLogger>> loggers = new HashMap<>();
    /**
     * Default Java System.out
     */
    private static final PrintStream SYSTEM_OUT = System.out;
    /**
     * Default Java System.err
     */
    private static final PrintStream SYSTEM_ERR = System.err;

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger("AlesharikWebServerMainLogger");
    /**
     * Class : Prefixes
     */
    private static final HashMap<String, ArrayList<String>> configuredPrefixes = new HashMap<>();
    private static final ArrayList<WeakReference<ClassLoader>> classLoaders = new ArrayList<>();
    private static AtomicBoolean isConfigured = new AtomicBoolean(false);
    private static LoggerListenerThread listenerThread;

    static {
        LOGGER.setLevel(Level.ALL);
    }

    /**
     * WARNING! DON'T WORKS IN JDK 9!<br>
     * Use {@link sun.misc.SharedSecrets} for getIpForMicroservice {@link StackTraceElement}
     *
     * @param i number of {@link StackTraceElement}
     * @return [ + file name + : + line number + ]
     */
    static String getPrefixLocation(int i) {
        StackTraceElement element = SharedSecrets.getJavaLangAccess().getStackTraceElement(new Exception(), i);
        return "[" + element.getFileName() + ":" + element.getLineNumber() + "]";
    }

    private static void log(String message, int depth) {
        String prefix = getPrefixFromClass(CallingClass.INSTANCE.getCallingClasses()[depth]);
        if(prefix.isEmpty()) {
            log(getPrefixLocation(depth), message);
        } else {
            log(prefix, message);
        }
    }

    public static void log(String message) {
        log(message, 2);
    }

    public static void log(Throwable throwable) {
        String prefix = getPrefixFromClass(CallingClass.INSTANCE.getCallingClasses()[2]);
        if(prefix.isEmpty()) {
            log(getPrefixLocation(2), throwable);
        } else {
            log(prefix + '[' + getPrefixLocation(2) + ']', throwable);
        }
    }

    //WARNING! Don't works in JDK9
    public static void log(String message, TextFormatter textFormatter) {
        String prefix = getPrefixFromClass(CallingClass.INSTANCE.getCallingClasses()[2]);
        if(prefix.isEmpty()) {
            log(getPrefixLocation(2), message, textFormatter);
        } else {
            log(prefix, message, textFormatter);
        }
    }

    public static void log(Throwable throwable, TextFormatter textFormatter) {
        String prefix = getPrefixFromClass(CallingClass.INSTANCE.getCallingClasses()[2]);
        if(prefix.isEmpty()) {
            log(getPrefixLocation(2), throwable, textFormatter);
        } else {
            log(prefix + '[' + getPrefixLocation(2) + ']', throwable, textFormatter);
        }
    }

    public static void log(String message, String... prefixes) {
        log(String.join("", prefixes), message);
    }

    public static void log(Throwable throwable, String... prefixes) {
        log(String.join("", prefixes), throwable);
    }

    public static void log(String message, TextFormatter textFormatter, String... prefixes) {
        log(String.join("", prefixes), message, textFormatter);
    }

    public static void log(Throwable throwable, TextFormatter textFormatter, String... prefixes) {
        log(String.join("", prefixes), throwable, textFormatter);
    }

    public static void log(String prefix, String message) {
        checkState();

        LOGGER.log(Level.INFO, prefix + ": " + message);
        if(listenerThread != null) {
            listenerThread.sendMessage(new LoggerListenerThread.Message(prefix, message));
        }
    }

    public static void log(String prefix, Throwable throwable) {
        checkState();

//        throwable.printStackTrace();
        LOGGER.log(Level.WARNING, prefix + ": " + throwable.toString());
        if(listenerThread != null)
            listenerThread.sendMessage(new LoggerListenerThread.Message(prefix, throwable.toString()));
        Arrays.asList(throwable.getStackTrace()).forEach(stackTraceElement -> {
            LOGGER.log(Level.WARNING, prefix + ": " + stackTraceElement.toString());
            if(listenerThread != null) {
                listenerThread.sendMessage(new LoggerListenerThread.Message(prefix, stackTraceElement.toString()));
            }
        });
    }

    public static void log(String prefix, String message, TextFormatter textFormatter) {
        if(textFormatter.wholeMessage) {
            LOGGER.log(Level.INFO, textFormatter.format(prefix + ": " + message));
        } else {
            LOGGER.log(Level.INFO, prefix + ": " + textFormatter.format(message));
        }

        if(listenerThread != null) {
            listenerThread.sendMessage(new LoggerListenerThread.Message(prefix, message));
        }
    }

    public static void log(String prefix, Throwable throwable, TextFormatter textFormatter) {
        throwable.printStackTrace();
        if(textFormatter.wholeMessage) {
            LOGGER.log(Level.WARNING, textFormatter.format(prefix + ": " + throwable.toString()));
        } else {
            LOGGER.log(Level.WARNING, prefix + ": " + textFormatter.format(throwable.toString()));
        }
        if(listenerThread != null)
            listenerThread.sendMessage(new LoggerListenerThread.Message(prefix, throwable.toString()));
        Arrays.asList(throwable.getStackTrace()).forEach(stackTraceElement -> {
            LOGGER.log(Level.WARNING, prefix + ": " + stackTraceElement.toString());
            if(listenerThread != null) {
                listenerThread.sendMessage(new LoggerListenerThread.Message(prefix, stackTraceElement.toString()));
            }
        });
    }

    public static void setupLogger(File log, int listenerQueueCapacity) {
        if(!isConfigured.get()) {
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

                listenerThread = new LoggerListenerThread(listenerQueueCapacity);
                listenerThread.start();

                isConfigured.set(true);
                log("Logger successfully setup!");

                System.setOut(new LoggerPrintStream(SYSTEM_OUT));
                System.setErr(new LoggerErrorPrintStream(SYSTEM_ERR));
            } catch (SecurityException | IOException e) {
                e.printStackTrace(System.out);
            }
        } else {
            Logger.log("Oops! Someone try to reconfigure logger! ");
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

    public static void addListener(LoggerListener loggerListener) {
        listenerThread.addListener(loggerListener);
    }

    public static void removeListener(LoggerListener loggerListener) {
        listenerThread.removeListener(loggerListener);
    }

    @SneakyThrows
    //TODO write asm loader
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

    //    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings("UC_USELESS_OBJECT")
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
                    File[] files = dir.listFiles();
                    if(files == null) {
                        continue;
                    }
                    for(File file : files) {
                        findClassesFromFile(file, configurationValueWithBasePackage, classLoader)
                                .forEach(aClass -> addPrefix(aClass.getCanonicalName(), prefix));
                    }
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
        if(classLoaders.stream().noneMatch(classLoaderWeakReference -> classLoader.equals(classLoaderWeakReference.get()))) {
            loadConfigurations(classLoader);
        }
        if(configuredPrefixes.containsKey(clazz.getCanonicalName())) {
            return configuredPrefixes.get(clazz.getCanonicalName()).stream().reduce("", String::concat);
        } else {
            return "";
        }
    }

    /**
     * Check logger state
     *
     * @throws LoggerNotConfiguredException if logger not configured
     */
    private static void checkState() throws LoggerNotConfiguredException {
        if(!isConfigured.get()) {
            throw new LoggerNotConfiguredException();
        }
    }

    public static PrintStream getSystemOut() {
        return SYSTEM_OUT;
    }

    public static PrintStream getSystemErr() {
        return SYSTEM_ERR;
    }

    static void disable() {
        LOGGER.setLevel(Level.OFF);
        listenerThread.disable();
    }

    /**
     * Enum of foreground colors
     */
    public enum ForegroundColor {
        BLACK(30),
        RED(31),
        GREEN(32),
        YELLOW(33),
        BLUE(34),
        MAGENTA(35),
        CYAN(36),
        WHITE(37),
        /**
         * Default terminal color
         */
        NONE(0);

        private final int code;

        ForegroundColor(int code) {
            this.code = code;
        }

        /**
         * Color code
         */
        public int getCode() {
            return code;
        }

        @Override
        public String toString() {
            return "ForegroundColor{" +
                    "code=" + code +
                    '}';
        }
    }

    /**
     * Enum of background colors
     */
    public enum BackgroundColor {
        BLACK(40),
        RED(41),
        GREEN(42),
        YELLOW(43),
        BLUE(44),
        MAGENTA(45),
        CYAN(46),
        WHITE(47),
        /**
         * Default terminal color
         */
        NONE(0);

        private final int code;

        BackgroundColor(int code) {
            this.code = code;
        }

        /**
         * Color code
         */
        public int getCode() {
            return code;
        }

        @Override
        public String toString() {
            return "BackgroundColor{" +
                    "code=" + code +
                    '}';
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

    private static final class LoggerListenerThread extends Thread {
        private final MpscAtomicArrayQueue<Message> messages;
        private final CopyOnWriteArrayList<LoggerListener> loggerListeners;

        private final AtomicBoolean disabled = new AtomicBoolean(false);

        public LoggerListenerThread(int listenerQueueCapacity) {
            messages = new MpscAtomicArrayQueue<>(listenerQueueCapacity);
            loggerListeners = new CopyOnWriteArrayList<>();
            setName("LoggerListenerThread");
            setDaemon(true);
        }

        public void disable() {
            disabled.set(true);
        }

        public void addListener(LoggerListener loggerListener) {
            loggerListeners.add(loggerListener);
        }

        public void removeListener(LoggerListener loggerListener) {
            loggerListeners.remove(loggerListener);
        }

        public void sendMessage(Message message) {
            if(disabled.get()) {
                return;
            }
            messages.add(message);
        }

        public void run() {
            while(true) {
                Message message = messages.poll();
                if(message != null) {
                    loggerListeners.forEach(loggerListener -> loggerListener.listen(message.getPrefixes(), message.getMessage()));
                } else {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public static final class Message {
            private final String message;
            private final String prefixes;

            public Message(String prefixes, String message) {
                this.message = message;
                this.prefixes = prefixes;
            }

            public String getMessage() {
                return message;
            }

            public String getPrefixes() {
                return prefixes;
            }

            @Override
            public boolean equals(Object o) {
                if(this == o) return true;
                if(!(o instanceof Message)) return false;

                Message message1 = (Message) o;

                if(getMessage() != null ? !getMessage().equals(message1.getMessage()) : message1.getMessage() != null)
                    return false;
                return getPrefixes() != null ? getPrefixes().equals(message1.getPrefixes()) : message1.getPrefixes() == null;
            }

            @Override
            public int hashCode() {
                int result = getMessage() != null ? getMessage().hashCode() : 0;
                result = 31 * result + (getPrefixes() != null ? getPrefixes().hashCode() : 0);
                return result;
            }

            @Override
            public String toString() {
                return "Message{" +
                        "message='" + message + '\'' +
                        ", prefixes='" + prefixes + '\'' +
                        '}';
            }
        }
    }

    /**
     * TextFormatter add color to given text
     */
    public static class TextFormatter {
        protected static final String PREFIX = "\033[";
        protected static final String SEPARATOR = ";";
        protected static final String POSTFIX = "m";

        protected final ForegroundColor foregroundColor;
        protected final BackgroundColor backgroundColor;
        protected final boolean wholeMessage;

        /**
         * @param wholeMessage if true color all message with prefix and etc, overwise color only user message
         */
        public TextFormatter(ForegroundColor foregroundColor, BackgroundColor backgroundColor, boolean wholeMessage) {
            this.foregroundColor = foregroundColor;
            this.backgroundColor = backgroundColor;
            this.wholeMessage = wholeMessage;
        }

        /**
         * Add color(background and foreground) to string
         *
         * @param str the string
         * @return colored string
         */
        public String format(String str) {
            return PREFIX +
                    foregroundColor.getCode() +
                    SEPARATOR +
                    backgroundColor.getCode() +
                    POSTFIX +
                    str +
                    PREFIX +
                    ForegroundColor.WHITE.code +
                    SEPARATOR +
                    BackgroundColor.NONE.code +
                    POSTFIX;
        }

        public ForegroundColor getForegroundColor() {
            return foregroundColor;
        }

        public BackgroundColor getBackgroundColor() {
            return backgroundColor;
        }

        public boolean isWholeMessage() {
            return wholeMessage;
        }
    }

    /**
     * Override <code>System#out</code>. Stream has auto flush. Buffer is thread local
     */
    @ThreadSafe
    private static final class LoggerPrintStream extends PrintStream {
        private static final int NEW_LINE_INDEX = '\n';

        /**
         * Close bit
         */
        private static final int CLOSE = 1;
        /**
         * Error bit
         */
        private static final int ERROR = 2;

        private final ThreadLocal<String> lineBuffer;
        private final AtomicInteger state;

        public LoggerPrintStream(OutputStream out) {
            super(out);
            state = new AtomicInteger(0);
            lineBuffer = new ThreadLocal<>();
            lineBuffer.set("");
        }

        @Override
        public void flush() {
            Logger.log(lineBuffer.get());
            lineBuffer.set("");
        }

        @Override
        public void close() {
            int v;
            do {
                v = state.get();
            } while(!state.compareAndSet(v, v | CLOSE));
        }

        @Override
        public boolean checkError() {
            return (state.get() & ERROR) == ERROR;
        }

        @Override
        protected void setError() {
            int v;
            do {
                v = state.get();
            } while(!state.compareAndSet(v, v | ERROR));
        }

        @Override
        protected void clearError() {
            int v;
            do {
                v = state.get();
            } while(!state.compareAndSet(v, v & ~ERROR));
        }

        @Override
        public void write(int b) {
            if(b == NEW_LINE_INDEX) {
                Logger.log(lineBuffer.get(), 3);
                lineBuffer.set("");
            } else {
                lineBuffer.set(lineBuffer.get().concat(Character.toString((char) b)));
            }
        }

        @Override
        public void write(byte[] buf, int off, int len) {
            byte[] b = new byte[len];
            System.arraycopy(buf, off, b, 0, len);
            lineBuffer.set(lineBuffer.get().concat(new String(b, Charset.defaultCharset())));
            flush();
        }

        @Override
        public void print(boolean b) {
            lineBuffer.set(lineBuffer.get().concat(Boolean.toString(b)));
        }

        @Override
        public void print(char c) {
            lineBuffer.set(lineBuffer.get().concat(Character.toString(c)));
        }

        @Override
        public void print(int i) {
            lineBuffer.set(lineBuffer.get().concat(Integer.toString(i)));
        }

        @Override
        public void print(long l) {
            lineBuffer.set(lineBuffer.get().concat(Long.toString(l)));
        }

        @Override
        public void print(float f) {
            lineBuffer.set(lineBuffer.get().concat(Float.toString(f)));
        }

        @Override
        public void print(double d) {
            lineBuffer.set(lineBuffer.get().concat(Double.toString(d)));
        }

        @Override
        public void print(char[] arr) {
            lineBuffer.set(lineBuffer.get().concat(new String(arr)));
        }

        @Override
        public void print(String str) {
            lineBuffer.set(lineBuffer.get().concat(str));
        }

        @Override
        public void print(Object obj) {
            lineBuffer.set(lineBuffer.get().concat(obj.toString()));
        }

        @Override
        public void println() {
            flush();
        }

        @Override
        public void println(boolean x) {
            print(x);
            flush();
        }

        @Override
        public void println(char x) {
            print(x);
            flush();
        }

        @Override
        public void println(int x) {
            print(x);
            flush();
        }

        @Override
        public void println(long x) {
            print(x);
            flush();
        }

        @Override
        public void println(float x) {
            print(x);
            flush();
        }

        @Override
        public void println(double x) {
            print(x);
            flush();
        }

        @Override
        public void println(char[] x) {
            print(x);
            flush();
        }

        @Override
        public void println(String x) {
            print(x);
            flush();
        }

        @Override
        public void println(Object x) {
            print(x);
            flush();
        }

        @Override
        public PrintStream printf(String format, Object... args) {
            print(String.format(format, args));
            return this;
        }

        @Override
        public PrintStream printf(Locale l, String format, Object... args) {
            print(String.format(l, format, args));
            return this;
        }

        @Override
        public PrintStream format(String format, Object... args) {
            return printf(format, args);
        }

        @Override
        public PrintStream format(Locale l, String format, Object... args) {
            return printf(l, format, args);
        }

        @Override
        public PrintStream append(CharSequence csq) {
            print(csq.toString());
            return this;
        }

        @Override
        public PrintStream append(CharSequence csq, int start, int end) {
            print(csq.subSequence(start, end).toString());
            return this;
        }

        @Override
        public PrintStream append(char c) {
            print(c);
            return this;
        }

        @Override
        public void write(byte[] b) throws IOException {
            write(b, 0, b.length);
        }

    }

    /**
     * Override <code>System#err</code>. Stream has auto flush. Buffer is thread local. All messages are {@link ForegroundColor#RED} on {@link BackgroundColor#NONE} and format all message
     * without date
     */
    @ThreadSafe
    private static final class LoggerErrorPrintStream extends PrintStream {
        private static final TextFormatter FORMATTER = new TextFormatter(ForegroundColor.RED, BackgroundColor.NONE, true);
        private static final int NEW_LINE_INDEX = '\n';

        /**
         * Close bit
         */
        private static final int CLOSE = 1;
        /**
         * Error bit
         */
        private static final int ERROR = 2;

        private final ThreadLocal<String> lineBuffer;
        private final AtomicInteger state;

        public LoggerErrorPrintStream(OutputStream out) {
            super(out);
            state = new AtomicInteger(0);
            lineBuffer = new ThreadLocal<>();
            lineBuffer.set("");
        }

        @Override
        public void flush() {
            Logger.log(lineBuffer.get(), FORMATTER);
            lineBuffer.set("");
        }

        @Override
        public void close() {
            int v;
            do {
                v = state.get();
            } while(!state.compareAndSet(v, v | CLOSE));
        }

        @Override
        public boolean checkError() {
            return (state.get() & ERROR) == ERROR;
        }

        @Override
        protected void setError() {
            int v;
            do {
                v = state.get();
            } while(!state.compareAndSet(v, v | ERROR));
        }

        @Override
        protected void clearError() {
            int v;
            do {
                v = state.get();
            } while(!state.compareAndSet(v, v & ~ERROR));
        }

        @Override
        public void write(int b) {
            if(b == NEW_LINE_INDEX) {
                Logger.log(lineBuffer.get(), 3);
                lineBuffer.set("");
            } else {
                lineBuffer.set(lineBuffer.get().concat(Character.toString((char) b)));
            }
        }

        @Override
        public void write(byte[] buf, int off, int len) {
            byte[] b = new byte[len];
            System.arraycopy(buf, off, b, 0, len);
            lineBuffer.set(lineBuffer.get().concat(new String(b, Charset.defaultCharset())));
            flush();
        }

        @Override
        public void print(boolean b) {
            lineBuffer.set(lineBuffer.get().concat(Boolean.toString(b)));
        }

        @Override
        public void print(char c) {
            lineBuffer.set(lineBuffer.get().concat(Character.toString(c)));
        }

        @Override
        public void print(int i) {
            lineBuffer.set(lineBuffer.get().concat(Integer.toString(i)));
        }

        @Override
        public void print(long l) {
            lineBuffer.set(lineBuffer.get().concat(Long.toString(l)));
        }

        @Override
        public void print(float f) {
            lineBuffer.set(lineBuffer.get().concat(Float.toString(f)));
        }

        @Override
        public void print(double d) {
            lineBuffer.set(lineBuffer.get().concat(Double.toString(d)));
        }

        @Override
        public void print(char[] arr) {
            lineBuffer.set(lineBuffer.get().concat(new String(arr)));
        }

        @Override
        public void print(String str) {
            lineBuffer.set(lineBuffer.get().concat(str));
        }

        @Override
        public void print(Object obj) {
            lineBuffer.set(lineBuffer.get().concat(obj.toString()));
        }

        @Override
        public void println() {
            flush();
        }

        @Override
        public void println(boolean x) {
            print(x);
            flush();
        }

        @Override
        public void println(char x) {
            print(x);
            flush();
        }

        @Override
        public void println(int x) {
            print(x);
            flush();
        }

        @Override
        public void println(long x) {
            print(x);
            flush();
        }

        @Override
        public void println(float x) {
            print(x);
            flush();
        }

        @Override
        public void println(double x) {
            print(x);
            flush();
        }

        @Override
        public void println(char[] x) {
            print(x);
            flush();
        }

        @Override
        public void println(String x) {
            print(x);
            flush();
        }

        @Override
        public void println(Object x) {
            print(x);
            flush();
        }

        @Override
        public PrintStream printf(String format, Object... args) {
            print(String.format(format, args));
            return this;
        }

        @Override
        public PrintStream printf(Locale l, String format, Object... args) {
            print(String.format(l, format, args));
            return this;
        }

        @Override
        public PrintStream format(String format, Object... args) {
            return printf(format, args);
        }

        @Override
        public PrintStream format(Locale l, String format, Object... args) {
            return printf(l, format, args);
        }

        @Override
        public PrintStream append(CharSequence csq) {
            print(csq.toString());
            return this;
        }

        @Override
        public PrintStream append(CharSequence csq, int start, int end) {
            print(csq.subSequence(start, end).toString());
            return this;
        }

        @Override
        public PrintStream append(char c) {
            print(c);
            return this;
        }

        @Override
        public void write(byte[] b) throws IOException {
            write(b, 0, b.length);
        }

    }
}

