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

package com.alesharik.database.exception;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.annotation.Nonnull;

@EqualsAndHashCode(callSuper = true)
@ToString
@Getter
public final class ConstraintViolationException extends DatabaseStoreException {
    private final String columnName;
    private final Object value;

    public ConstraintViolationException(@Nonnull String columnName, @Nonnull Object value) {
        if(columnName.isEmpty())
            throw new IllegalArgumentException("Column name can't be empty!");
        this.columnName = columnName;
        this.value = value;
    }

    @Override
    public String getMessage() {
        return "Column " + columnName + " tries to violate constraint rule with " + value + " value!";
    }

    @Override
    public Throwable getCause() {
        return null;
    }

    @Override
    public Throwable initCause(Throwable cause) {
        throw new UnsupportedOperationException();
    }
}
