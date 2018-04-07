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

package com.alesharik.database.connection;

import com.alesharik.database.exception.DatabaseCloseSQLException;
import com.alesharik.database.exception.DatabaseClosedException;
import com.alesharik.database.exception.DatabaseInternalException;
import com.alesharik.database.exception.DatabaseOpenException;
import lombok.Getter;
import lombok.Setter;
import sun.misc.Cleaner;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;

/**
 * Fixed free connections, thread-bound connections, non-blocking
 */
public final class FixedThreadBoundConnectionPool implements ConnectionProvider {
    private final String url;
    private final String login;
    private final String password;

    private final int maxConnections;
    private final ThreadLocal<Connection> connection;
    private final List<Connection> freeConnections;

    @Getter
    @Setter
    private volatile int transactionLevel = Connection.TRANSACTION_REPEATABLE_READ;
    @Getter
    @Setter
    private volatile boolean transactional = false;

    private volatile int state = 0;

    public FixedThreadBoundConnectionPool(String url, String login, String password, int maxConnections) {
        this.url = url;
        this.login = login;
        this.password = password;
        this.maxConnections = maxConnections;
        this.connection = new ThreadLocal<>();
        this.freeConnections = new CopyOnWriteArrayList<>();
    }

    public FixedThreadBoundConnectionPool(String url, int maxConnections) {
        this.url = url;
        this.login = "";
        this.password = "";
        this.maxConnections = maxConnections;
        this.connection = new ThreadLocal<>();
        this.freeConnections = new CopyOnWriteArrayList<>();
    }

    @Override
    public void init() {
        if(state != 0)
            throw new IllegalStateException();
        for(int i = 0; i < maxConnections; i++) {
            Connection connection = newConnection();
            freeConnections.add(connection);
        }
        state = 1;
    }

    private void reallocConnection() {
        if(state == 2)
            return;
        freeConnections.add(newConnection());
    }

    private void checkConnections() {
        if(freeConnections.size() > maxConnections)
            while(freeConnections.size() > maxConnections) {
                try {
                    Connection connection = freeConnections.remove(0);
                    connection.close();
                } catch (IndexOutOfBoundsException e) {
                    //ignore
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
    }

    private Connection newConnection() {
        try {
            Connection connection;
            if(login.isEmpty() && password.isEmpty())
                connection = DriverManager.getConnection(url);
            else
                connection = DriverManager.getConnection(url, login, password);
            connection.setAutoCommit(!transactional);
            //noinspection MagicConstant
            connection.setTransactionIsolation(transactionLevel);

            Cleaner.create(connection, () -> ForkJoinPool.commonPool().execute(this::reallocConnection));

            return connection;
        } catch (SQLException e) {
            throw new DatabaseOpenException(e);
        }
    }

    @Override
    public Connection getConnection() {
        if(state == 2)
            throw new DatabaseClosedException();
        else if(state == 0)
            throw new IllegalStateException("Not open!");
        if(connection.get() != null)
            return connection.get();
        else {
            Connection connection;
            try {
                connection = freeConnections.isEmpty() ? newConnection() : freeConnections.remove(0);
            } catch (IndexOutOfBoundsException e) {
                connection = newConnection();
            }
            this.connection.set(connection);
            return connection;
        }
    }

    @Override
    public void shutdown() {
        state = 2;
        for(Connection freeConnection : freeConnections)
            try {
                freeConnection.close();
            } catch (SQLException e) {
                throw new DatabaseCloseSQLException(e);
            }
    }

    @Override
    public void requestNewConnection() {
        if(state != 1)
            return;
        Connection current = connection.get();
        connection.set(null); //Set new connection on next request
        try {
            if(!current.isClosed() && current.isValid(1000)) {
                freeConnections.add(current);
                checkConnections();
            }
        } catch (SQLException e) {
            throw new DatabaseInternalException("Exception while checking if DB is alive", e);
        }
    }

    @Override
    public void requestNewDedicatedConnection() {
        if(state != 1)
            return;
        Connection current = connection.get();
        connection.set(null);
        try {
            if(!current.isClosed() && current.isValid(1000)) {
                freeConnections.add(current);
                checkConnections();
            }
        } catch (SQLException e) {
            throw new DatabaseInternalException("Exception while checking if DB is alive", e);
        }
        connection.set(newConnection());
    }
}
