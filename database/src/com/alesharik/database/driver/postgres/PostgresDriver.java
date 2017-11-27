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

import com.alesharik.database.data.Schema;
import com.alesharik.database.driver.DatabaseDriver;
import com.alesharik.database.exception.DatabaseCloseSQLException;
import com.alesharik.database.exception.DatabaseInternalException;
import com.alesharik.database.exception.DatabaseReadSQLException;
import com.alesharik.database.exception.DatabaseStoreSQLException;
import com.alesharik.database.transaction.TransactionManager;
import com.alesharik.webserver.exception.error.UnexpectedBehaviorError;
import lombok.Getter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class PostgresDriver implements DatabaseDriver {
    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new UnexpectedBehaviorError("Postgres JDBC Driver not found! Please, check server installation!", e);
        }
    }

    private final List<Schema> schemas;
    private volatile Connection connection;
    private volatile String currentUser;
    @Getter
    private volatile TransactionManager transactionManager;

    public PostgresDriver() {
        schemas = new CopyOnWriteArrayList<>();
    }

    @Override
    public void init(Connection connection) {
        this.connection = connection;
        updateSchemas();
        updateCurrentUser();
        try {
            updateTransactional(!connection.getAutoCommit());
        } catch (SQLException e) {
            throw new DatabaseInternalException("Get auto commit failed", e);
        }
    }

    @Override
    public Schema getSchema(String name, boolean createIfNotExists) {
        for(Schema schema : schemas) {
            if(schema.getName().equals(name))
                return schema;
        }
        if(createIfNotExists) {
            Schema schema = new PostgresSchema(connection, name, currentUser);
            createSchema(schema);
            schemas.add(schema);
            return schema;
        } else
            return null;
    }

    @Override
    public Schema[] getSchemas() {
        return schemas.toArray(new Schema[0]);
    }

    @Override
    public void update() {
        updateSchemas();
        updateCurrentUser();
    }

    @Override
    public String getCurrentUser() {
        return currentUser;
    }

    @Override
    public void updateTransactional(boolean is) {
        if(is)
            transactionManager = new PostgresTransactionManager(connection);
        else
            transactionManager = null;
    }

    private void createSchema(Schema schema) {
        PreparedStatement statement = null;
        try {
            try {
                statement = connection.prepareStatement("CREATE SCHEMA " + schema.getName() + " AUTHORIZATION " + schema.getOwner());
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new DatabaseStoreSQLException(e);
            } finally {
                if(statement != null)
                    statement.close();
            }
        } catch (SQLException e) {
            throw new DatabaseCloseSQLException(e);
        }
    }

    private void updateCurrentUser() {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            try {
                statement = connection.prepareStatement("SELECT current_user");
                resultSet = statement.executeQuery();
                if(!resultSet.next())
                    throw new DatabaseInternalException("`SELECT current_user` database request return 0 lines");
                currentUser = resultSet.getString(1);
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

    private void updateSchemas() {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            try {
                statement = connection.prepareStatement("SELECT schema_name, schema_owner FROM information_schema.schemata");
                resultSet = statement.executeQuery();
                List<Schema> ret = new ArrayList<>();
                while(resultSet.next()) {
                    ret.add(new PostgresSchema(connection, resultSet.getString("schema_name"), resultSet.getString("schema_owner")));
                }
                schemas.clear();
                schemas.addAll(ret);
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

}
