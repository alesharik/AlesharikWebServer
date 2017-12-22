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
import com.alesharik.database.exception.DatabaseReadingNotPermittedException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * This class is entry point in User/Group/Privilege management system of Database
 *
 * @param <U>       user type
 * @param <G>       group type
 * @param <PrivMan> privilege manager type
 */
public interface UserManager<U extends User, G extends Group, PrivMan extends PrivilegeManager> {
    /**
     * Return all database users
     *
     * @return all database users
     * @throws DatabaseReadingNotPermittedException if current user doesn't have permissions to read information table
     * @throws DatabaseInternalException            if database catch internal exception
     * @throws DatabaseClosedException              if method called on closed connection
     */
    @Nonnull
    List<U> getUsers() throws DatabaseReadingNotPermittedException, DatabaseInternalException, DatabaseClosedException;

    /**
     * Return all database groups
     *
     * @return all database groups
     * @throws DatabaseReadingNotPermittedException if current user doesn't have permissions to read information table
     * @throws DatabaseInternalException            if database catch internal exception
     * @throws DatabaseClosedException              if method called on closed connection
     */
    @Nonnull
    List<G> getGroups() throws DatabaseReadingNotPermittedException, DatabaseInternalException, DatabaseClosedException;

    /**
     * Return current user
     *
     * @return current DB user
     * @throws DatabaseReadingNotPermittedException if current user doesn't have permissions to read information table
     * @throws DatabaseInternalException            if database catch internal exception
     * @throws DatabaseClosedException              if method called on closed connection
     */
    @Nonnull
    U getMe() throws DatabaseReadingNotPermittedException, DatabaseInternalException, DatabaseClosedException;

    /**
     * Return {@link PrivilegeManager} instance
     *
     * @return {@link PrivilegeManager} instance
     */
    @Nonnull
    PrivMan getPrivilegeManager();

    /**
     * Create new database user
     *
     * @param name     user name
     * @param password user password(will be encrypted)
     * @param group    user group. <code>null</code> is 'no group'
     * @param admin    set admin permissions to new user
     * @return new user
     * @throws DatabaseModifyingNotPermittedException if current user can't create users
     * @throws DatabaseInternalException              if database catch internal exception
     * @throws DatabaseClosedException                if method called on closed connection
     */
    @Nonnull
    default U createUser(String name, String password, @Nullable Group group, boolean admin) throws DatabaseModifyingNotPermittedException, DatabaseInternalException, DatabaseClosedException {
        return createUser(name, password, group, admin, admin, admin, admin);
    }

    /**
     * Create new database user
     *
     * @param name          user name
     * @param password      user password(will be encrypted)
     * @param group         user group. <code>null</code> is 'no group'
     * @param admin         set admin permissions to new user
     * @param canCreateDB   give DB creation right to user
     * @param canCreateUser give User creation right to user (usually - full access)
     * @param canCreateRole give Group creation right to user (usually - full access)
     * @return new user
     * @throws DatabaseModifyingNotPermittedException if current user can't create users
     * @throws DatabaseInternalException              if database catch internal exception
     * @throws DatabaseClosedException                if method called on closed connection
     */
    @Nonnull
    U createUser(String name, String password, @Nullable Group group, boolean admin, boolean canCreateDB, boolean canCreateUser, boolean canCreateRole) throws DatabaseModifyingNotPermittedException, DatabaseInternalException, DatabaseClosedException;

    /**
     * Create new database user
     *
     * @param name            user name
     * @param password        user password(will be encrypted)
     * @param group           user group. <code>null</code> is 'no group'
     * @param admin           set admin permissions to new user
     * @param canCreateDB     give DB creation right to user
     * @param canCreateUser   give User creation right to user (usually - full access)
     * @param canCreateRole   give Group creation right to user (usually - full access)
     * @param connectionLimit connection limit
     * @return new user
     * @throws DatabaseModifyingNotPermittedException if current user can't create users
     * @throws DatabaseInternalException              if database catch internal exception
     * @throws DatabaseClosedException                if method called on closed connection
     */
    @Nonnull
    U createUser(String name, String password, @Nullable Group group, boolean admin, boolean canCreateDB, boolean canCreateUser, boolean canCreateRole, int connectionLimit) throws DatabaseModifyingNotPermittedException, DatabaseInternalException, DatabaseClosedException;
}
