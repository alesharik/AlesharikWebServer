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

package com.alesharik.webserver.module.http.bundle.impl.error.impl;

import com.alesharik.webserver.module.http.bundle.impl.error.ErrorVisitor;
import com.alesharik.webserver.module.http.http.Request;
import com.alesharik.webserver.module.http.http.Response;

public final class LoggerErrorVisitor implements ErrorVisitor {
    @Override
    public void visitException(Request request, Response response, Exception e) {
        System.err.println("Exception in request " + request);
        System.err.println("Response: " + response);
        e.printStackTrace();
    }

    @Override
    public void visitError(Request request, Response response) {
        if(response.getResponseCode() >= 500) {
            System.err.println("Server error response code detected!");
            System.err.println("Request: " + request);
            System.err.println("Response: " + response);
        }
    }
}
