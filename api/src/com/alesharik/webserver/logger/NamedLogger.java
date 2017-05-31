package com.alesharik.webserver.logger;

import com.alesharik.webserver.api.misc.Triple;
import com.alesharik.webserver.api.mx.bean.MXBeanManager;
import com.alesharik.webserver.logger.mx.NamedLoggerMXBean;
import com.alesharik.webserver.logger.storing.DisabledStoringStrategy;
import com.alesharik.webserver.logger.storing.StoringStrategy;
import com.alesharik.webserver.logger.storing.StoringStrategyFactory;
import lombok.Getter;
import lombok.Setter;
import org.jctools.queues.atomic.MpscLinkedAtomicQueue;
import sun.misc.Cleaner;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.locks.LockSupport;

/**
 * This type of logger write data in it's log file and in main log with prefixes <code>[Logger][name]</code>,
 * where name is it's unique name.<br>
 * Creation: {@link Logger#createNewNamedLogger(String name, File file)}<br>
 * By default it use {@link DisabledStoringStrategy}
 * @see StoringStrategy
 * @see StoringStrategyFactory
 */
@NotThreadSafe
@Prefixes("[NamedLogger]")
public final class NamedLogger implements Closeable, NamedLoggerMXBean {
    private static final NamedLoggerStoringStrategyHandlerThread storingStrategyHandler;

    static {
        storingStrategyHandler = new NamedLoggerStoringStrategyHandlerThread();
        storingStrategyHandler.start();
    }

    @Getter
    private final String name;
    private final File file;

    @Getter
    @Setter
    private String defaultPrefix;
    private StoringStrategy storingStrategy;

    private boolean isClosed = false;

    NamedLogger(String name, File file) {
        this.name = name;
        this.file = file;
        this.defaultPrefix = "";
        MXBeanManager.registerMXBean(this, NamedLoggerMXBean.class, "NamedLogger-" + name);
        Cleaner.create(this, () -> {
            isClosed = true;
            tryCloseStrategy();
            Logger.loggers.remove(name);
            MXBeanManager.unregisterMXBean("NamedLogger-" + name);
        });
    }

    public void setStoringStrategyFactory(@Nonnull StoringStrategyFactory<?> factory) {
        updateStrategy(factory);
    }

    /**
     * Close logger and unregister MXBean
     */
    public void close() throws IOException {
        isClosed = true;
        if(storingStrategy != null)
            storingStrategy.close();
        Logger.loggers.remove(name);
        MXBeanManager.unregisterMXBean("NamedLogger-" + name);
    }

    public void log(String message) {
        log(defaultPrefix, message);
    }

    public void log(Throwable throwable) {
        log(defaultPrefix, throwable);
    }

    public void log(String message, String... prefixes) {
        StringBuilder sb = new StringBuilder();
        Arrays.asList(prefixes).forEach(sb::append);
        log(sb.toString(), message);
    }

    public void log(Throwable throwable, String... prefixes) {
        StringBuilder sb = new StringBuilder();
        Arrays.asList(prefixes).forEach(sb::append);
        log(sb.toString(), throwable);
    }

    public void log(String prefix, String message) {
        checkIfClosed();

        String completePrefix = "[Logger][" + name + ']' + prefix;
        publishIntoStoringStrategy(completePrefix, message);
        Logger.log(completePrefix, message);
    }

    public void log(String prefix, Throwable throwable) {
        checkIfClosed();

        String completePrefix = "[Logger][" + name + "]" + prefix;

        String firstMessage = throwable.getMessage();
        publishIntoStoringStrategy(completePrefix, firstMessage);
        Logger.log(completePrefix + firstMessage);
        for(StackTraceElement stackTraceElement : throwable.getStackTrace()) {
            String message = stackTraceElement.toString();
            publishIntoStoringStrategy(completePrefix, message);
            Logger.log(message);
        }
    }

    private void publishIntoStoringStrategy(String prefix, String message) {
        if(storingStrategy != null)
            storingStrategyHandler.publishMessage(storingStrategy, prefix, message);
    }

    private void checkIfClosed() {
        if(isClosed) {
            throw new IllegalStateException("Logger was closed!");
        }
    }

    /**
     * Close strategy. If {@link StoringStrategy#close()} method throws {@link IOException}, it will be logged
     */
    private void tryCloseStrategy() {
        try {
            if(storingStrategy != null) {
                storingStrategy.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateStrategy(StoringStrategyFactory factory) {
        try {
            tryCloseStrategy();

            storingStrategy = factory.newInstance(file);
            storingStrategy.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getStoringStrategy() {
        String canonicalName = storingStrategy.getClass().getCanonicalName();
        if(canonicalName == null)
            return storingStrategy.getClass().getName();
        return canonicalName;
    }

    @Override
    public String getFile() {
        return file.getPath();
    }

    private static final class NamedLoggerStoringStrategyHandlerThread extends Thread {
        private final MpscLinkedAtomicQueue<Triple<StoringStrategy, String, String>> messageQueue;
        private final ElementSignaller elementSignaller;

        public NamedLoggerStoringStrategyHandlerThread() {
            setDaemon(true);
            setPriority(Thread.MIN_PRIORITY + 2);
            setName("NamedLogger-StoringStrategyHandler");

            this.messageQueue = new MpscLinkedAtomicQueue<>();
            this.elementSignaller = new ElementSignaller(messageQueue, this);
        }

        @Override
        public void run() {
            while(isAlive() && !isInterrupted()) {
                if(messageQueue.isEmpty())
                    try {
                        ForkJoinPool.managedBlock(elementSignaller);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                Triple<StoringStrategy, String, String> msg = messageQueue.poll();
                msg.getA().publish(msg.getB(), msg.getC());
            }
        }

        public void publishMessage(StoringStrategy storingStrategy, String prefix, String message) {
            messageQueue.offer(Triple.immutable(storingStrategy, prefix, message));
            LockSupport.unpark(elementSignaller.thread);
        }

        private static final class ElementSignaller implements ForkJoinPool.ManagedBlocker {
            private final MpscLinkedAtomicQueue queue;
            private final Thread thread;

            public ElementSignaller(MpscLinkedAtomicQueue queue, Thread thread) {
                this.queue = queue;
                this.thread = thread;
            }

            @Override
            public boolean block() {
                if(isReleasable())
                    return true;
                else
                    LockSupport.park(this);
                return isReleasable();
            }

            @Override
            public boolean isReleasable() {
                return !queue.isEmpty();
            }
        }
    }
}
