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

import com.alesharik.webserver.module.http.bundle.ErrorHandler;
import com.alesharik.webserver.module.http.bundle.HttpBundle;
import com.alesharik.webserver.module.http.bundle.HttpHandlerBundle;
import com.alesharik.webserver.module.http.bundle.Validator;
import com.alesharik.webserver.module.http.bundle.processor.HttpProcessor;
import com.alesharik.webserver.module.http.http.HttpStatus;

import javax.annotation.Nonnull;

@HttpBundle("test")
@Deprecated
public class TestBundle implements HttpHandlerBundle {
    @Nonnull
    @Override
    public Validator getValidator() {
        return request -> true;
    }

    @Nonnull
    @Override
    public ErrorHandler getErrorHandler() {
        return (e, request, response, pool) -> e.printStackTrace();
    }

    @Nonnull
    @Override
    public HttpProcessor getProcessor() {
        return (request, response) -> {
            response.respond(HttpStatus.OK_200);
            response.getWriter().write("test");
        };
    }
}
