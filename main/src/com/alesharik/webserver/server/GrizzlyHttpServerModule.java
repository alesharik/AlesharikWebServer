package com.alesharik.webserver.server;

import com.alesharik.webserver.api.Utils;
import com.alesharik.webserver.configuration.Layer;
import com.alesharik.webserver.configuration.ModuleManager;
import com.alesharik.webserver.exceptions.error.ConfigurationParseError;
import com.alesharik.webserver.logger.Prefixes;
import com.alesharik.webserver.server.api.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.grizzly.websockets.WebSocketAddOn;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

@Prefixes("[GrizzlyHttpServerModule]")
public final class GrizzlyHttpServerModule implements HttpServerModule {
    private final ArrayList<NetworkListener> networkListeners;

    private HttpServer httpServer;

    public GrizzlyHttpServerModule() {
        networkListeners = new ArrayList<>();
    }

    @Override
    public long requestCount() {
        return 0;
    }

    @Override
    public int getCoreThreadCount() {
        return 0;
    }

    @Override
    public int getMaxThreadCount() {
        return 0;
    }

    @Override
    public int getWorkerQueueLimit() {
        return 0;
    }

    @Override
    public int getWorkerQueueSize() {
        return 0;
    }

    @Override
    public int getSelectorThreadCount() {
        return 0;
    }

    @Override
    public void parse(@Nullable Element configNode) {
        if(configNode == null) {
            throw new ConfigurationParseError("Server don't have configuration!");
        }

        Element networkListenersNode = (Element) configNode.getElementsByTagName("networkListeners").item(0);
        if(networkListenersNode == null) {
            System.out.println("Network listeners not found!");
        }

        NodeList networkListenerNodeList = networkListenersNode.getElementsByTagName("networkListener");
        if(networkListenerNodeList == null || networkListenerNodeList.getLength() == 0) {
            System.out.println("Network listeners not found!");
        }

        for(int i = 0; i < networkListenerNodeList.getLength(); i++) {
            Element networkListenerNode = (Element) networkListenerNodeList.item(i);

            String host;
            int port;

            Node hostNode = networkListenerNode.getElementsByTagName("host").item(0);
            if(hostNode == null) {
                throw new ConfigurationParseError("Server host node not found!");
            } else {
                host = hostNode.getTextContent();
            }
            if(host.equals("default")) {
                host = Utils.getExternalIp();
            }

            Node portNode = networkListenerNode.getElementsByTagName("host").item(0);
            if(portNode == null) {
                throw new ConfigurationParseError("Server port node not found!");
            } else {
                port = Integer.parseInt(portNode.getTextContent());
            }

            String name;
            Element nameNode = (Element) networkListenerNode.getElementsByTagName("name").item(0);
            if(nameNode == null) {
                throw new ConfigurationParseError("Name not found!");
            } else {
                name = nameNode.getTextContent();
            }

            NetworkListener networkListener = new NetworkListener(name, host, port);
//            networkListener.
            //TODO write config

            WebSocketAddOn addOn = new WebSocketAddOn();
            networkListener.registerAddOn(addOn); //TODO wirte it
            networkListeners.add(networkListener);
        }

        httpServer = new HttpServer();
        networkListeners.forEach(httpServer::addListener);

        ServerConfiguration configuration = httpServer.getServerConfiguration();
        //TODO write config

        Element config = (Element) configNode.getElementsByTagName("config").item(0);

//
//        NetworkListener networkListener = new NetworkListener("grizzly", host, port);
//        networkListener.getFileCache().setEnabled(true);
//        networkListener.setCompression(String.valueOf(CompressionLevel.ON));
//        String compressedMimeTypes = MIMETypes.findType(".html") +
//                ',' +
//                MIMETypes.findType(".jpg") +
//                ',' +
//                MIMETypes.findType(".png") +
//                ',' +
//                MIMETypes.findType(".css") +
//                ',' +
//                MIMETypes.findType(".js");
//        networkListener.setCompressableMimeTypes(compressedMimeTypes);

        ServerConfiguration serverConfiguration = httpServer.getServerConfiguration();
        serverConfiguration.setJmxEnabled(true);

        Element httpHandlersNode = (Element) configNode.getElementsByTagName("httpHandlers").item(0);
        if(httpHandlersNode == null) {
            System.out.println("Http handlers node not found!");
        } else {
            Map<String, HttpHandler> handlers = ModuleManager.getModules().stream()
                    .filter(module -> HttpHandler.class.isAssignableFrom(module.getClass()))
                    .map(module -> (HttpHandler) module)
                    .collect(Collectors.toMap(HttpHandler::getHandlerName, httpHandler -> httpHandler));

            NodeList httpHandlerList = httpHandlersNode.getElementsByTagName("httpHandler");
            for(int i = 0; i < httpHandlerList.getLength(); i++) {
                String handlerName = httpHandlerList.item(i).getTextContent();
                if(handlers.containsKey(handlerName)) {
                    serverConfiguration.addHttpHandler(handlers.get(handlerName));
                } else {
                    System.out.println(handlerName + " not found!");
                }
            }
        }
//        serverConfiguration.addHttpHandler(new MainHttpHandler(handlerList, fileManager, logRequests, logFile, errorPageGenerator), "/");
//      //TODO write it
//        TCPNIOTransportBuilder transportBuilder = TCPNIOTransportBuilder.newInstance();
//        transportBuilder.setIOStrategy(networkListener.getTransport().getIOStrategy());
//        ThreadPoolConfig config = ThreadPoolConfig.defaultConfig();
//        config.setCorePoolSize(30)
//                .setMaxPoolSize(50)
//                .setQueueLimit(-1);
//        transportBuilder.setWorkerThreadPoolConfig(config);
//        networkListener.setTransport(transportBuilder.build());


    }

    @Override
    public void reload(@Nullable Element configNode) {
        shutdown();
        parse(configNode);
        start();
    }

    @Override
    public void start() {
        try {
            httpServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void shutdown() {
        httpServer.stop();
    }

    @Override
    public void shutdownNow() {
        httpServer.stop();
    }

    @Nonnull
    @Override
    public String getName() {
        return "grizzly-http-server";
    }

    @Nullable
    @Override
    public Layer getMainLayer() {
        return null;
    }
}
