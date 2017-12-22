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

package com.alesharik.database.user;

import com.alesharik.database.exception.DatabaseClosedException;
import com.alesharik.database.exception.DatabaseInternalException;
import com.alesharik.database.exception.DatabaseReadingNotPermittedException;

import javax.annotation.Nonnull;

/**
 * Privileged class have DB privileges for tables/schemas
 */
public interface Privileged {
    /**
     * Return true if privilege is 'granted'
     *
     * @param privilege the privilege
     * @return true if privilege is 'granted'
     * @throws DatabaseReadingNotPermittedException if current user doesn't have permissions to read information table
     * @throws DatabaseInternalException            if database catch internal exception
     * @throws DatabaseClosedException              if method called on closed connection
     */
    boolean hasPrivilege(@Nonnull Privilege privilege) throws DatabaseReadingNotPermittedException, DatabaseInternalException, DatabaseClosedException;
}
