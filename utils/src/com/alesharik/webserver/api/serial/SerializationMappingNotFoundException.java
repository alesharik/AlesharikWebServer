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

package com.alesharik.webserver.api.serial;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;

/**
 * This exception will be thrown when {@link Serial} doesn't have mapping for id. You have to synchronize mappings for id
 * and then deserialize data
 */
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
public final class SerializationMappingNotFoundException extends RuntimeException {
    @Getter
    private final long id;

    @Override
    @Nonnull
    public String getMessage() {
        return "Mapping for id " + id + " not found!";
    }

    @Override
    @Contract("_ -> null")
    public Throwable initCause(Throwable cause) {
        return null;
    }

    @Override
    @Contract("-> null")
    public Throwable getCause() {
        return null;
    }
}
