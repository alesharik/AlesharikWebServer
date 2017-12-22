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

import org.jetbrains.annotations.Contract;

import javax.annotation.Nullable;

/**
 * This exception will be thrown if current user can't do requested action because of the rights
 */
public abstract class DatabaseUnprivilegedActionException extends DatabaseException {
    private static final long serialVersionUID = 642431061973138017L;

    public DatabaseUnprivilegedActionException(String message) {
        super(message);
    }

    @Nullable
    @Contract("-> null")
    @Override
    public Throwable getCause() {
        return null;
    }

    @Override
    @Contract("_ -> null")
    @Nullable
    public Throwable initCause(Throwable cause) {
        return null;
    }
}
