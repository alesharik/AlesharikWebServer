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

package com.alesharik.webserver.server.api;

import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * This interface used for provide custom request handler
 */
public interface RequestHandler {

    /**
     * Return true if this handler can handle request
     *
     * @param request request form client
     */
    boolean canHandleRequest(@Nonnull Request request) throws IOException;

    /**
     * Handle request
     *
     * @param request  http server request
     * @param response http server response
     * @throws Exception if anything happens
     */
    void handleRequest(@Nonnull Request request, @Nonnull Response response) throws Exception;
}
