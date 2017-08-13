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

import com.alesharik.database.data.Schema;
import com.alesharik.database.driver.DatabaseDriver;
import com.alesharik.database.exception.DatabaseCloseSQLException;
import com.alesharik.database.exception.DatabaseInternalException;
import com.alesharik.database.transaction.TransactionManager;
import lombok.Getter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * WARNING! Database has many ways to do SQL injection, so DO NOT EXPOSE IT!
 */
public class Database {
    @Getter
    protected final Connection connection;
    protected final DatabaseDriver databaseDriver;
    protected final AtomicBoolean transactional;

    protected Database(Connection connection, DatabaseDriver databaseDriver, boolean transactional) {
        this.connection = connection;
        this.databaseDriver = databaseDriver;
        this.transactional = new AtomicBoolean(transactional);
        setTransactional(transactional);
        this.databaseDriver.init(connection);
    }

    public static Database newDatabase(Connection connection, DatabaseDriver databaseDriver, boolean transactional) {
        return new Database(connection, databaseDriver, transactional);
    }

    public static Database newDatabase(Connection connection, DatabaseDriver databaseDriver) throws SQLException {
        return new Database(connection, databaseDriver, !connection.getAutoCommit());
    }

    public static Database newDatabase(String url, DatabaseDriver databaseDriver, boolean transactional) throws SQLException {
        Connection connection = DriverManager.getConnection(url);
        return new Database(connection, databaseDriver, transactional);
    }

    public static Database newDatabase(String url, String login, String password, DatabaseDriver databaseDriver, boolean transactional) throws SQLException {
        Connection connection = DriverManager.getConnection(url, login, password);
        return new Database(connection, databaseDriver, transactional);
    }

    public void setTransactional(boolean is) {
        transactional.set(is);
        try {
            connection.setAutoCommit(!is);
        } catch (SQLException e) {
            throw new DatabaseInternalException("Can't update connection commit option", e);
        }
    }

    public boolean isTransactional() {
        return transactional.get();
    }

    public Schema[] getSchemas() {
        return databaseDriver.getSchemas();
    }

    public Schema getSchema(String name, boolean createIfNotExists) {
        return databaseDriver.getSchema(name, createIfNotExists);
    }

    public String getCurrentUser() {
        return databaseDriver.getCurrentUser();
    }

    /**
     * Close database connection
     */
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new DatabaseCloseSQLException(e);
        }
    }

    public void update() {
        databaseDriver.update();
    }

    public TransactionManager getTransactionManager() {
        if(!isTransactional())
            throw new IllegalStateException("Database must be transactional!");
        else
            return databaseDriver.getTransactionManager();
    }
}
