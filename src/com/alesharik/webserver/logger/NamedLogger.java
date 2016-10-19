package com.alesharik.webserver.logger;

import com.alesharik.webserver.logger.storingStrategies.DisabledStoringStrategy;
import com.alesharik.webserver.logger.storingStrategies.StoringStrategy;
import com.alesharik.webserver.logger.storingStrategies.StoringStrategyFactory;
import lombok.Setter;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

import static com.alesharik.webserver.logger.Logger.getPrefixLocation;

/**
 * This logger used for write data in the main log and the specific log. It use specific prefix.
 * Creation: {@link Logger}.createNewNamedLogger(String name, File file)
 */
public final class NamedLogger {
    private static final Finalizer FINALIZER = new Finalizer();

    private final String name;
    private final File file;

    private StoringStrategyFactory<? extends StoringStrategy> factory = DisabledStoringStrategy::new;
    private StoringStrategy storingStrategy;
    private boolean isClosed = false;

    @Setter
    private String defaultPrefix = "";

    NamedLogger(String name, File file) {
        this.name = name;
        this.file = file;
    }

    public <T extends StoringStrategy> void setStoringStrategyFactory(StoringStrategyFactory<T> factory) {
        this.factory = factory;
        updateStrategy();
    }

    private void updateStrategy() {
        try {
            if(storingStrategy != null) {
                storingStrategy.close();
            }

            storingStrategy = factory.newInstance(file);
            storingStrategy.open();
        } catch (IOException e) {
            Logger.log(e);
        }
    }

    @SneakyThrows
    public void close() {
        isClosed = true;
        if(storingStrategy != null) {
            storingStrategy.close();
        }
        Logger.loggers.remove(name);
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

        String prefixx = "[Logger][" + name + "]" + prefix;
        storingStrategy.publish(prefixx, message);
        Logger.log(prefixx, message);
    }

    public void log(String prefix, Throwable throwable) {
        checkIfClosed();

        String prefixx = "[Logger][" + name + "]" + prefix;

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(prefixx);
        stringBuilder.append(throwable.getMessage());
        Arrays.asList(throwable.getStackTrace()).forEach(stackTraceElement -> {
            stringBuilder.append(prefixx);
            stringBuilder.append(stackTraceElement.toString());
            stringBuilder.append("\n");
        });
        storingStrategy.publish(prefix, stringBuilder.toString());
        Logger.log(stringBuilder.toString());
    }

    public void log(String message) {
        log(defaultPrefix, message);
    }

    public void log(Throwable throwable) {
        log(getPrefixLocation(2), throwable);
    }

    private void checkIfClosed() {
        if(isClosed) {
            throw new IllegalStateException("Logger was closed!");
        }
    }

    @Override
    protected void finalize() throws Throwable {
        FINALIZER.addLogger(this);
        super.finalize();
    }

    private static class Finalizer extends Thread {
        private LinkedBlockingQueue<NamedLogger> loggers = new LinkedBlockingQueue<>();

        public Finalizer() {
            this.setDaemon(true);
            this.setName("NamedLoggerFinalizer");
            this.start();
        }

        @SneakyThrows
        public void run() {
            while(isAlive()) {
                NamedLogger next = loggers.poll();
                if(next == null) {
                    Thread.sleep(1);
                } else {
                    next.close();
                }
            }
        }

        public void addLogger(NamedLogger namedLogger) {
            loggers.add(namedLogger);
        }
    }
}
