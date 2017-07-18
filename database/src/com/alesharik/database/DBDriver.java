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

package com.alesharik.database;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class DBDriver {
    protected volatile Connection connection;

    public void init() {
    }

    final void setConnection(Connection connection) {
        this.connection = connection;
    }

    public abstract Schema[] getSchemas() throws SQLException;

    public abstract Schema getSchema(String name, boolean createIfNotExists) throws SQLException;
}
