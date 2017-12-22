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

import com.alesharik.database.data.Schema;
import com.alesharik.database.data.Table;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * This manager provide access to DB {@link Privilege}s
 */
public interface PrivilegeManager {
    /**
     * Return all possible privileges for table
     *
     * @param table the table
     * @return all possible privileges or empty list
     */
    @Nonnull
    List<Privilege> getPrivileges(@Nonnull Table<?> table);

    /**
     * Return all possible privileges for schema
     *
     * @param schema the schema
     * @return all possible privileges or empty list
     */
    @Nonnull
    List<Privilege> getPrivileges(@Nonnull Schema schema);

    /**
     * Return SELECT privilege for table
     *
     * @param table the table
     * @return SELECT privilege
     */
    @Nonnull
    Privilege selectPrivilege(@Nonnull Table<?> table);

    /**
     * Return SELECT privilege for schema
     *
     * @param schema the schema
     * @return SELECT privilege
     */
    @Nonnull
    Privilege selectPrivilege(@Nonnull Schema schema);

    /**
     * Return INSERT privilege for table
     *
     * @param table the table
     * @return INSERT privilege
     */
    @Nonnull
    Privilege insertPrivilege(@Nonnull Table<?> table);

    /**
     * Return INSERT privilege for schema
     *
     * @param schema the schema
     * @return INSERT privilege
     */
    @Nonnull
    Privilege insertPrivilege(@Nonnull Schema schema);

    /**
     * Return UPDATE privilege for table
     *
     * @param table the table
     * @return UPDATE privilege
     */
    @Nonnull
    Privilege updatePrivilege(@Nonnull Table<?> table);

    /**
     * Return UPDATE privilege for schema
     *
     * @param schema the schema
     * @return UPDATE privilege
     */
    @Nonnull
    Privilege updatePrivilege(@Nonnull Schema schema);

    /**
     * Return DELETE privilege for table
     *
     * @param table the table
     * @return DELETE privilege
     */
    @Nonnull
    Privilege deletePrivilege(@Nonnull Table<?> table);

    /**
     * Return DELETE privilege for schema
     *
     * @param schema the schema
     * @return DELETE privilege
     */
    @Nonnull
    Privilege deletePrivilege(@Nonnull Schema schema);

    /**
     * Return privilege for it's id
     *
     * @param id    privilege's id
     * @param table the table
     * @return privilege or <code>null</code> if privilege not found
     */
    @Nullable
    Privilege byID(@Nonnull PrivilegeId id, @Nonnull Table<?> table);

    /**
     * Return privilege for it's id
     *
     * @param id     privilege's id
     * @param schema the schema
     * @return privilege or <code>null</code> if privilege not found
     */
    @Nullable
    Privilege byID(@Nonnull PrivilegeId id, @Nonnull Schema schema);
}
