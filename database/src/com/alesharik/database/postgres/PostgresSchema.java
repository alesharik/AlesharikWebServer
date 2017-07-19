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

import com.alesharik.database.EntityManager;
import com.alesharik.database.Schema;
import com.alesharik.database.Table;
import com.alesharik.database.entity.TypeTranslator;
import lombok.Getter;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Getter
class PostgresSchema implements Schema {
    private final PostgresDriver driver;
    private final String name;
    private final String owner;
    private final Connection connection;
    private final TypeTranslator typeTranslator;

    PostgresSchema(PostgresDriver driver, String name, String owner, Connection connection, TypeTranslator typeTranslator) {
        this.driver = driver;
        this.name = name;
        this.owner = owner;
        this.connection = connection;
        this.typeTranslator = typeTranslator;
    }

    @SneakyThrows
    @Override
    public <T> Table<T> getTable(String table, Class<T> entity) {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement("SELECT EXISTS (" +
                    "   SELECT 1" +
                    "   FROM   information_schema.tables " +
                    "   WHERE  table_schema = ?" +
                    "   AND    table_name = ?" +
                    "   );");
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, table);
            resultSet = preparedStatement.executeQuery();
            resultSet.next();
            if(resultSet.getBoolean(1))
                return new PostgresTable<>(connection, table, name, entity, typeTranslator);
            else
                return null;
        } finally {
            if(preparedStatement != null)
                preparedStatement.close();
            if(resultSet != null)
                resultSet.close();
        }
    }

    public <T> Table<T> newTable(String name, Class<T> entity) {
        EntityManager.createEntityTable(connection, this.name + name, typeTranslator, entity);
        return new PostgresTable<>(connection, name, this.name, entity, typeTranslator);
    }
}
