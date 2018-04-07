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

package com.alesharik.database.entity.asm;

import com.alesharik.webserver.api.reflection.FieldAccessor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.lang.reflect.Field;
import java.util.Collection;

@ToString
@EqualsAndHashCode
@Getter
public final class EntityColumn {
    private final Field field;
    private final String columnName;
    private final boolean foreign;
    private final String foreignTable;
    private final String foreignColumn;
    private final boolean indexed;
    private final boolean primary;
    private final boolean unique;
    private final boolean nullable;
    private final String constraint;
    private final String constraintName;
    private final String overrideDomain;
    private final boolean bridge;
    private final boolean lazy;

    EntityColumn(Field fieldName, String columnName, boolean foreign, String foreignTable, String foreignColumn, boolean indexed, boolean primary, boolean unique, boolean nullable, String constraint, String constraintName, String overrideDomain, boolean bridge, boolean lazy) {
        this.field = fieldName;
        this.constraint = constraint;
        this.constraintName = constraintName;
        this.overrideDomain = overrideDomain;
        this.columnName = columnName.isEmpty() ? fieldName.getName() : columnName;
        this.foreign = foreign;
        this.foreignTable = foreignTable;
        this.foreignColumn = foreignColumn;
        this.indexed = indexed;
        this.primary = primary;
        this.unique = unique;
        this.nullable = nullable;

        this.bridge = bridge;
        this.lazy = lazy;

        this.field.setAccessible(true);

    }

    public boolean isArray() {
        return field.getType().isArray() || field.getType().isAssignableFrom(Collection.class);
    }

    public Object getValue(Object obj) {
        try {
            return field.get(obj);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    public void setValue(Object o, Object val) {
        FieldAccessor.setField(o, val, field);
    }
}
