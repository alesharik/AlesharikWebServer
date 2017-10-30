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

package com.alesharik.webserver.logger;

import com.alesharik.webserver.api.collections.ConcurrentLiveHashMap;
import com.alesharik.webserver.api.mx.bean.MXBeanManager;
import com.alesharik.webserver.api.reflection.ParentPackageIterator;
import com.alesharik.webserver.api.statistics.FuzzyTimeCountStatistics;
import com.alesharik.webserver.api.statistics.TimeCountStatistics;
import com.alesharik.webserver.logger.level.LoggingLevel;
import com.alesharik.webserver.logger.level.LoggingLevelManager;
import com.alesharik.webserver.logger.level.LoggingLevelManagerMXBean;
import com.alesharik.webserver.logger.logger.FileLoggerHandler;
import com.alesharik.webserver.logger.logger.LoggerFormatter;
import com.alesharik.webserver.logger.logger.PrintStreamErrorManager;
import com.alesharik.webserver.logger.logger.PrintStreamLoggerHandler;
import com.alesharik.webserver.logger.mx.LoggerListenerThreadMXBean;
import com.alesharik.webserver.logger.mx.LoggerMXBean;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jctools.queues.MpscLinkedQueue;
import org.jctools.queues.MpscLinkedQueue7;
import org.jctools.queues.atomic.MpscAtomicArrayQueue;
import sun.misc.SharedSecrets;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * This class used for log messages in AlesharikWebServer.<br>
 * You can set prefix by:<ul>
 * <li>{@link Prefixes} - used for setup multiple prefixes</li>
 * <li>Configuration - used for setup prefixes for folders, files and etc</li>
 * </ul>
 */
@Prefixes("[LOGGER]")
public final class Logger {
    private static final LoggerMXBean bean = new LoggerMXBeanImpl();

    static final HashMap<String, WeakReference<NamedLogger>> loggers = new HashMap<>();
    /**
     * Default Java System.out
     */
    private static final PrintStream SYSTEM_OUT = System.out;
    /**
     * Default Java System.err
     */
    private static final PrintStream SYSTEM_ERR = System.err;

    private static final CopyOnWriteArrayList<Handler> loggerHandlers = new CopyOnWriteArrayList<>();
    private static final AtomicBoolean isConfigured = new AtomicBoolean(false);

    private static File logFile;
    private static LoggerListenerThread listenerThread;

    /**
     * message : caller : location prefix
     */
    private static MpscLinkedQueue<Message> messageQueue;
    private static final LoggerThread loggerThread = new LoggerThread();
    private static volatile boolean shutdown = false;

    @Getter
    private static final LoggingLevelManager loggingLevelManager = new LoggingLevelManagerImpl();

