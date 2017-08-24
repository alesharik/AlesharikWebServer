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

package com.alesharik.webserver.reporting;

import com.alesharik.webserver.api.ThreadFactories;
import com.alesharik.webserver.api.agent.classPath.ClassPathScanner;
import com.alesharik.webserver.api.agent.classPath.ListenClass;
import com.alesharik.webserver.api.ticking.ExecutorPoolBasedTickingPool;
import com.alesharik.webserver.api.ticking.TickingPool;
import com.alesharik.webserver.configuration.Layer;
import com.alesharik.webserver.exceptions.error.ConfigurationParseError;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefixes;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@ClassPathScanner
@Prefixes("[ReportingModule]")
public final class ReportingModuleImpl implements ReportingModule {
    private static final int DEFAULT_THREAD_COUNT = 10;
    private static final long DEFAULT_PERIOD = 1000; //1 sec
    static final Map<String, Constructor<?>> reporters = new ConcurrentHashMap<>();

    private static final AtomicLong idCounter = new AtomicLong(0);

    private final ConcurrentHashMap<Reporter, Long> activeReporters = new ConcurrentHashMap<>();
    private final Map<Reporter, Long> programReporters = new ConcurrentHashMap<>();
    private final ThreadGroup threadGroup = new ThreadGroup("ReportingModule-" + idCounter.getAndIncrement());

    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    private volatile int threadCount = DEFAULT_THREAD_COUNT;
    private volatile TickingPool tickingPool;

    @Override
    public void parse(@Nullable Element configNode) {
        if(configNode == null)
            throw new ConfigurationParseError("Reporting module must have configuration");

        activeReporters.clear();

        threadCount = parseThreadCount(configNode);

        Element reportersElement = (Element) configNode.getElementsByTagName("reporters").item(0);
        if(reportersElement != null) {
            NodeList reporters = reportersElement.getElementsByTagName("reporter");
            if(reporters.getLength() == 0) {
                System.err.println("Configuration don't have any reporters. Reporter module won't do anything...");
            } else {
                setupReporters(reporters);
            }
        } else {
            System.err.println("Configuration don't have reporters parameter. Reporter module won't do anything...");
        }
    }

