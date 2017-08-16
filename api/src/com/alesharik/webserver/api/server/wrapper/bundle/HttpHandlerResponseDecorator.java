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

import com.alesharik.webserver.api.server.wrapper.http.Request;
import com.alesharik.webserver.api.server.wrapper.http.Response;

/**
 * Provides ability to decorate request before sending
 */
public interface HttpHandlerResponseDecorator {
    /**
     * Executes after {@link HttpHandler} handles request
     *
     * @param request  the request
     * @param response response to decorate
     * @param noAnswer true if no {@link HttpHandler} accept this {@link Request}, overwise false
     */
    void decorate(Request request, Response response, boolean noAnswer);

    final class Ignore implements HttpHandlerResponseDecorator {
        public static final Ignore INSTANCE = new Ignore();

        private Ignore() {
        }

        @Override
        public void decorate(Request request, Response response, boolean noAnswer) {

        }
    }
}
