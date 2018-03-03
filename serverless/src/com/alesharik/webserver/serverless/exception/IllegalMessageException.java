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

package com.alesharik.webserver.serverless.exception;

public class IllegalMessageException extends AgentException {
    private static final long serialVersionUID = 8019606021616991727L;

    public IllegalMessageException() {
    }

    public IllegalMessageException(String message) {
        super(message);
    }

    public IllegalMessageException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalMessageException(Throwable cause) {
        super(cause);
    }
}
