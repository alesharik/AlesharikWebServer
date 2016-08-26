package com.alesharik.webserver.main.server;

import com.alesharik.webserver.api.MIMETypes;
import com.alesharik.webserver.api.server.RequestHandler;
import com.alesharik.webserver.api.server.RequestHandlerList;
import com.alesharik.webserver.api.server.WebServer;
import com.alesharik.webserver.generators.ModularErrorPageGenerator;
import com.alesharik.webserver.handlers.MainHttpHandler;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.main.FileManager;
import com.alesharik.webserver.main.ServerController;
import com.alesharik.webserver.main.websockets.ServerControllerWebSocketApplication;
import com.alesharik.webserver.plugin.accessManagers.ServerAccessManagerBuilder;
import org.glassfish.grizzly.http.CompressionConfig;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;
import org.glassfish.grizzly.websockets.WebSocketAddOn;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.glassfish.grizzly.websockets.WebSocketEngine;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;

public final class MainServer extends WebServer {
    private final ModularErrorPageGenerator errorPageGenerator;
    private HttpServer httpServer;
    private RequestHandlerList handlerList = new RequestHandlerList();
    private ServerController serverController;


    public MainServer(String host, int port, FileManager fileManager, ServerController serverController) {
        this.serverController = serverController;
        errorPageGenerator = new ModularErrorPageGenerator(fileManager);

        NetworkListener networkListener = new NetworkListener("grizzly", host, port);
        networkListener.getFileCache().setEnabled(true);
        networkListener.getCompressionConfig().setCompressionMode(CompressionConfig.CompressionMode.ON);
        networkListener.getCompressionConfig().setCompressableMimeTypes(
                MIMETypes.findType(".html"),
                MIMETypes.findType(".jpg"),
                MIMETypes.findType(".png"),
                MIMETypes.findType(".css"),
                MIMETypes.findType(".js")
        );
        networkListener.setDefaultErrorPageGenerator(errorPageGenerator);

        httpServer = new HttpServer();
        httpServer.addListener(networkListener);
        ServerConfiguration serverConfiguration = httpServer.getServerConfiguration();
        serverConfiguration.addHttpHandler(new MainHttpHandler(handlerList, fileManager), "/");

        TCPNIOTransportBuilder transportBuilder = TCPNIOTransportBuilder.newInstance();
        transportBuilder.setIOStrategy(networkListener.getTransport().getIOStrategy());
        ThreadPoolConfig config = ThreadPoolConfig.defaultConfig();
        config.setCorePoolSize(30)
                .setMaxPoolSize(50)
                .setQueueLimit(-1);
        transportBuilder.setWorkerThreadPoolConfig(config);
        networkListener.setTransport(transportBuilder.build());

        WebSocketAddOn addOn = new WebSocketAddOn();
        networkListener.registerAddOn(addOn);
        registerNewWebSocket(new ServerControllerWebSocketApplication(fileManager, this), "", "/serverControl");
    }

    @Override
    public synchronized void addRequestHandler(RequestHandler requestHandler) {
        handlerList.add(requestHandler);
    }

    @Override
    public synchronized void removeRequestHandler(RequestHandler requestHandler) {
        handlerList.remove(requestHandler);
    }

    @Override
    public synchronized void start() throws IOException {
        httpServer.start();
    }

    public boolean isLogPassValid(String logPass) {
        try {
            return serverController.isLogPassValid(logPass);
        } catch (IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException | InvalidKeyException | InvalidKeySpecException e) {
            Logger.log(e);
        }
        return false;
    }

    @Override
    public synchronized void shutdown() {
        httpServer.shutdown();
    }

    public synchronized void registerNewWebSocket(WebSocketApplication application, String contextPath, String urlPattern) {
        WebSocketEngine.getEngine().register(contextPath, urlPattern, application);
    }

    public synchronized void unregisterWebSocket(WebSocketApplication application) {
        WebSocketEngine.getEngine().unregister(application);
    }

    @Override
    public void setupServerAccessManagerBuilder(ServerAccessManagerBuilder builder) {
        errorPageGenerator.setupServerAccessManagerBuilder(builder);
        super.setupServerAccessManagerBuilder(builder);
    }
}
