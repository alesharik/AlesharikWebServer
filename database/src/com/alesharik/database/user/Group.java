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

import javax.annotation.Nonnull;
import java.util.List;

/**
 * This class represents DB group
 *
 * @param <U>     user type
 * @param <Write> writable group type
 */
public interface Group<U extends User, Write extends Group.WriteGroup<?, U>> extends Privileged {
    /**
     * Return all users from this group
     *
     * @return user list or empty list
     */
    @Nonnull
    List<U> getUsers();

    /**
     * Return group name
     *
     * @return group name
     */
    @Nonnull
    String getName();

    /**
     * Return new {@link WriteGroup}
     *
     * @return new {@link WriteGroup}
     */
    @Nonnull
    Write write();

    /**
     * This interface allow editing groups. It will use transactions
     *
     * @param <T> this type
     * @param <U> user type
     */
    interface WriteGroup<T extends WriteGroup, U extends User> extends WritablePrivileged<T> {
        /**
         * Add user to group
         *
         * @param user the user
         * @return this instance
         */
        @Nonnull
        T addUser(@Nonnull U user);

        /**
         * Remove user from this group
         *
         * @param user the user
         * @return this instance
         */
        @Nonnull
        T removeUser(@Nonnull U user);

        /**
         * Rename group
         *
         * @param newName new name
         * @return this instance
         */
        @Nonnull
        T rename(String newName);

        /**
         * Drop this group
         *
         * @param sure true - drop, false - ignore
         * @return this instance
         */
        @Nonnull
        T drop(boolean sure);

        /**
         * Commit changes
         */
        void commit();

        /**
         * Rollback changes
         */
        void rollback();
    }
}
