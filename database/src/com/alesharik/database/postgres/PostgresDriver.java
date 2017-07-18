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

import com.alesharik.database.DBDriver;
import com.alesharik.database.Schema;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class PostgresDriver extends DBDriver {
    public PostgresDriver() {
    }

    @Override
    public void init() {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.execute("CREATE OR REPLACE FUNCTION get_schema(name TEXT, createNew BOOLEAN) RETURNS TABLE(schema_name TEXT, schema_owner TEXT) AS $$ " +
                    "DECLARE " +
                    "BEGIN " +
                    "SELECT * FROM information_schema.schemata WHERE schema_name = name LIMIT 1; " +
                    "IF NOT FOUND AND createNew THEN " +
                    "CREATE SCHEMA name; " +
                    "END IF; " +
                    "END; $$ LANGUAGE plpgsql;");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(statement != null)
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
        }
    }

    @Override
    public Schema[] getSchemas() throws SQLException {
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT schema_name, schema_owner FROM information_schema.schemata");
            List<Schema> schemas = new ArrayList<>();
            schemas.add(new PostgresSchema(this, resultSet.getString(1), resultSet.getString(2)));
            while(resultSet.next()) {
                schemas.add(new PostgresSchema(this, resultSet.getString(1), resultSet.getString(2)));
            }

            return schemas.toArray(new Schema[schemas.size()]);
        } finally {
            if(resultSet != null)
                resultSet.close();
            if(statement != null)
                statement.close();
        }
    }

    @Override
    public Schema getSchema(String name, boolean createIfNotExists) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement("SELECT schema_name, schema_owner FROM get_schema(?, ?)");
            statement.setString(1, name);
            statement.setBoolean(2, createIfNotExists);
            resultSet = statement.executeQuery();

            return new PostgresSchema(this, resultSet.getString(1), resultSet.getString(2));
        } finally {
            if(resultSet != null)
                resultSet.close();
            if(statement != null)
                statement.close();
        }
    }
}
