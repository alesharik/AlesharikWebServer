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
import javax.annotation.Nullable;

/**
 * This class represents DB user
 *
 * @param <G>     group type
 * @param <Write> user's {@link WriteUser}
 */
public interface User<G extends Group, Write extends User.WriteUser> extends Privileged {
    /**
     * Return user name
     *
     * @return user name
     */
    String getName();

    /**
     * Return true if user is admin
     *
     * @return true if user is admin
     */
    boolean isAdmin();

    /**
     * Return true if user is superuser
     *
     * @return true if user is superuser
     */
    boolean isSuperUser();

    /**
     * Return true if user can manage Databases
     *
     * @return true if user can manage Databases
     */
    boolean canCreateDB();

    /**
     * Return true if user can manage Users(usually admin)
     *
     * @return true if user can manage Users
     */
    boolean canCreateUser();

    /**
     * Return true if user can manage roles(usually admin)
     *
     * @return true if user can manage roles
     */
    boolean canCreateRole();

    /**
     * Return user's group
     *
     * @return user's group. <code>null</code> - no group
     */
    @Nullable
    G getGroup();

    /**
     * Return user's {@link WriteUser}
     */
    @Nonnull
    Write write();

    /**
     * This interface allow to edit user. It always use transactions
     *
     * @param <T> this type
     */
    interface WriteUser<T extends WriteUser> extends WritablePrivileged<T> {
        /**
         * Allow/deny user to manage databases
         *
         * @param can true - allow, false - deny
         * @return this instance
         */
        @Nonnull
        T setCreateDB(boolean can);

        /**
         * Allow/deny user to manage users
         *
         * @param can true - allow, false - deny
         * @return this instance
         */
        @Nonnull
        T setCanCreateUser(boolean can);

        /**
         * Allow/deny user to manage groups
         *
         * @param can true - allow, false - deny
         * @return this instance
         */
        @Nonnull
        T setCanCreateGroup(boolean can);

        /**
         * Rename user
         *
         * @param newName new name
         * @return this instance
         */
        @Nonnull
        T rename(String newName);

        /**
         * Set user parameter
         *
         * @param name  the parameter key
         * @param param the parameter value
         * @return this instance
         */
        @Nonnull
        T setParameter(String name, Object param);

        /**
         * Reset user parameter
         *
         * @param name the parameter name
         * @return this instance
         */
        @Nonnull
        T resetParameter(String name);

        /**
         * Drop user
         *
         * @param sure true - drop, false - ignore
         * @return this instance
         */
        @Nonnull
        T drop(boolean sure);

        /**
         * Change user's password
         *
         * @param name new password
         * @return this instance
         */
        @Nonnull
        T changePassword(String name);

        /**
         * Grant superuser
         *
         * @param is true - grant, false - revoke
         * @return this instance
         */
        @Nonnull
        T setSuperUser(boolean is);

        /**
         * Commit changes
         */
        void commit();

        /**
         * Ignore changes
         */
        void rollback();
    }
}
