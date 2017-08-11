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

package com.alesharik.database.exception;

/**
 * This exception is superclass for all database store exceptions
 */
public abstract class DatabaseStoreException extends DatabaseException {
    private static final long serialVersionUID = 8123263938213813523L;

    protected DatabaseStoreException() {
    }

    protected DatabaseStoreException(String message) {
        super(message);
    }

    protected DatabaseStoreException(String message, Throwable cause) {
        super(message, cause);
    }

    protected DatabaseStoreException(Throwable cause) {
        super(cause);
    }

    protected DatabaseStoreException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
