package com.alesharik.webserver.reporter;

import com.alesharik.webserver.api.ThreadFactories;
import com.alesharik.webserver.api.ticking.ExecutorPoolBasedTickingPool;
import com.alesharik.webserver.api.ticking.TickingPool;
import com.alesharik.webserver.configuration.Layer;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefix;
import com.alesharik.webserver.reporting.Reporter;
import com.alesharik.webserver.reporting.ReportingModule;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Prefix("[ReportingModule]")
public class ReportingModuleImpl implements ReportingModule {
    private static final int DEFAULT_THREAD_COUNT = 10;
    private static final long DEFAULT_PREIOD = 1000; //1 sec

    private static final AtomicLong idCounter = new AtomicLong(0);

    private final CopyOnWriteArrayList<Reporter> reporters;
    private final ConcurrentHashMap<Reporter, Long> activeReporters;
    private final ThreadGroup threadGroup;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private TickingPool tickingPool;

    private AtomicInteger threadCount = new AtomicInteger(DEFAULT_THREAD_COUNT);

    public ReportingModuleImpl(ThreadGroup parent) {
        reporters = new CopyOnWriteArrayList<>();
        activeReporters = new ConcurrentHashMap<>();
        threadGroup = new ThreadGroup(parent, "ReportingModule-" + idCounter.getAndIncrement());
    }

    @Override
    public void parse(Element configNode) {
        activeReporters.clear();

        parseThreadCount(configNode);

        Element reportersElement = (Element) configNode.getElementsByTagName("reporters").item(0);
        if(reportersElement != null) {
            NodeList reporters = reportersElement.getElementsByTagName("reporter");
            if(reporters.getLength() == 0) {
                Logger.log("Configuration don't have any reporters. Reporters won't do anything...");
            } else {
                setupReporters(reporters);
            }
        } else {
            Logger.log("Configuration don't have reporters parameter. Reporters won't do anything...");
        }
    }

    private void setupReporters(NodeList reporters) {
        for(int i = 0; i < reporters.getLength(); i++) {
            Element reporter = (Element) reporters.item(i);
            Element nameElement = (Element) reporter.getElementsByTagName("name").item(0);
            if(nameElement == null) {
                Logger.log("Reporter with index " + i + " don't have a name. Skipping...");
                continue;
            }
            String name = nameElement.getTextContent();

            Reporter rep = getReporter(name);
            if(rep == null) {
                Logger.log("Reporter " + name + " not found! Skipping...");
            } else {
                setupReporter(reporter, name, rep);
            }
        }
    }

    private void parseThreadCount(Element configNode) {
        Element threadCount = (Element) configNode.getElementsByTagName("threadCount").item(0);
        if(threadCount != null) {
            this.threadCount.set(Integer.parseInt(threadCount.getTextContent()));
        } else {
            Logger.log("Configuration don't have threadCount parameter! Use default(" + DEFAULT_THREAD_COUNT + ")...");
            this.threadCount.set(DEFAULT_THREAD_COUNT);
        }
    }

    private void setupReporter(Element reporter, String name, Reporter rep) {
        Element fileElement = (Element) reporter.getElementsByTagName("file").item(0);
        File file;
        if(fileElement == null) {
            Logger.log("Reporter " + name + " doesn't have a file! It will receive null as file.");
            file = null;
        } else {
            file = new File(fileElement.getTextContent());
            if(file.isDirectory()) {
                Logger.log("Reporter " + name + " file " + file.toString() + " is a directory. Reporter will receive null as file.");
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
        }

        Element periodElement = (Element) reporter.getElementsByTagName("period").item(0);
        long period;
        if(periodElement != null) {
            period = Long.parseLong(periodElement.getTextContent());
        } else {
            Logger.log("Reporter " + name + " doesn't have a period parameter! It will receive default value(" + DEFAULT_PREIOD + ") as period.");
            period = DEFAULT_PREIOD;
        }

        Element config = (Element) reporter.getElementsByTagName("configuration").item(0);
        if(config == null) {
            Logger.log("Reporter " + name + " doesn't have a configuration!");
        }

        rep.setup(file, period, config);

        activeReporters.put(rep, period);
    }

    @Override
    public void start() {
        if(!isRunning.get()) {
            tickingPool = new ExecutorPoolBasedTickingPool(threadCount.get(), ThreadFactories.newThreadFactory(threadGroup));
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
    public Layer getMainLayer() {
        return null;
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
        return threadCount.get();
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

    @Override
    public void reload(Element configNode) {
        int threadCount = Integer.parseInt(configNode.getElementsByTagName("threadCount").item(0).getTextContent());

        if(threadCount != this.threadCount.get()) {
            Logger.log("Reloading ticking pool...");
            shutdown();
            this.threadCount.set(threadCount);
            this.tickingPool = new ExecutorPoolBasedTickingPool(threadCount, ThreadFactories.newThreadFactory(threadGroup));
        }

        NodeList reporters = configNode.getElementsByTagName("reporters");

        ArrayList<Reporter> used = new ArrayList<>();

        for(int i = 0; i < reporters.getLength(); i++) {
            Element reporter = (Element) reporters.item(i);
            Element nameElement = (Element) reporter.getElementsByTagName("name").item(0);
            String name = nameElement.getTextContent();

            Reporter rep = getReporter(name);
            if(rep != null) {
                used.add(rep);
                if(!isRunning.get()) {
                    setupReporter(reporter, name, rep);
                } else {
                    if(activeReporters.containsKey(rep)) {
                        Element config = (Element) reporter.getElementsByTagName("configuration").item(0);
                        rep.reload(config);
                    } else { //Add reporter
                        setupReporter(reporter, name, rep);
                        tickingPool.startTicking(rep, activeReporters.get(rep));
                    }
                }
            } else {
                Logger.log("Reporter " + name + " not found! Skipping...");
            }
        }

        activeReporters.forEach((reporter, delay) -> {
            if(!used.contains(reporter)) {
                reporter.shutdown();
                tickingPool.stopTicking(reporter);
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
