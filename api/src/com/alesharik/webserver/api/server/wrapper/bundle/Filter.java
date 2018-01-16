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
 * This class filter requests and can decorate it. If request is not valid, you MUST respond with error code!
 */
@Deprecated
public interface Filter {
    /**
     * Accept and decorate request
     *
     * @param request  the request
     * @param response response
     * @return true if request passed and can be handled with {@link HttpHandler}, overwise false
     */
    boolean accept(Request request, Response response);

    class Always implements Filter {
        private static final Always INSTANCE = new Always();

        public static Filter getInstance() {
            return INSTANCE;
        }

        private Always() {
        }

        @Override
        public boolean accept(Request request, Response response) {
            return true;
        }
    }

    class Never implements Filter {
        private static final Never INSTANCE = new Never();

        public static Filter getInstance() {
            return INSTANCE;
        }

        private Never() {
        }

        @Override
        public boolean accept(Request request, Response response) {
            return false;
        }
    }
}