    private void setupReporters(NodeList reporters) {
        for(int i = 0; i < reporters.getLength(); i++) {
            Element reporter = (Element) reporters.item(i);
            Element nameElement = (Element) reporter.getElementsByTagName("name").item(0);
            if(nameElement == null) {
                Logger.log("Reporter with index " + i + " doesn't have a name. Skipping...");
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

    private int parseThreadCount(Element configNode) {
        Element threadCount = (Element) configNode.getElementsByTagName("threadCount").item(0);
        if(threadCount != null) {
            return Integer.parseInt(threadCount.getTextContent());
        } else {
            Logger.log("Configuration don't have threadCount parameter! Use default(" + DEFAULT_THREAD_COUNT + ")...");
            return DEFAULT_THREAD_COUNT;
        }
    }

    private void setupReporter(Element reporter, String name, Reporter rep) {
        Element fileElement = (Element) reporter.getElementsByTagName("file").item(0);
        File file;
        if(fileElement == null) {
            System.out.println("Reporter " + name + " doesn't have a file! It will receive null as file");
            file = null;
        } else {
            file = new File(fileElement.getTextContent());
            if(file.isDirectory()) {
                System.out.println("Reporter " + name + " file " + file.toString() + " is a directory. Reporter will receive null as file");
                file = null;
            } else if(!file.exists()) {
                System.out.println("Creating file " + file.toString());
                try {
                    if(!file.createNewFile()) {
                        System.err.println("Can't create file " + file.toString() + ". Skipping reporter " + name);
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("Can't create file " + file.toString() + ". Skipping reporter " + name);
                    return;
                }
            }
        }

        Element periodElement = (Element) reporter.getElementsByTagName("period").item(0);
        long period;
        if(periodElement != null) {
            period = Long.parseLong(periodElement.getTextContent());
        } else {
            System.out.println("Reporter " + name + " doesn't have a period parameter! It will receive default value(" + DEFAULT_PERIOD + ") as period");
            period = DEFAULT_PERIOD;
        }

        Element config = (Element) reporter.getElementsByTagName("configuration").item(0);
        rep.setup(file, period, config);

        activeReporters.put(rep, period);
    }

    @Override
    public void start() {
        if(!isRunning.get()) {
            tickingPool = new ExecutorPoolBasedTickingPool(threadCount, ThreadFactories.newThreadFactory(threadGroup));
            activeReporters.forEach(tickingPool::startTicking);
            programReporters.forEach(tickingPool::startTicking);
            isRunning.set(true);
        }
    }

    @Override
    public void shutdown() {
        if(isRunning.get()) {
            activeReporters.forEach((reporter, aLong) -> reporter.shutdown());
            programReporters.forEach((reporter, aLong) -> reporter.shutdown());
            tickingPool.shutdown();
            isRunning.set(false);
        }
    }

    @Override
    public void shutdownNow() {
        if(isRunning.get()) {
            activeReporters.forEach((reporter, aLong) -> reporter.shutdownNow());
            programReporters.forEach((reporter, aLong) -> reporter.shutdownNow());
            tickingPool.shutdownNow();
            isRunning.set(false);
        }
    }

    @Override
    public Layer getMainLayer() {
        return null;
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
                e.printStackTrace();
            }
        });
        programReporters.forEach((reporter, l) -> {
            try {
                reporter.tick();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void reload(@Nullable Element configNode) {
        if(configNode == null)
            throw new ConfigurationParseError("Reporting module must have configuration");

        int threadCount = parseThreadCount(configNode);

        if(threadCount != this.threadCount) {
            System.out.println("Reloading ticking pool...");
            if(isRunning.get())
                this.tickingPool.shutdown();
            this.threadCount = threadCount;
            this.tickingPool = new ExecutorPoolBasedTickingPool(threadCount, ThreadFactories.newThreadFactory(threadGroup));
        }

        NodeList reporters = configNode.getElementsByTagName("reporters");

        ArrayList<Reporter> used = new ArrayList<>();

        for(int i = 0; i < reporters.getLength(); i++) {
            Element reporter = (Element) reporters.item(i);
            Element nameElement = (Element) reporter.getElementsByTagName("name").item(0);
            if(nameElement == null) {
                System.err.println("Reporter doesn't have name! Skipping...");
                continue;
            }
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
                System.err.println("Reporter " + name + " not found! Skipping...");
            }
        }

        activeReporters.forEach((reporter, delay) -> {
            if(!used.contains(reporter)) {
                reporter.shutdown();
                tickingPool.stopTicking(reporter);
            }
        });
    }

    @Nullable
    static Reporter getReporter(String name) {
        Constructor<?> constructor = reporters.get(name);
        if(constructor == null)
            return null;
        try {
            return (Reporter) constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    @ListenClass(Reporter.class)
    static void listenReporter(Class<?> reporterClazz) {
        try {
            ReporterName name = reporterClazz.getAnnotation(ReporterName.class);
            if(name == null) {
                System.err.println("Class " + reporterClazz.getCanonicalName() + " doesn't have ReporterName annotation and will be ignored!");
                return;
            }
            Constructor<?> constructor = reporterClazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            reporters.put(name.value(), constructor);
        } catch (NoSuchMethodException e) {
            System.err.println("Class " + reporterClazz.getCanonicalName() + " doesn't have empty constructor and will be ignored!");
        }
    }

    @Override
    public void enableReporter(Reporter reporter) {
        enableReporter(reporter, DEFAULT_PERIOD);
    }

    @Override
    public void enableReporter(Reporter reporter, long time) {
        programReporters.put(reporter, time);
        if(isRunning.get())
            tickingPool.startTicking(reporter, time);
    }

    @Override
    public void disableReporter(Reporter reporter) {
        programReporters.remove(reporter);
        if(isRunning.get()) {
            reporter.shutdown();
            tickingPool.stopTicking(reporter);
        }
    }
}
