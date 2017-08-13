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

import javax.crypto.SecretKey;

/**
 * This class provides access control ank key for {@link SecuredStoreModule} key(s)
 */
public interface SecuredStoreAccessController {
    /**
     * Return true if class can access password(s), overwise false
     *
     * @param clazz class, that tries to access
     * @return can class access password(s)
     */
    boolean grantAccess(Class<?> clazz);

    /**
     * Must be same all load cycles
     */
    SecretKey passwordKey();
}
