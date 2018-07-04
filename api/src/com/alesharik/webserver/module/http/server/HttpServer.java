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

package com.alesharik.webserver.module.http.server;


import com.alesharik.webserver.module.http.bundle.HttpHandlerBundle;
import com.alesharik.webserver.module.http.server.mx.HttpServerMXBean;
import com.alesharik.webserver.module.http.server.socket.ServerSocketWrapper;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * This interface provides basic access for HttpServer
 */
public interface HttpServer extends HttpServerMXBean {
    @Nonnull
    ExecutorPool getPool();

    @Nonnull
    HttpRequestHandler getHandler();

    @Nonnull
    List<ServerSocketWrapper> getServerSocketWrappers();

    @Nonnull
    List<HttpHandlerBundle> getBundles();

    @Nonnull
    ThreadGroup getServerThreadGroup();

    void addHttpHandlerBundle(HttpHandlerBundle bundle);

    void removeHttpHandlerBundle(HttpHandlerBundle bundle);
}
