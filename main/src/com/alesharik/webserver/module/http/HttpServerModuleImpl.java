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

package com.alesharik.webserver.module.http;

import com.alesharik.webserver.api.name.NamedManager;
import com.alesharik.webserver.api.server.wrapper.bundle.HttpBundle;
import com.alesharik.webserver.api.server.wrapper.bundle.HttpHandlerBundle;
import com.alesharik.webserver.api.server.wrapper.server.ExecutorPool;
import com.alesharik.webserver.api.server.wrapper.server.HttpRequestHandler;
import com.alesharik.webserver.api.server.wrapper.server.HttpServerModule;
import com.alesharik.webserver.api.server.wrapper.server.ServerSocketWrapper;
import com.alesharik.webserver.api.server.wrapper.server.mx.ExecutorPoolMXBean;
import com.alesharik.webserver.api.server.wrapper.server.mx.HttpServerStatistics;
import com.alesharik.webserver.api.statistics.AtomicCounter;
import com.alesharik.webserver.api.statistics.AverageCounter;
import com.alesharik.webserver.api.statistics.BasicAverageCounter;
import com.alesharik.webserver.api.statistics.Counter;
import com.alesharik.webserver.configuration.Layer;
import com.alesharik.webserver.configuration.SubModule;
import com.alesharik.webserver.exceptions.error.ConfigurationParseError;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.alesharik.webserver.configuration.XmlHelper.*;

/**
 * Server MXBean shows after server start
 */
public final class HttpServerModuleImpl implements HttpServerModule {
    private final Set<ServerSocketWrapper> wrappers;
    private final Set<HttpHandlerBundle> bundles;
    private final HttpServerStatisticsImpl httpServerStatistics = new HttpServerStatisticsImpl();

    @Getter
    private volatile ExecutorPool pool;
    @Getter
    private volatile HttpRequestHandler handler;
    @Getter
    private volatile ThreadGroup serverThreadGroup;
    @Getter
    private volatile Layer mainLayer;

    private volatile AcceptorThread acceptorThread;
    private volatile SelectorContextImpl.Factory factory;

    public HttpServerModuleImpl() {
        wrappers = new CopyOnWriteArraySet<>();
        bundles = new CopyOnWriteArraySet<>();
    }

    @Override
    public Set<ServerSocketWrapper> getServerSocketWrappers() {
        return Collections.unmodifiableSet(wrappers);
    }

    @Override
    public Set<HttpHandlerBundle> getBundles() {
        return Collections.unmodifiableSet(bundles);
    }

    @Override
    public void addHttpHandlerBundle(HttpHandlerBundle bundle) {
        bundles.add(bundle);
    }

    @Override
    public void parse(@Nullable Element configNode) {
        if(configNode == null)
            throw new ConfigurationParseError("HttpServer configuration must have configuration!");

        String groupName = getString("group", configNode, false);
        if(groupName == null)
            groupName = getName();


        Element poolConfig = getXmlElement("pool", configNode, true);

        String poolName = getString("name", poolConfig, true);
        Class<?> poolClass = NamedManager.getClassForNameAndType(poolName, ExecutorPool.class);
        if(poolClass == null)
            throw new ConfigurationParseError("Server doesn't have ExecutorPool with name " + poolName + "!");

        int selectorCount = getInteger("selector", poolConfig, true, 10);
        int workerCount = getInteger("worker", poolConfig, true, 10);

        ExecutorPool executorPool;

        ThreadGroup threadGroup = new ThreadGroup(groupName);
        try {
            Constructor<?> constructor = poolClass.getDeclaredConstructor(int.class, int.class, ThreadGroup.class);
            constructor.setAccessible(true);
            executorPool = (ExecutorPool) constructor.newInstance(selectorCount, workerCount, threadGroup);
        } catch (NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new ConfigurationParseError(e);
        }


        Set<ServerSocketWrapper> wrappers = new HashSet<>();
        getElementList("wrappers", "wrapper", configNode, true).forEach((Element element) -> {
            String name = getString("name", element, true);
            Class<?> clazz = NamedManager.getClassForName(name);
            if(clazz == null)
                throw new ConfigurationParseError("Server doesn't have ServerSocketWrapper with name " + name + "!");

            ServerSocketWrapper wrapper;
            try {
                wrapper = (ServerSocketWrapper) clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new ConfigurationParseError(e);
            }
            Element config = getXmlElement("configuration", element, false);

            wrapper.setExecutorPool(executorPool);
            wrapper.parseConfig(config);

            wrappers.add(wrapper);
        });

        this.wrappers.clear();
        this.wrappers.addAll(wrappers);
        this.pool = executorPool;
        this.serverThreadGroup = threadGroup;

        this.bundles.clear();
        getList("bundles", "bundle", configNode, true)
                .stream()
                .map(s -> {
                    Class<?> clazz = HttpBundleManager.getBundleClass(s);
                    if(clazz == null)
                        throw new ConfigurationParseError("Server doesn't have HttpHandlerBundle with name " + s + "!");
                    return clazz;
                })
                .map(aClass -> {
                    HttpBundle annotation = aClass.getAnnotation(HttpBundle.class);
                    try {
                        return Pair.of(aClass, (HttpBundle.Condition) annotation.condition().newInstance());
                    } catch (InstantiationException | IllegalAccessException e) {
                        throw new ConfigurationParseError(e);
                    }
                })
                .filter(classHttpBundlePair -> classHttpBundlePair.getRight().allow(this))
                .map(classConditionPair -> {
                    try {
                        return (HttpHandlerBundle) classConditionPair.getLeft().newInstance();
                    } catch (InstantiationException | IllegalAccessException e) {
                        throw new ConfigurationParseError(e);
                    }
                })
                .forEach(bundles::add);

        String handlerName = getString("handler", configNode, true);
        Class<?> handlerClazz = NamedManager.getClassForNameAndType(handlerName, HttpRequestHandler.class);
        if(handlerClazz == null)
            throw new ConfigurationParseError("Server doesn't have HttpRequestHandler with name " + handlerName + "!");
        HttpRequestHandler httpRequestHandler;
        try {
            Constructor<?> constructor = handlerClazz.getDeclaredConstructor(Set.class);
            constructor.setAccessible(true);
            httpRequestHandler = (HttpRequestHandler) constructor.newInstance(bundles);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new ConfigurationParseError(e);
        }

        this.handler = httpRequestHandler;

        this.mainLayer = new MainLayerImpl(wrappers, pool);
        this.acceptorThread = new AcceptorThread(threadGroup, executorPool);
        this.factory = () -> new SelectorContextImpl(httpServerStatistics, httpRequestHandler, executorPool);
    }

