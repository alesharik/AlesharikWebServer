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

import com.alesharik.database.exception.DatabaseConnectionFailedException;
import com.alesharik.database.exception.DatabaseExecutionException;
import com.alesharik.database.proxy.TransactionProxyFactory;
import com.alesharik.database.transaction.TransactionManager;
import com.alesharik.database.transaction.TransactionManagerImpl;
import com.alesharik.database.transaction.reflect.TransactionRunnable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Savepoint;

public class Database {
    protected final String dbUrl;
    protected final String user;
    protected final String password;
    protected final DBDriver dbDriver;

    protected final boolean transactional;

    protected volatile Connection connection;
    protected volatile TransactionManagerImpl transactionManager;

    public Database(String db, String user, String pass, DBDriver dbDriver, boolean transactional) {
        this.dbUrl = db;
        this.user = user;
        this.password = pass;
        this.dbDriver = dbDriver;
        this.transactional = transactional;
    }

    public Database(String db, DBDriver dbDriver, boolean transactional) {
        this.dbUrl = db;
        this.dbDriver = dbDriver;
        this.transactional = transactional;
        this.user = null;
        this.password = null;
    }

    public void connect() {
        try {
            if(user == null && password == null)
                connection = DriverManager.getConnection(dbUrl);
            else
                connection = DriverManager.getConnection(dbUrl, user, password);
            dbDriver.setConnection(connection);
            dbDriver.init();
            if(transactional)
                connection.setAutoCommit(false);
            transactionManager = new TransactionManagerImpl(connection);
        } catch (SQLException e) {
            throw new DatabaseConnectionFailedException(e);
        }
    }

    public void disconnect() {
        checkDbInit();
        try {
            transactionManager.commitAll();
            connection.close();
            connection = null;
            transactionManager = null;
        } catch (SQLException e) {
            throw new DatabaseConnectionFailedException(e);
        }
    }

    private void checkDbInit() {
        if(connection == null)
            throw new IllegalStateException();
    }

    public Schema[] getSchemas() {
        try {
            return dbDriver.getSchemas();
        } catch (SQLException e) {
            throw new DatabaseExecutionException(e);
        }
    }

    public Schema getSchema(String name, boolean createIfNotExists) {
        try {
            return dbDriver.getSchema(name, createIfNotExists);
        } catch (SQLException e) {
            throw new DatabaseExecutionException(e);
        }
    }

    public void executeTransaction(TransactionRunnable runnable) {
        if(!transactional)
            throw new IllegalStateException("Database is not transactional!");
        Savepoint savepoint = null;
        try {
            savepoint = connection.setSavepoint();
            runnable.run();
            connection.commit();
            connection.releaseSavepoint(savepoint);
        } catch (Throwable e) {
            if(savepoint != null)
                try {
                    connection.rollback(savepoint);
                } catch (SQLException e1) {
                    throw new DatabaseExecutionException(e1);
                }
        }
    }

    public TransactionManager getTransactionManager() {
        if(!transactional)
            throw new IllegalStateException();
        return transactionManager;
    }

    public <T, R extends T> T wrapTransaction(Class<T> iface, R real) {
        if(!transactional)
            throw new IllegalStateException();
        return TransactionProxyFactory.newTransactionProxy(iface, real, transactionManager);
    }
}
