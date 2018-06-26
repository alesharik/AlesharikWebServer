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
import com.alesharik.webserver.configuration.module.Module;
import com.alesharik.webserver.configuration.module.ShutdownNow;
import com.alesharik.webserver.configuration.module.Start;
import com.alesharik.webserver.configuration.module.layer.Layer;
import com.alesharik.webserver.daemon.annotation.Shutdown;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Server MXBean shows after server start
 */
@Module("http-server")//todo rewrite
public final class HttpServerModuleImpl implements HttpServerModule {
    private final Set<ServerSocketWrapper> wrappers;
    private final Set<HttpHandlerBundle> bundles;
    private final HttpServerStatisticsImpl httpServerStatistics = new HttpServerStatisticsImpl();
    private final List<String> addons = new ArrayList<>();

    @Getter
    private volatile ExecutorPool pool;
    @Getter
    private volatile HttpRequestHandler handler;
    @Getter
    private volatile ThreadGroup serverThreadGroup;

    private volatile AcceptorThread acceptorThread;
    private volatile SelectorContextImpl.Factory factory;
    private MainLayerImpl mainLayer;

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
//
//    @Override
//    public void parse(@Nullable Element configNode) {
//        if(configNode == null)
//            throw new ConfigurationParseError("HttpServer configuration must have configuration!");
//
//        String groupName = getString("group", configNode, false);
//        if(groupName == null)
//            groupName = getName();
//
//
//        Element poolConfig = getXmlElement("pool", configNode, true);
//
//        String poolName = getString("name", poolConfig, true);
//        Class<?> poolClass = NamedManager.getClassForNameAndType(poolName, ExecutorPool.class);
//        if(poolClass == null)
//            throw new ConfigurationParseError("Server doesn't have ExecutorPool with name " + poolName + "!");
//
//        int selectorCount = getInteger("selector", poolConfig, true, 10);
//        int workerCount = getInteger("worker", poolConfig, true, 10);
//
//        ExecutorPool executorPool;
//
//        ThreadGroup threadGroup = new ThreadGroup(groupName);
//        try {
//            Constructor<?> constructor = poolClass.getDeclaredConstructor(int.class, int.class, ThreadGroup.class);
//            constructor.setAccessible(true);
//            executorPool = (ExecutorPool) constructor.newInstance(selectorCount, workerCount, threadGroup);
//        } catch (NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException e) {
//            throw new ConfigurationParseError(e);
//        }
//
//
//        Set<ServerSocketWrapper> wrappers = new HashSet<>();
//        getElementList("wrappers", "wrapper", configNode, true).forEach((Element element) -> {
//            String name = getString("name", element, true);
//            Class<?> clazz = NamedManager.getClassForName(name);
//            if(clazz == null)
//                throw new ConfigurationParseError("Server doesn't have ServerSocketWrapper with name " + name + "!");
//
//            ServerSocketWrapper wrapper;
//            try {
//                wrapper = (ServerSocketWrapper) clazz.newInstance();
//            } catch (InstantiationException | IllegalAccessException e) {
//                throw new ConfigurationParseError(e);
//            }
//            Element config = getXmlElement("configuration", element, false);
//
//            wrapper.setExecutorPool(executorPool);
//            wrapper.parseConfig(config);
//
//            wrappers.add(wrapper);
//        });
//
//        this.wrappers.clear();
//        this.wrappers.addAll(wrappers);
//        this.pool = executorPool;
//        this.serverThreadGroup = threadGroup;
//
//        this.bundles.clear();
//        getList("bundles", "bundle", configNode, true)
//                .stream()
//                .map(s -> {
//                    Class<?> clazz = HttpBundleManager.getBundleClass(s);
//                    if(clazz == null)
//                        throw new ConfigurationParseError("Server doesn't have HttpHandlerBundle with name " + s + "!");
//                    return clazz;
//                })
//                .map(aClass -> {
//                    HttpBundle annotation = aClass.getAnnotation(HttpBundle.class);
//                    try {
//                        return Pair.of(aClass, (HttpBundle.Condition) annotation.condition().newInstance());
//                    } catch (InstantiationException | IllegalAccessException e) {
//                        throw new ConfigurationParseError(e);
//                    }
//                })
//                .filter(classHttpBundlePair -> classHttpBundlePair.getRight().allow(this))
//                .map(classConditionPair -> {
//                    try {
//                        return (HttpHandlerBundle) classConditionPair.getLeft().newInstance();
//                    } catch (InstantiationException | IllegalAccessException e) {
//                        throw new ConfigurationParseError(e);
//                    }
//                })
//                .forEach(bundles::add);
//
//        String handlerName = getString("handler", configNode, true);
//        Class<?> handlerClazz = NamedManager.getClassForNameAndType(handlerName, HttpRequestHandler.class);
//        if(handlerClazz == null)
//            throw new ConfigurationParseError("Server doesn't have HttpRequestHandler with name " + handlerName + "!");
//        HttpRequestHandler httpRequestHandler;
//        try {
//            Constructor<?> constructor = handlerClazz.getDeclaredConstructor(Set.class);
//            constructor.setAccessible(true);
//            httpRequestHandler = (HttpRequestHandler) constructor.newInstance(bundles);
//        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
//            throw new ConfigurationParseError(e);
//        }
//
//        addons.addAll(getList("addons", "addon", configNode, true));
//
//        this.handler = httpRequestHandler;
//
//        this.mainLayer = new MainLayerImpl(wrappers, pool);
//        this.acceptorThread = new AcceptorThread(threadGroup, executorPool);
//        this.factory = () -> new SelectorContextImpl(httpServerStatistics, httpRequestHandler, executorPool, addons);
//    }