    public static void shutdown() {
        shutdown = true;
        loggerThread.interrupt();
        listenerThread.interrupt();
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

    /**
     * Add message to messageQueue. Do not throw {@link LoggerNotConfiguredException} and do not execute {@link #checkState()} method.
     * You need to execute it before call this method!
     *
     * @param message the message
     * @param depth   caller class stack depth
     */
    @SuppressWarnings("unchecked")
    static void logMessageUnsafe(String message, int depth, boolean isSout) {
        messageQueue.add(new Message("", message, CallingClass.INSTANCE.getCallingClasses()[depth + 1], getPrefixLocation(depth + 1), isSout));

        synchronized (loggerThread.synchronizerLock) {
            loggerThread.synchronizerLock.notifyAll();
        }
    }

    /**
     * Add message to messageQueue. Do not throw {@link LoggerNotConfiguredException} and do not execute {@link #checkState()} method.
     * You need to execute it before call this method!
     *
     * @param prefixes user-defined message prefixes
     * @param message  the message
     * @param depth    caller class stack depth
     */
    @SuppressWarnings("unchecked")
    static void logMessageUnsafe(String prefixes, String message, int depth, boolean isSout) {
        messageQueue.add(new Message(prefixes, message, CallingClass.INSTANCE.getCallingClasses()[depth + 1], getPrefixLocation(depth + 1), isSout));

        synchronized (loggerThread.synchronizerLock) {
            loggerThread.synchronizerLock.notifyAll();
        }
    }

    /**
     * Add message to messageQueue. Do not throw {@link LoggerNotConfiguredException} and do not execute {@link #checkState()} method.
     * You need to execute it before call this method!
     *
     * @param message the message
     * @param depth   caller class stack depth
     */
    @SuppressWarnings("unchecked")
    static void logMessageUnsafeDebug(String message, int depth) {
        messageQueue.add(new Message("", message, CallingClass.INSTANCE.getCallingClasses()[depth + 1], getPrefixLocation(depth + 1), false, true));

        synchronized (loggerThread.synchronizerLock) {
            loggerThread.synchronizerLock.notifyAll();
        }
    }

    /**
     * Add message to messageQueue. Do not throw {@link LoggerNotConfiguredException} and do not execute {@link #checkState()} method.
     * You need to execute it before call this method!
     *
     * @param prefixes user-defined message prefixes
     * @param message  the message
     * @param depth    caller class stack depth
     */
    @SuppressWarnings("unchecked")
    static void logMessageUnsafeDebug(String prefixes, String message, int depth) {
        messageQueue.add(new Message(prefixes, message, CallingClass.INSTANCE.getCallingClasses()[depth + 1], getPrefixLocation(depth + 1), false, true));

        synchronized (loggerThread.synchronizerLock) {
            loggerThread.synchronizerLock.notifyAll();
        }
    }


    /**
     * Add throwable to messageQueue
     *
     * @param throwable the throwable
     * @param depth     caller class stack depth
     * @throws LoggerNotConfiguredException if logger not already configured
     */
    static void logThrowable(Throwable throwable, int depth) {
        checkState();

        depth++;

        logMessageUnsafe(throwable.toString(), depth);
        for(StackTraceElement stackTraceElement : throwable.getStackTrace()) {
            logMessageUnsafe(stackTraceElement.toString(), depth);
        }
    }

    private static void logMessageUnsafe(String s, int depth) {
        logMessageUnsafe(s, depth, false);
    }

    /**
     * Add throwable to messageQueue
     *
     * @param prefixes  user-defined message prefixes
     * @param throwable the throwable
     * @param depth     caller class stack depth
     * @throws LoggerNotConfiguredException if logger not already configured
     */
    static void logThrowable(String prefixes, Throwable throwable, int depth) {
        checkState();

        depth++;

        logMessageUnsafe(prefixes, throwable.toString(), depth);
        for(StackTraceElement stackTraceElement : throwable.getStackTrace()) {
            logMessageUnsafe(prefixes, stackTraceElement.toString(), depth);
        }
    }

    private static void logMessageUnsafe(String prefixes, String s, int depth) {
        logMessageUnsafe(prefixes, s, depth, false);
    }

    /**
     * Add throwable to messageQueue
     *
     * @param throwable     the throwable
     * @param depth         caller class stack depth
     * @param textFormatter the text formatter
     * @throws LoggerNotConfiguredException if logger not already configured
     */
    static void logThrowable(Throwable throwable, int depth, TextFormatter textFormatter) {
        checkState();

        depth++;

        logMessageUnsafe(textFormatter.format(throwable.toString()), depth);
        for(StackTraceElement stackTraceElement : throwable.getStackTrace()) {
            logMessageUnsafe(textFormatter.format(stackTraceElement.toString()), depth);
        }
    }

    /**
     * Add throwable to messageQueue
     *
     * @param prefixes      user-defined message prefixes
     * @param throwable     the throwable
     * @param depth         caller class stack depth
     * @param textFormatter the text formatter
     * @throws LoggerNotConfiguredException if logger not already configured
     */
    static void logThrowable(Throwable throwable, int depth, TextFormatter textFormatter, String... prefixes) {
        checkState();

        depth++;
        String prefs = String.join("", prefixes);
        if(!prefs.isEmpty())
            prefs = textFormatter.format(prefs);
        logMessageUnsafe(prefs, textFormatter.format(throwable.toString()), depth);
        for(StackTraceElement stackTraceElement : throwable.getStackTrace()) {
            logMessageUnsafe(prefs, textFormatter.format(stackTraceElement.toString()), depth);
        }
    }

    /**
     * Add throwable to messageQueue
     *
     * @param prefixes      user-defined message prefixes
     * @param throwable     the throwable
     * @param depth         caller class stack depth
     * @param textFormatter the text formatter
     * @throws LoggerNotConfiguredException if logger not already configured
     */
    static void logThrowableDebug(Throwable throwable, int depth, TextFormatter textFormatter, String... prefixes) {
        checkState();

        depth++;
        String prefs = String.join("", prefixes);
        if(!prefs.isEmpty())
            prefs = textFormatter.format(prefs);
        logMessageUnsafeDebug(prefs, textFormatter.format(throwable.toString()), depth);
        for(StackTraceElement stackTraceElement : throwable.getStackTrace()) {
            logMessageUnsafeDebug(prefs, textFormatter.format(stackTraceElement.toString()), depth);
        }
    }

    public static void log(String message) {
        checkState();

        logMessageUnsafe(message, 2);
    }

    public static void log(Throwable throwable) {
        logThrowable(throwable, 2);
    }

    public static void log(@Nonnull String message, @Nonnull TextFormatter textFormatter) {
        checkState();

        logMessageUnsafe(textFormatter.format(message), 2);
    }

    private static void logFromStream(String message, TextFormatter textFormatter) {
        logMessageUnsafe(textFormatter.format(message), 5);
    }

    private static void logFromStream(String message, boolean isSout) {
        logMessageUnsafe(message, 5, isSout);
    }

    public static void log(@Nonnull Throwable throwable, @Nonnull TextFormatter textFormatter) {
        logThrowable(throwable, 2, textFormatter);
    }

    public static void log(@Nonnull String message, String... prefixes) {
        checkState();

        logMessageUnsafe(String.join("", prefixes), message, 2);
    }

    public static void log(@Nonnull Throwable throwable, String... prefixes) {
        logThrowable(String.join("", prefixes), throwable, 2);
    }

    public static void log(@Nonnull String message, @Nonnull TextFormatter textFormatter, String... prefixes) {
        checkState();

        String prefs = String.join("", prefixes);
        logMessageUnsafe(prefs.isEmpty() ? "" : textFormatter.format(prefs), textFormatter.format(message), 2);
    }


    public static void log(@Nonnull Throwable throwable, @Nonnull TextFormatter textFormatter, String... prefixes) {
        logThrowable(throwable, 2, textFormatter, prefixes);
    }

    public static void log(String prefix, String message) {
        checkState();

        logMessageUnsafe(prefix, message, 2);
    }

    public static void log(@Nonnull String prefix, @Nonnull Throwable throwable) {
        logThrowable(prefix, throwable, 2);
    }

    public static void log(@Nonnull String prefix, @Nonnull String message, @Nonnull TextFormatter textFormatter) {
        checkState();

        logMessageUnsafe(prefix.isEmpty() ? "" : textFormatter.format(prefix), textFormatter.format(message), 2);
    }

    public static void log(@Nonnull String prefix, @Nonnull Throwable throwable, @Nonnull TextFormatter textFormatter) {
        logThrowable(throwable, 2, textFormatter, prefix);
    }

    public static void setupLogger(File log, int listenerQueueCapacity) {
        if(!isConfigured.get()) {
            try {
                if(log.exists()) {
                    log = new File(log.getPath() + 1);
                }
                logFile = log;

                LoggerFormatter formatter = new LoggerFormatter();
                PrintStreamErrorManager errorManager = new PrintStreamErrorManager(SYSTEM_ERR);

                FileLoggerHandler fileHandler = new FileLoggerHandler(log);
                fileHandler.setFormatter(formatter);
                fileHandler.setErrorManager(errorManager);
                loggerHandlers.add(fileHandler);

                PrintStreamLoggerHandler infoHandler = new PrintStreamLoggerHandler();
                infoHandler.setOutputStream(SYSTEM_OUT);
                infoHandler.setEncoding("UTF-8");
                infoHandler.setLevel(Level.INFO);
                infoHandler.setErrorManager(errorManager);
                infoHandler.setFormatter(formatter);
                loggerHandlers.add(infoHandler);

                PrintStreamLoggerHandler warningHandler = new PrintStreamLoggerHandler();
                warningHandler.setOutputStream(SYSTEM_ERR);
                warningHandler.setEncoding("UTF-8");
                warningHandler.setLevel(Level.WARNING);
                warningHandler.setErrorManager(errorManager);
                warningHandler.setFormatter(formatter);
                loggerHandlers.add(warningHandler);

                messageQueue = new MpscLinkedQueue7<>();
                loggerThread.start();

                listenerThread = new LoggerListenerThread(listenerQueueCapacity);
                listenerThread.start();

                isConfigured.set(true);
                log("Logger successfully initialized");

                System.setOut(new LoggerPrintStream(SYSTEM_OUT));
                System.setErr(new LoggerErrorPrintStream(SYSTEM_ERR));

                log("Default print streams replaced");
                MXBeanManager.registerMXBean(bean, LoggerMXBean.class, "com.alesharik.webserver.logger:type=Logger");
                MXBeanManager.registerMXBean(loggingLevelManager, LoggingLevelManagerMXBean.class, "com.alesharik.webserver.logger:type=LoggingLevelManager");
                log("Logger successfully started");
            } catch (SecurityException | UnsupportedEncodingException e) {
                e.printStackTrace(SYSTEM_ERR);
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

    public static boolean isListenerThreadEnabled() {
        return listenerThread.isEnabled();
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

    public static void disable() {
        loggerHandlers.forEach(Handler::close);
        loggerHandlers.clear();
        if(listenerThread != null)
            listenerThread.disable();

        System.setOut(SYSTEM_OUT);
        System.setErr(SYSTEM_ERR);
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

    private static final class LoggerThread extends Thread {
        private final Object synchronizerLock;
        private final LoggerThreadCache cache;
        private final TimeCountStatistics statistics;

        public LoggerThread() {
            setName("LoggerThread");
            setDaemon(true);

            cache = new LoggerThreadCache();
            synchronizerLock = new Object();

            statistics = new FuzzyTimeCountStatistics(1, TimeUnit.SECONDS);
        }

        @Override
        public void run() {
            try {
                while(!isInterrupted()) {
                    while(messageQueue.isEmpty()) {
                        try {
                            synchronized (synchronizerLock) {
                                if(messageQueue.isEmpty())
                                    synchronizerLock.wait();
                            }
                        } catch (InterruptedException e) {
                            if(!shutdown)
                                Logger.SYSTEM_ERR.println("Logger thread was received interrupt signal! Stopping logging...");
                            return;
                        }
                    }

                    Message message = messageQueue.poll();
                    if(message == null) continue;

                    Class<?> clazz = message.getCaller();
                    com.alesharik.webserver.logger.level.Level level = (message.isSout || message.isDebug) ? clazz.getAnnotation(com.alesharik.webserver.logger.level.Level.class) : null;

                    String prefix = cache.get(clazz);
                    if(prefix == null) {
                        prefix = generatePrefixes(clazz);
                        cache.add(clazz, prefix);
                    }
                    String prefixes = prefix + message.getPrefixes() + message.getClassPrefix();
                    String msg = prefixes + ": " + message.getMessage();

                    if(level != null && (message.isDebug || level.replaceSout())) {
                        LoggingLevel loggingLevel = loggingLevelManager.getLoggingLevel(level.value());
                        if(loggingLevel != null)
                            loggingLevel.log(msg);
                    } else {
                        LogRecord record = new LogRecord(Level.INFO, msg);
                        record.setMillis(System.currentTimeMillis());
                        record.setLoggerName("Logger");
                        loggerHandlers.forEach(handler -> handler.publish(record));
                    }
                    Logger.listenerThread.sendMessage(message);

                    statistics.measure(1);
                }
            } catch (Error e) {
                Logger.SYSTEM_ERR.println(e.toString());
                for(StackTraceElement stackTraceElement : e.getStackTrace())
                    Logger.SYSTEM_ERR.println(stackTraceElement.toString());

                Logger.SYSTEM_ERR.println("Logger error detected! Server reboot required!");
                throw e;
            } catch (Throwable e) {
                Logger.SYSTEM_ERR.println(e.toString());
                for(StackTraceElement stackTraceElement : e.getStackTrace())
                    Logger.SYSTEM_ERR.println(stackTraceElement.toString());
                Logger.SYSTEM_ERR.println("Logger thread was interrupted! Stopping logging...");
            }
        }

        @Nullable
        private String generatePrefixes(Class<?> clazz) {
            Iterator<Package> iter = ParentPackageIterator.forPackage(clazz.getName().substring(0, clazz.getName().lastIndexOf(".")));
            StringBuilder ret = new StringBuilder();
            while(iter.hasNext()) {
                Package next = iter.next();
                if(next.isAnnotationPresent(Prefixes.class))
                    for(String s : next.getAnnotation(Prefixes.class).value())
                        ret.append(s);
            }
            return ret.toString();
        }

        public long getMessagePerSecond() {
            statistics.update();
            return statistics.getCount();
        }
    }

    static final class LoggerThreadCache {
        static final int CONTAINS_TIME = 60 * 1000;
        static final int UPDATE_DELAY = 1000;

        private final ConcurrentLiveHashMap<Class<?>, String> prefixes;

        LoggerThreadCache() {
            prefixes = new ConcurrentLiveHashMap<>(UPDATE_DELAY);
            prefixes.start();
        }

        @Nullable
        public String get(Class<?> clazz) {
            if(clazz.isAssignableFrom(LoggingLevelImpl.class))
                return "";

            String prefix = prefixes.get(clazz);
            if(prefix != null)
                prefixes.setTime(clazz, CONTAINS_TIME);
            return prefix;
        }

        public void add(Class<?> clazz, String str) {
            prefixes.put(clazz, str, CONTAINS_TIME);
        }
    }

    @ToString
    @EqualsAndHashCode
    @AllArgsConstructor
    @Getter
    static class Message {
        private final String prefixes;
        private final String message;
        private final Class<?> caller;
        private final String locationPrefix;
        private final boolean isSout;
        private final boolean isDebug;

        public Message(String prefixes, String message, Class<?> caller, String locationPrefix, boolean isSout) {
            this.prefixes = prefixes;
            this.message = message;
            this.caller = caller;
            this.locationPrefix = locationPrefix;
            this.isSout = isSout;
            this.isDebug = false;
        }

        /**
         * Return class prefix and debug prefix if needed
         *
         * @return prefixes or empty string
         */
        public String getClassPrefix() {
            Prefixes annotation;
            if((annotation = caller.getAnnotation(Prefixes.class)) != null && annotation.requireDebugPrefix())
                return String.join("", annotation.value()) + locationPrefix;
            else if(annotation != null)
                return String.join("", annotation.value());
            else
                return locationPrefix;
        }
    }

    @EqualsAndHashCode(callSuper = true)
    static final class MessageWithLogRecord extends Message {
        private final LogRecord logRecord;

        public MessageWithLogRecord(String prefixes, String message, Class<?> caller, String locationPrefix, LogRecord record) {
            super(prefixes, message, caller, locationPrefix, false);
            this.logRecord = record;
        }

        public LogRecord copyLogRecord() {
            LogRecord logRecord = new LogRecord(this.logRecord.getLevel(), this.logRecord.getMessage());
            logRecord.setLoggerName(this.logRecord.getLoggerName());
            logRecord.setMillis(this.logRecord.getMillis());
            return logRecord;
        }
    }

    private static final class LoggerListenerThread extends Thread implements LoggerListenerThreadMXBean {
        private final MpscAtomicArrayQueue<Message> messages;
        private final CopyOnWriteArrayList<LoggerListener> loggerListeners;
        private final Object synchronizerLock;
        private final TimeCountStatistics statistics;

        private final AtomicBoolean enabled = new AtomicBoolean(true);

        public LoggerListenerThread(int listenerQueueCapacity) {
            messages = new MpscAtomicArrayQueue<>(listenerQueueCapacity);
            loggerListeners = new CopyOnWriteArrayList<>();
            synchronizerLock = new Object();
            statistics = new FuzzyTimeCountStatistics(1, TimeUnit.SECONDS);
            setName("LoggerListenerThread");
            setDaemon(true);

            MXBeanManager.registerMXBean(this, LoggerListenerThreadMXBean.class, "com.alesharik.webserver.logger:type=LoggerListener");
        }

        public void disable() {
            enabled.set(false);
            MXBeanManager.unregisterMXBean("com.alesharik.webserver.logger:type=LoggerListener");
        }

        public void addListener(LoggerListener loggerListener) {
            loggerListeners.add(loggerListener);
        }

        public void removeListener(LoggerListener loggerListener) {
            loggerListeners.remove(loggerListener);
        }

        public void sendMessage(Message message) {
            if(!enabled.get()) {
                return;
            }
            messages.add(message);
            synchronized (synchronizerLock) {
                synchronizerLock.notifyAll();
            }
        }

        public void run() {
            while(!isInterrupted() && enabled.get()) {
                Message message = messages.poll();

                if(message != null) {
                    loggerListeners.forEach(loggerListener -> loggerListener.listen(message.getPrefixes(), message.getMessage(), message.getCaller()));
                    if(message instanceof MessageWithLogRecord)
                        loggerListeners.forEach(loggerListener -> loggerListener.postListen(((MessageWithLogRecord) message).copyLogRecord()));
                    statistics.measure(1);
                } else {
                    try {
                        synchronized (synchronizerLock) {
                            if(loggerListeners.isEmpty())
                                synchronizerLock.wait();
                        }
                    } catch (InterruptedException e) {
                        if(!shutdown)
                            Logger.SYSTEM_ERR.println("Logger Listener thread was received interrupt signal! Stopping listening...");
                    }
                }
            }
        }

        @Override
        public int getListenerCount() {
            return loggerListeners.size();
        }

        @Override
        public long getMessagesPerSecond() {
            statistics.update();
            return statistics.getCount();
        }

        @Override
        public boolean isEnabled() {
            return enabled.get();
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

        public TextFormatter(ForegroundColor foregroundColor, BackgroundColor backgroundColor) {
            this.foregroundColor = foregroundColor;
            this.backgroundColor = backgroundColor;
        }

        /**
         * Add color(background and foreground) to string
         *
         * @param str the string
         * @return colored string
         */
        public String format(@Nonnull String str) {
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

        private final AtomicReference<String> lineBuffer;
        private final AtomicInteger state;

        public LoggerPrintStream(OutputStream out) {
            super(out);
            state = new AtomicInteger(0);
            lineBuffer = new AtomicReference<>();
            lineBuffer.set("");
        }

        @Override
        public void flush() {
            Logger.logFromStream(lineBuffer.get(), true);
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
                print('\n');
                flush();
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
        private static final TextFormatter FORMATTER = new TextFormatter(ForegroundColor.RED, BackgroundColor.NONE);
        private static final int NEW_LINE_INDEX = '\n';

        /**
         * Close bit
         */
        private static final int CLOSE = 1;
        /**
         * Error bit
         */
        private static final int ERROR = 2;

        private final AtomicReference<String> lineBuffer;
        private final AtomicInteger state;

        public LoggerErrorPrintStream(OutputStream out) {
            super(out);
            state = new AtomicInteger(0);
            lineBuffer = new AtomicReference<>();
            lineBuffer.set("");
        }

        @Override
        public void flush() {
            Logger.logFromStream(lineBuffer.get(), FORMATTER);
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
                print('\n');
                flush();
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

    private static final class LoggerMXBeanImpl implements LoggerMXBean {

        @Override
        public String getLogFile() {
            return Logger.logFile.getPath();
        }

        @Override
        public int getMessageQueueCapacity() {
            return Logger.messageQueue.capacity();
        }

        @Override
        public int getNamedLoggerCount() {
            return Logger.loggers.size();
        }

        @Override
        public long getMessagesParsedPerSecond() {
            return Logger.loggerThread.getMessagePerSecond();
        }
    }

    private static final class LoggingLevelManagerImpl implements LoggingLevelManager {
        private final Map<String, LoggingLevel> loggingLevels = new ConcurrentHashMap<>();

        @Override
        public LoggingLevel getLoggingLevel(String name) {
            return loggingLevels.get(name);
        }

        @Override
        public LoggingLevel createLoggingLevel(String name) {
            if(loggingLevels.containsKey(name))
                return loggingLevels.get(name);
            LoggingLevel loggingLevel = new LoggingLevelImpl(name);
            loggingLevels.put(name, loggingLevel);
            return loggingLevel;
        }

        @Override
        public int getLoggingLevelCount() {
            return loggingLevels.size();
        }

        @Override
        public List<String> getLoggingLevels() {
            return new ArrayList<>(loggingLevels.keySet());
        }
    }

    private static final class LoggingLevelImpl implements LoggingLevel {
        private final String prefix;
        @Getter
        private final String name;

        private volatile boolean enabled = true;
        @Setter
        @Getter
        private volatile NamedLogger logger = null;
        @Setter
        @Getter
        private volatile TextFormatter formatter = null;

        public LoggingLevelImpl(String name) {
            this.prefix = '[' + name + ']';
            this.name = name;
        }

        @Override
        public void enable() {
            enabled = true;
        }

        @Override
        public void disable() {
            enabled = false;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public void log(String s) {
            if(enabled) {
                TextFormatter textFormatter = this.formatter;
                if(textFormatter != null)
                    s = textFormatter.format(s);

                NamedLogger namedLogger = this.logger;
                if(namedLogger != null)
                    namedLogger.log(prefix, s);
                else
                    Logger.log(prefix, s);
            }
        }

        @Override
        public void log(Exception e) {
            if(enabled) {
                TextFormatter textFormatter = this.formatter;

                NamedLogger namedLogger = this.logger;
                if(namedLogger != null)
                    namedLogger.log(prefix, e);
                else {
                    if(textFormatter == null)
                        Logger.log(prefix, e);
                    else
                        Logger.log(prefix, e, textFormatter);
                }
            }
        }
    }
}

