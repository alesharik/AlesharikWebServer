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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates, what annotated class is Database Entity.
 * Entity must have getters for all fields and setters for all non-final fields. Also entity class must not be final and must have @{@link Creator} method.
 * Field will be ignored only if it is <code>transient</code>. Entity CAN"T HAVE FIELD WITH NAME <code>_entity_manager</code> and <code>_entity_description</code>.
 * Setters are <code>set{field}</code>, where {field} is field name, starting with uppercase char. Setter must have 1 return point and return void! Getters are <code>get{field}</code>, where {field} is field name, starting with uppercase char.
 * {@link Boolean} (include primitive) getters can be named like <code>is{field}</code>. All getter code will bre replaced.
 * Entity extends will be detected if and only if entity extends another entity. Entity can't extends normal objects and override getters/setters of another entity!
 * All entities MUST have primary key!
 * Entity can have basic java arrays or {@link java.util.Collection}. {@link java.util.List}/{@link java.util.Set} or anything like this aren't supported by entity manager.
 * Java arrays will be updated as-is, {@link java.util.Collection} will be replaced with wrapper. {@link java.util.Collection} will offer sync with database in it. This means what
 * you do not need use setter method to update it's values in database, like you need to do with basic java arrays. Collection object must be initialized and be empty, because it's contents will not synchronize with database! If it is not, new {@link java.util.ArrayList} instance will be sed instead. Collections are lazy, arrays - not.
 * Collections iterators aren't lazy. Setter for collection will not do anything, because collection execute synchronization in use process. Entity manager will use provided collection as cache. Variable name <code>value</code> strongly not advised!
 * <p>
 * All entity instantiation is created by {@link sun.misc.Unsafe} features
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Entity {
    /**
     * Return entity version for database migration system
     * @return version. <code>-1</code> disable versioning for current entity
     */
    int version() default -1;
}
