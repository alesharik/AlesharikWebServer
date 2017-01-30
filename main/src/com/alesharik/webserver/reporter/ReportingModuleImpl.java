package com.alesharik.webserver.reporter;

import com.alesharik.webserver.api.ThreadFactories;
import com.alesharik.webserver.api.ticking.ExecutorPoolBasedTickingPool;
import com.alesharik.webserver.api.ticking.TickingPool;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefix;
import com.alesharik.webserver.reporting.Reporter;
import com.alesharik.webserver.reporting.ReportingModule;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Prefix("[ReportingModule]")
public class ReportingModuleImpl implements ReportingModule {
    private static final int DEFAULT_THREAD_COUNT = 10;

    private static final AtomicLong idCounter = new AtomicLong(0);

    private final CopyOnWriteArrayList<Reporter> reporters;
    private final ConcurrentHashMap<Reporter, Long> activeReporters;
    private final ThreadGroup threadGroup;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private TickingPool tickingPool;

    private volatile int threadCount = DEFAULT_THREAD_COUNT;

    public ReportingModuleImpl(ThreadGroup parent) {
        reporters = new CopyOnWriteArrayList<>();
        activeReporters = new ConcurrentHashMap<>();
        threadGroup = new ThreadGroup(parent, "ReportingModule-" + idCounter.getAndIncrement());
    }

    @Override
    public void parse(Element configNode) {
        activeReporters.clear();

        Element threadCount = (Element) configNode.getElementsByTagName("threadCount").item(0);
        this.threadCount = Integer.parseInt(threadCount.getTextContent());

        NodeList reporters = configNode.getElementsByTagName("reporters");

        for(int i = 0; i < reporters.getLength(); i++) {
            Element reporter = (Element) reporters.item(i);
            Element nameElement = (Element) reporter.getElementsByTagName("name").item(0);
            String name = nameElement.getTextContent();

            Reporter rep = getReporter(name);
            if(rep == null) {
                Logger.log("Reporter " + name + " not found! Skipping...");
            } else {
                Element fileElement = (Element) reporter.getElementsByTagName("file").item(0);
                File file = new File(fileElement.getTextContent());
                if(file.isDirectory()) {
                    Logger.log("Reporter " + name + " file " + file.toString() + " is a directory. Reporter will receive null as file!");
                    file = null;
                } else if(!file.exists()) {
                    Logger.log("Creating file " + file.toString());
                    try {
                        if(!file.createNewFile()) {
                            Logger.log("Can't create file " + file.toString());
                        }
                    } catch (IOException e) {
                        Logger.log(e);
                    }
                }

                Element periodElement = (Element) reporter.getElementsByTagName("period").item(0);
                long period = Long.parseLong(periodElement.getTextContent());

                Element config = (Element) reporter.getElementsByTagName("configuration").item(0);

                rep.setup(file, period, config);

                activeReporters.put(rep, period);
            }
        }
    }

    @Override
    public void start() {
        if(!isRunning.get()) {
            tickingPool = new ExecutorPoolBasedTickingPool(threadCount, ThreadFactories.newThreadFactory(threadGroup));
            activeReporters.forEach(tickingPool::startTicking);
            isRunning.set(true);
        }
    }

    @Override
    public void shutdown() {
        if(isRunning.get()) {
            tickingPool.shutdown();
        }
    }

    @Override
    public void shutdownNow() {
        if(isRunning.get()) {
            tickingPool.shutdownNow();
        }
    }

    @Override
    public String getName() {
        return "reporting";
    }

    @Override
    public void registerNewReporter(Reporter reporter) {
        Objects.requireNonNull(reporter);
        reporters.add(reporter);
    }

    @Override
    public void unregisterReporter(Reporter reporter) {
        Objects.requireNonNull(reporter);
        reporters.remove(reporter);
    }

    @Override
    public int getReporterCount() {
        return reporters.size();
    }

    @Override
    public int getActiveReporterCount() {
        return activeReporters.size();
    }

    @Override
    public int getThreadCount() {
        return threadCount;
    }

    @Override
    public ThreadGroup getThreadGroup() {
        return threadGroup;
    }

    @Override
    public void reportAll() {
        activeReporters.forEach((reporter, aLong) -> {
            try {
                reporter.tick();
            } catch (Exception e) {
                Logger.log(e);
            }
        });
    }

    private Reporter getReporter(String name) {
        for(Reporter reporter : reporters) {
            if(name.equals(reporter.getName())) {
                return reporter;
            }
        }
        return null;
    }
}