    @Override
    public void reload(@Nullable Element configNode) {
        shutdown();
        parse(configNode);
        start();
    }

    @Override
    public void start() {
        mainLayer.start();
        for(ServerSocketWrapper wrapper : this.wrappers) {
            this.acceptorThread.handle(wrapper);
        }
        acceptorThread.start();
        pool.setSelectorContexts(factory);
    }

    @Override
    public void shutdown() {
        mainLayer.shutdown();
        acceptorThread.shutdown();
        httpServerStatistics.responseTime.reset();
    }

    @Override
    public void shutdownNow() {
        mainLayer.shutdownNow();
        acceptorThread.shutdown();
        httpServerStatistics.responseTime.reset();
    }

    @Nonnull
    @Override
    public String getName() {
        return "http-server";
    }

    @Override
    public ExecutorPoolMXBean getPoolMXBean() {
        return pool;
    }

    @Override
    public HttpServerStatistics getStatistics() {
        return httpServerStatistics;
    }

    private static final class MainLayerImpl implements Layer {
        private final List<SubModule> executorPool;
        private final List<Layer> layer;

        public MainLayerImpl(Set<ServerSocketWrapper> wrappers, ExecutorPool executorPool) {
            this.layer = Collections.singletonList(new WrapperLayer(new ArrayList<>(wrappers)));
            this.executorPool = Collections.singletonList(executorPool);
        }

        @Nonnull
        @Override
        public List<SubModule> getSubModules() {
            return executorPool;
        }

        @Nonnull
        @Override
        public List<Layer> getSubLayers() {
            return layer;
        }

        @Nonnull
        @Override
        public String getName() {
            return "http-server-layer";
        }

        @AllArgsConstructor
        private static final class WrapperLayer implements Layer {
            private final List<SubModule> wrappers;

            @Nonnull
            @Override
            public List<SubModule> getSubModules() {
                return wrappers;
            }

            @Nonnull
            @Override
            public List<Layer> getSubLayers() {
                return Collections.emptyList();
            }

            @Nonnull
            @Override
            public String getName() {
                return "wrappers";
            }
        }
    }

    static final class HttpServerStatisticsImpl implements HttpServerStatistics {
        private final Counter connectionCounter = new AtomicCounter();
        private final Counter requestCounter = new AtomicCounter();
        private final Counter errorCounter = new AtomicCounter();
        private final AverageCounter responseTime = new BasicAverageCounter();

        final AtomicLong aliveConnections = new AtomicLong();

        public HttpServerStatisticsImpl() {
            responseTime.setTimeDelay(1, TimeUnit.SECONDS);
        }

        @Override
        public long getAliveConnections() {
            return aliveConnections.get();
        }

        @Override
        public long getConnectionCount() {
            return connectionCounter.getAmount();
        }

        @Override
        public void resetConnectionCount() {
            connectionCounter.reset();
        }

        void newConnection() {
            connectionCounter.add();
        }

        void newRequest() {
            requestCounter.add();
        }

        void newError() {
            errorCounter.add();
        }

        void addResponseTimeAvg(long a) {
            responseTime.addUnit(a);
        }

        @Override
        public long getRequestCount() {
            return requestCounter.getAmount();
        }

        @Override
        public void resetRequestCount() {
            requestCounter.reset();
        }

        @Override
        public long getErrorCount() {
            return errorCounter.getAmount();
        }

        @Override
        public void resetErrorCount() {
            errorCounter.reset();
        }

        @Override
        public long getAverageResponseTime() {
            return responseTime.getAverage();
        }
    }
}
