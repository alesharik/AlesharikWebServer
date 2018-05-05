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

import com.alesharik.database.cache.DatabaseCache;
import com.alesharik.database.connection.ConnectionProvider;
import com.alesharik.database.data.Schema;
import com.alesharik.database.driver.DatabaseDriver;
import com.alesharik.database.transaction.TransactionManager;
import com.alesharik.database.user.User;
import com.alesharik.database.user.UserManager;
import lombok.Getter;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * WARNING! Database has many ways to do SQL injection, so DO NOT EXPOSE IT!
 *///TODO add cache update system
public class Database {
    @Getter
    protected final ConnectionProvider connection;
    protected final DatabaseDriver databaseDriver;
    protected final AtomicBoolean transactional;

    protected Database(ConnectionProvider connection, DatabaseDriver databaseDriver, boolean transactional) {
        this.connection = connection;
        this.databaseDriver = databaseDriver;
        this.transactional = new AtomicBoolean(transactional);
        setTransactional(transactional);
    }

    public static Database newDatabase(ConnectionProvider connection, DatabaseDriver databaseDriver, boolean transactional) {
        return new Database(connection, databaseDriver, transactional);
    }

    public static Database newDatabase(ConnectionProvider connection, DatabaseDriver databaseDriver) {
        return new Database(connection, databaseDriver, connection.isTransactional());
    }

    public void init() {
        this.connection.init();
        this.databaseDriver.init(connection);
    }

    public void setTransactional(boolean is) {
        transactional.set(is);
        connection.setTransactional(is);
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

    public User getCurrentUser() {
        return databaseDriver.getUserManager().getMe();
    }

    public UserManager<?, ?, ?> getUserManager() {
        return databaseDriver.getUserManager();
    }

    /**
     * Close database connection
     */
    public void close() {
        connection.shutdown();
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

    public DatabaseCache<?> getCache() {
        return databaseDriver.getCache();
    }

    public void requestNewConnection() {
        connection.requestNewConnection();
    }

    public void requestDedicatedConnection() {
        connection.requestNewDedicatedConnection();
    }

    public void setTransactionIsolation(int isolation) {
        connection.setTransactionLevel(isolation);
    }

    public int getTransactionIsolation() {
        return connection.getTransactionLevel();
    }
}
