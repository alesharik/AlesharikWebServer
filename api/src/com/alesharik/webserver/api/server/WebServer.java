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

package com.alesharik.webserver.api.server;

import com.alesharik.webserver.api.errorPageGenerators.ErrorPageGenerator;
import com.alesharik.webserver.api.fileManager.FileManager;
import com.alesharik.webserver.control.dashboard.DashboardDataHolder;
import com.alesharik.webserver.server.api.RequestHandler;
import org.glassfish.grizzly.websockets.WebSocketApplication;

import java.io.IOException;

@Deprecated
public abstract class WebServer extends Server {
    public WebServer(String host, int port, FileManager fileManager, DashboardDataHolder holder) {
        super(host, port);
    }

    protected WebServer(String host, int port) {
        super(host, port);
    }

    protected WebServer() {
    }

    public abstract void addRequestHandler(RequestHandler requestHandler);

    public abstract void removeRequestHandler(RequestHandler requestHandler);

    @Override
    public abstract void start() throws IOException;

    @Override
    public abstract void shutdown();

    public abstract void registerNewWebSocket(WebSocketApplication application, String contextPath, String urlPattern);

    public abstract void unregisterWebSocket(WebSocketApplication application);

    public abstract ErrorPageGenerator getErrorPageGenerator();
}