    @Start
    public void start() {
        for(ServerSocketWrapper wrapper : this.wrappers) {
            this.acceptorThread.handle(wrapper);
        }
        acceptorThread.start();
        pool.setSelectorContexts(factory);
    }

    @Shutdown
    public void shutdown() {
        acceptorThread.shutdown();
        httpServerStatistics.responseTime.reset();
    }

    @ShutdownNow
    public void shutdownNow() {
        acceptorThread.shutdown();
        httpServerStatistics.responseTime.reset();
    }

    @Override
    public ExecutorPoolMXBean getPoolMXBean() {
        return pool;
    }

    @Override
    public HttpServerStatistics getStatistics() {
        return httpServerStatistics;
    }

    @Layer("main")
    private static final class MainLayerImpl {
        private final ExecutorPool executorPool;
        private final WrapperLayer wrappers;

        public MainLayerImpl(Set<ServerSocketWrapper> wrappers, ExecutorPool executorPool) {
            this.wrappers = new WrapperLayer(new ArrayList<>(wrappers));
            this.executorPool = executorPool;
        }

        @AllArgsConstructor
        @Layer("wrappers")
        private static final class WrapperLayer {
            private final List<ServerSocketWrapper> wrappers;

            @Start
            public void start() {
                for(ServerSocketWrapper wrapper : wrappers) wrapper.start();
            }

            @Shutdown
            public void shutdown() {
                for(ServerSocketWrapper wrapper : wrappers) wrapper.shutdown();
            }

            @ShutdownNow
            public void shutdownNow() {
                for(ServerSocketWrapper wrapper : wrappers) wrapper.shutdownNow();
            }
        }
    }

    static final class HttpServerStatisticsImpl implements HttpServerStatistics {
        final AtomicLong aliveConnections = new AtomicLong();
        private final Counter connectionCounter = new AtomicCounter();
        private final Counter requestCounter = new AtomicCounter();
        private final Counter errorCounter = new AtomicCounter();
        private final AverageCounter responseTime = new BasicAverageCounter();

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
