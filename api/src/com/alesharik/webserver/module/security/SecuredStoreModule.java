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

package com.alesharik.webserver.module.security;

import com.alesharik.webserver.configuration.Module;

import javax.annotation.Nonnull;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;

/**
 * This module store passwords and strings in secured store
 */
public interface SecuredStoreModule extends Module {
    /**
     * Bind controller to key
     *
     * @param controller access controller
     * @param name       password name in store
     * @throws IllegalAccessException if controller already bound
     */
    void storeString(@Nonnull SecuredStoreAccessController controller, @Nonnull String name) throws IllegalAccessException;

    /**
     * Read password from store
     * @param name password name in store
     * @return password or empty string if password not exists
     * @throws IllegalAccessException if access controller doesn't grant access to caller class
     */
    @Nonnull
    String readString(@Nonnull String name) throws IllegalAccessException, IOException, InvalidKeySpecException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException;

    /**
     * Write password to store
     * @param name password name in store
     * @param password the password
     * @throws IllegalAccessException if access controller doesn't grant access to caller class
     */
    void writeString(@Nonnull String name, @Nonnull String password) throws IllegalAccessException, InvalidKeySpecException, InvalidKeyException, IOException, BadPaddingException, IllegalBlockSizeException;

    @Nonnull
    @Override
    default String getName() {
        return "secured-password-store";
    }
}
