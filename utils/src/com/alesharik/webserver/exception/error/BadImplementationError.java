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

package com.alesharik.webserver.exception.error;

import javax.annotation.Nullable;

/**
 * This means that class implements method incorrectly
 */
public final class BadImplementationError extends UnexpectedBehaviorError {
    private static final long serialVersionUID = -7226837153421331381L;

    public BadImplementationError(String message) {
        super(message);
    }

    @Override
    @Nullable
    public Throwable initCause(Throwable cause) {
        return null;
    }

    @Override
    @Nullable
    public Throwable getCause() {
        return null;
    }
}
