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

import com.alesharik.webserver.api.mx.bean.MXBeanManager;
import com.alesharik.webserver.api.name.NamedManager;
import com.alesharik.webserver.api.statistics.AtomicCounter;
import com.alesharik.webserver.api.statistics.AverageCounter;
import com.alesharik.webserver.api.statistics.BasicAverageCounter;
import com.alesharik.webserver.api.statistics.Counter;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationElement;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationObject;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationTypedObject;
import com.alesharik.webserver.extension.module.ConfigurationError;
import com.alesharik.webserver.extension.module.Configure;
import com.alesharik.webserver.extension.module.Module;
import com.alesharik.webserver.extension.module.Shutdown;
import com.alesharik.webserver.extension.module.ShutdownNow;
import com.alesharik.webserver.extension.module.Start;
import com.alesharik.webserver.extension.module.layer.meta.SubModuleAdapter;
import com.alesharik.webserver.extension.module.layer.meta.SubModuleMetaFactory;
import com.alesharik.webserver.extension.module.meta.ScriptElementConverter;
import com.alesharik.webserver.internals.instance.ClassInstantiator;
import com.alesharik.webserver.module.http.bundle.HttpBundle;
import com.alesharik.webserver.module.http.bundle.HttpHandlerBundle;
import com.alesharik.webserver.module.http.server.ExecutorPool;
import com.alesharik.webserver.module.http.server.HttpRequestHandler;
import com.alesharik.webserver.module.http.server.HttpServer;
import com.alesharik.webserver.module.http.server.mx.ExecutorPoolMXBean;
import com.alesharik.webserver.module.http.server.mx.HttpServerStatistics;
import com.alesharik.webserver.module.http.server.socket.ServerSocketWrapper;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.alesharik.webserver.extension.module.ConfigurationUtils.*;

/**
 * Server MXBean shows after server start
 */
@Module(value = "http-server", autoInvoke = false)
public final class HttpServerModuleImpl implements HttpServer {
    private final List<ServerSocketWrapper> wrappers = new CopyOnWriteArrayList<>();
    private final List<HttpHandlerBundle> bundles = new CopyOnWriteArrayList<>();
    private final HttpServerStatisticsImpl httpServerStatistics = new HttpServerStatisticsImpl();
    private final List<String> addons = new ArrayList<>();
    private final Map<ServerSocketWrapper, SubModuleAdapter> wrapperSubmodules = new ConcurrentHashMap<>();

    @Getter
    private volatile ExecutorPool pool;
    @Getter
    private volatile HttpRequestHandler handler;
    @Getter
    private volatile ThreadGroup serverThreadGroup;

    private volatile AcceptorThread acceptorThread;
    private volatile SelectorContextImpl.Factory factory;

    @Override
    public List<ServerSocketWrapper> getServerSocketWrappers() {
        return wrappers;
    }

    @Override
    public List<HttpHandlerBundle> getBundles() {
        return bundles;
    }

    @Override
    public void addHttpHandlerBundle(HttpHandlerBundle bundle) {
        bundles.add(bundle);
    }

    @Override
    public void removeHttpHandlerBundle(HttpHandlerBundle bundle) {
        bundles.remove(bundle);
    }

