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
import com.alesharik.database.exception.DatabaseModifyingNotPermittedException;

import javax.annotation.Nonnull;

/**
 * Implementing class can change it's state
 *
 * @param <T>
 */
public interface WritablePrivileged<T> {
    /**
     * Grant privilege to it
     *
     * @param privilege the privilege
     * @return this instance
     * @throws DatabaseModifyingNotPermittedException if current user can't create users
     * @throws DatabaseInternalException              if database catch internal exception
     * @throws DatabaseClosedException                if method called on closed connection
     */
    @Nonnull
    T grant(@Nonnull Privilege privilege) throws DatabaseModifyingNotPermittedException, DatabaseInternalException, DatabaseClosedException;

    /**
     * Revoke privilege from it
     *
     * @param privilege the privilege
     * @return this instance
     * @throws DatabaseModifyingNotPermittedException if current user can't create users
     * @throws DatabaseInternalException              if database catch internal exception
     * @throws DatabaseClosedException                if method called on closed connection
     */
    @Nonnull
    T revoke(@Nonnull Privilege privilege) throws DatabaseModifyingNotPermittedException, DatabaseInternalException, DatabaseClosedException;

    /**
     * Revoke all privileges from it
     *
     * @return this instance
     * @throws DatabaseModifyingNotPermittedException if current user can't create users
     * @throws DatabaseInternalException              if database catch internal exception
     * @throws DatabaseClosedException                if method called on closed connection
     */
    @Nonnull
    T revokeAll() throws DatabaseModifyingNotPermittedException, DatabaseInternalException, DatabaseClosedException;
}
