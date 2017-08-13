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

package com.alesharik.webserver.main.server;

import com.alesharik.webserver.api.MIMETypes;
import com.alesharik.webserver.api.errorPageGenerators.ErrorPageGenerator;
import com.alesharik.webserver.api.fileManager.FileManager;
import com.alesharik.webserver.api.server.WebServer;
import com.alesharik.webserver.control.dashboard.DashboardDataHolder;
import com.alesharik.webserver.generators.ModularErrorPageGenerator;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.main.ServerController;
import com.alesharik.webserver.server.api.RequestHandler;
import com.alesharik.webserver.server.api.RequestHandlerList;
import org.glassfish.grizzly.http.server.CompressionLevel;
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
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;

@Deprecated
public final class MainServer extends WebServer {
    private final ModularErrorPageGenerator errorPageGenerator;
    private HttpServer httpServer;
    private RequestHandlerList handlerList = new RequestHandlerList();
    private ServerController serverController;

    public MainServer(String host, int port, FileManager fileManager, ServerController serverController, DashboardDataHolder holder, boolean logRequests, File logFile) {
        super(host, port);
        this.serverController = serverController;
        errorPageGenerator = new ModularErrorPageGenerator(fileManager);

        NetworkListener networkListener = new NetworkListener("grizzly", host, port);
        networkListener.getFileCache().setEnabled(true);
        networkListener.setCompression(String.valueOf(CompressionLevel.ON));
        String compressedMimeTypes = MIMETypes.findType(".html") +
                ',' +
                MIMETypes.findType(".jpg") +
                ',' +
                MIMETypes.findType(".png") +
                ',' +
                MIMETypes.findType(".css") +
                ',' +
                MIMETypes.findType(".js");
        networkListener.setCompressableMimeTypes(compressedMimeTypes);

        httpServer = new HttpServer();
        httpServer.addListener(networkListener);
        ServerConfiguration serverConfiguration = httpServer.getServerConfiguration();
        serverConfiguration.setJmxEnabled(true);
//        serverConfiguration.addHttpHandler(new MainHttpHandler(fileManager, logRequests, logFile, errorPageGenerator), "/");

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
        httpServer.stop();
    }

    public synchronized void registerNewWebSocket(WebSocketApplication application, String contextPath, String urlPattern) {
        WebSocketEngine.getEngine().register(contextPath, urlPattern, application);
    }

    public synchronized void unregisterWebSocket(WebSocketApplication application) {
        WebSocketEngine.getEngine().unregister(application);
    }

    @Override
    public ErrorPageGenerator getErrorPageGenerator() {
        return errorPageGenerator;
    }
}
