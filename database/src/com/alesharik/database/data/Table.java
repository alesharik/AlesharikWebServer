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

package com.alesharik.database.data;

import com.alesharik.database.entity.EntityManager;

import java.util.List;

public interface Table<E> extends EntityManager<E> {
    /**
     * Select entity from database/cache for primary key
     *
     * @param select entity, that contains filled primary key fields
     * @return fully filled entity form database
     */
    E selectByPrimaryKey(E select);

    /**
     * Execute <code>SELECT</code> request on database and parse entities from it. Entity fields will be taken as column names. If column for field not exists,
     * field will be <code>null</code>
     *
     * @param request <code>SELECT</code> request
     * @return returned entities
     */
    List<E> select(String request);

    /**
     * Select elements with limit
     *
     * @param limit max entity count. -1 disable limit and return all entities
     * @return entities from database
     */
    List<E> select(int limit);

    /**
     * Select elements with limit and sort
     *
     * @param limit  max entity count. -1 disable limit and return all entities
     * @param sortBy name of column for sorting
     * @param desc   if true, use descend sorting, overwise ascend
     * @return entities from database
     */
    List<E> select(int limit, String sortBy, boolean desc);

    EntityPreparedStatement<E> prepareStatement(String statement);

    void drop(boolean sure);

    Schema getSchema();

    String getName();
}