    @Configure
    public void parse(ConfigurationTypedObject object, ScriptElementConverter converter) {
        wrapperSubmodules.clear();
        String groupName = getString("group", object.getElement("group"), converter)
                .orElse("http-server");

        ConfigurationObject pool = getObject("pool", object.getElement("pool"), converter)
                .orElseThrow(() -> new ConfigurationError("pool can't be null!"));

        String poolName = getString("name", pool.getElement("name"), converter)
                .orElseThrow(() -> new ConfigurationError("pool name can't be null!"));
        Class<?> poolClass = NamedManager.getClassForNameAndType(poolName, ExecutorPool.class);
        if(poolClass == null)
            throw new ConfigurationError("Pool " + poolName + " not found!");

        int selectorCount = getInteger("selector", pool.getElement("selector"), converter)
                .orElse(5);
        int workerCount = getInteger("worker", pool.getElement("worker"), converter)
                .orElse(10);

        serverThreadGroup = new ThreadGroup(groupName);
        try {
            Constructor<?> constructor = poolClass.getDeclaredConstructor(int.class, int.class, ThreadGroup.class);
            constructor.setAccessible(true);
            this.pool = (ExecutorPool) constructor.newInstance(selectorCount, workerCount, serverThreadGroup);
        } catch (NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new ConfigurationError(e);
        }

        wrappers.clear();

        getArray("wrappers", object.getElement("wrappers"), converter).ifPresent(configurationElements -> {
            for(ConfigurationElement configurationElement : configurationElements) {
                if(!(configurationElement instanceof ConfigurationObject))
                    throw new ConfigurationError("element in wrappers must be an object!");
                ConfigurationObject o = (ConfigurationObject) configurationElement;

                String name = getString("name", o.getElement("name"), converter)
                        .orElseThrow(() -> new ConfigurationError("wrapper name can't be null!"));

                Class<?> clazz = NamedManager.getClassForNameAndType(name, ServerSocketWrapper.class);
                ServerSocketWrapper wrapper = (ServerSocketWrapper) ClassInstantiator.instantiate(clazz);

                wrapper.setExecutorPool(this.pool);
                wrapper.parseConfig(
                        getObject("configuration", o.getElement("configuration"), converter)
                                .orElse(null)
                        , converter);

                wrappers.add(wrapper);
            }
        });

        getArray("bundles", object.getElement("bundles"), converter).ifPresent(configurationElements -> {
            for(ConfigurationElement configurationElement : configurationElements) {
                getString("bundle", configurationElement, converter)
                        .map(s -> {
                            Class<?> clazz = HttpBundleManager.getBundleClass(s);
                            if(clazz == null)
                                throw new ConfigurationError("Server doesn't have HttpHandlerBundle with name " + s + "!");
                            return clazz;
                        })
                        .map(aClass -> {
                            HttpBundle annotation = aClass.getAnnotation(HttpBundle.class);
                            return Pair.of(aClass, (HttpBundle.Condition) ClassInstantiator.instantiate(annotation.condition()));
                        })
                        .filter(classHttpBundlePair -> classHttpBundlePair.getRight().allow(this))
                        .map(classConditionPair -> (HttpHandlerBundle) ClassInstantiator.instantiate(classConditionPair.getKey()))
                        .ifPresent(bundles::add);

            }
        });

        String handler = getString("handler", object.getElement("handler"), converter)
                .orElseThrow(() -> new ConfigurationError("handler can't be null!"));
        Class<?> handlerClazz = NamedManager.getClassForNameAndType(handler, HttpRequestHandler.class);
        if(handlerClazz == null)
            throw new ConfigurationError("Server doesn't have HttpRequestHandler with name " + handler + "!");
        try {
            Constructor<?> constructor = handlerClazz.getDeclaredConstructor(List.class);
            constructor.setAccessible(true);
            this.handler = (HttpRequestHandler) constructor.newInstance(bundles);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new ConfigurationError(e);
        }

        getArray("addons", object.getElement("addons"), converter).ifPresent(configurationElements -> {
            for(ConfigurationElement configurationElement : configurationElements)
                getString("addon", configurationElement, converter)
                        .ifPresent(addons::add);
        });

        acceptorThread = new AcceptorThread(serverThreadGroup, this.pool);
        factory = () -> new SelectorContextImpl(httpServerStatistics, this.handler, this.pool, addons);
    }

    @Start
    public void start() {
        pool.start();
        pool.setSelectorContexts(factory);
        for(ServerSocketWrapper wrapper : wrappers) {
            SubModuleAdapter subModuleAdapter = SubModuleMetaFactory.create(wrapper);
            subModuleAdapter.start();
            wrapperSubmodules.put(wrapper, subModuleAdapter);
            acceptorThread.handle(wrapper);
        }
        acceptorThread.start();
        MXBeanManager.registerMXBean(this, "com.alesharik.webserver.module.http.HttpServerModuleImpl:a=1");
    }

    @Shutdown
    public void shutdown() {
        acceptorThread.shutdown();
        for(ServerSocketWrapper wrapper : wrappers) {
            SubModuleAdapter subModuleAdapter = wrapperSubmodules.get(wrapper);
            subModuleAdapter.shutdown();
        }
        pool.shutdown();
    }

    @ShutdownNow
    public void shutdownNow() {
        acceptorThread.shutdownNow();
        for(ServerSocketWrapper wrapper : wrappers) {
            SubModuleAdapter subModuleAdapter = SubModuleMetaFactory.create(wrapper);
            subModuleAdapter.shutdownNow();
        }
        pool.shutdownNow();
    }

    @Override
    public ExecutorPoolMXBean getPoolMXBean() {
        return pool;
    }

    @Override
    public HttpServerStatistics getStatistics() {
        return httpServerStatistics;
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
