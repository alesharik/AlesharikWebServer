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

package com.alesharik.webserver.api.server.wrapper.bundle;

import com.alesharik.webserver.api.server.wrapper.http.HttpStatus;
import com.alesharik.webserver.api.server.wrapper.http.Request;
import com.alesharik.webserver.api.server.wrapper.http.Response;

import javax.annotation.Nonnull;

@HttpBundle("test")
@Deprecated
public class TestBundle implements HttpHandlerBundle {
    @Override
    public Validator getValidator() {
        return request -> true;
    }

    @Override
    public RequestRouter getRouter() {
        return (request, chains) -> chains[0];
    }

    @Override
    public FilterChain[] getFilterChains() {
        return new FilterChain[]{
                new FilterChain() {
                    @Nonnull
                    @Override
                    public Response handleRequest(Request request, HttpHandler[] httpHandlers) {
                        Response response = Response.getResponse();
                        httpHandlers[0].handle(request, response);
                        return response;
                    }

                    @Override
                    public boolean accept(Request request, Response response) {
                        return true;
                    }
                }
        };
    }

    @Override
    public HttpHandler[] getHttpHandlers() {
        return new HttpHandler[]{
                (request, response) -> {
                    response.setContentLength(0);
                    response.respond(HttpStatus.OK_200);
                }
        };
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return null;
    }
}
