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

package com.alesharik.database.entity;

import com.alesharik.database.entity.asm.EntityDescription;

/**
 * This class manages all entity creation/deletion and update
 *
 * @param <E>
 */
public interface EntityManager<E> {
    /**
     * Register new entity
     *
     * @param e           new entity instance
     * @param description entity description
     * @return e parameter
     */
    E createEntity(E e, EntityDescription description);

    /**
     * Delete entity from database
     *
     * @param e           entity instance
     * @param description entity description
     */
    void deleteEntity(E e, EntityDescription description);

    /**
     * Update entity field in database
     *
     * @param e           entity instance
     * @param field       entity field name
     * @param description entity description
     * @param fieldValue  current field value
     */
    void updateEntity(E e, String field, EntityDescription description, Object fieldValue);

    /**
     * Return entity field from database
     *
     * @param e           entity instance
     * @param field       entity field name
     * @param description entity description
     * @return value from database
     */
    Object getEntityField(E e, String field, EntityDescription description);
}
