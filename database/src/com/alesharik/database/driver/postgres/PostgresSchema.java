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

package com.alesharik.database.driver.postgres;

import com.alesharik.database.connection.ConnectionProvider;
import com.alesharik.database.data.Schema;
import com.alesharik.database.data.Table;
import com.alesharik.database.entity.asm.EntityClassManager;
import com.alesharik.database.entity.asm.EntityDescription;
import com.alesharik.database.exception.DatabaseCloseSQLException;
import com.alesharik.database.exception.DatabaseInternalException;
import com.alesharik.database.exception.DatabaseReadSQLException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.annotation.Nullable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ToString
@EqualsAndHashCode
final class PostgresSchema implements Schema {
    private final ConnectionProvider connection;
    @Getter
    private final String name;
    @Getter
    private final String owner;
    private final Map<String, PostgresTable<?>> tables;

    public PostgresSchema(ConnectionProvider connection, String name, String owner) {
        this.connection = connection;
        this.name = name;
        this.owner = owner;
        this.tables = new ConcurrentHashMap<>();
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> Table<T> getTable(String name, boolean createIfNotExists, Class<T> entity) {
        EntityDescription description = EntityClassManager.getEntityDescription(entity);
        if(tables.containsKey(name) && tables.get(name).getEntityDescription().equals(description))
            return (Table<T>) tables.get(name);
        if(tableExists(name)) {
            PostgresTable<T> value = new PostgresTable<>(connection, name, this, description);
            tables.put(name, value);
            return value;
        } else if(createIfNotExists) {
            PostgresTable<T> val = PostgresTable.create(connection, name, this, description);
            tables.put(name, val);
            return val;
        }
        return null;
    }

    @Override
    public boolean tableExists(String name) {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            try {
                statement = connection.getConnection().prepareStatement("SELECT EXISTS(SELECT 1 FROM information_schema.tables WHERE table_schema = ? AND table_name = ?)");
                statement.setString(1, this.name);
                statement.setString(2, name);
                resultSet = statement.executeQuery();
                if(!resultSet.next())
                    throw new DatabaseInternalException("`SELECT EXISTS(SELECT 1 FROM information_schema.tables WHERE table_schema = ? AND table_name = ?)` return 0 rows!");
                return resultSet.getBoolean(1);
            } catch (SQLException e) {
                throw new DatabaseReadSQLException(e);
            } finally {
                if(statement != null)
                    statement.close();
                if(resultSet != null)
                    resultSet.close();
            }
        } catch (SQLException e) {
            throw new DatabaseCloseSQLException(e);
        }
    }

    @Override
    public void update() {
        tables.clear();
    }
}
