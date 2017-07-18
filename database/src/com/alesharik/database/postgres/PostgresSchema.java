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

package com.alesharik.database.postgres;

import com.alesharik.database.Schema;
import com.alesharik.database.Table;
import lombok.Getter;

@Getter
class PostgresSchema implements Schema {
    private final PostgresDriver driver;
    private final String name;
    private final String owner;

    PostgresSchema(PostgresDriver driver, String name, String owner) {
        this.driver = driver;
        this.name = name;
        this.owner = owner;
    }

    @Override
    public Table[] getTables() {
        return new Table[0];
    }

    @Override
    public Table getTable(String table) {
        return null;
    }
}
