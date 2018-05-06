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
import com.alesharik.database.exception.DatabaseTransactionException;
import com.alesharik.database.transaction.Transaction;
import com.alesharik.database.transaction.TransactionManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.UUID;
import java.util.concurrent.Callable;

final class PostgresTransactionManager implements TransactionManager {
    private final ConnectionProvider connection;

    public PostgresTransactionManager(ConnectionProvider connection) {
        this.connection = connection;
    }

    @Override
    public boolean executeTransaction(Runnable runnable) {
        Connection connection = this.connection.getConnection();

        Savepoint savepoint;
        try {
            savepoint = connection.setSavepoint(UUID.randomUUID().toString());
        } catch (SQLException e) {
            throw new DatabaseTransactionException(e);
        }

        try {
            runnable.run();

            Savepoint s = savepoint;
            savepoint = null;
            connection.releaseSavepoint(s);

            connection.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace(System.out);
            if(savepoint != null)
                try {
                    connection.rollback(savepoint);
                } catch (SQLException e1) {
                    throw new DatabaseTransactionException(e1);
                }
        }

        return false;
    }

    @Override
    public <C> C executeTransaction(Callable<C> cCallable) {
        Connection connection = this.connection.getConnection();

        Savepoint savepoint;
        try {
            savepoint = connection.setSavepoint(UUID.randomUUID().toString());
        } catch (SQLException e) {
            throw new DatabaseTransactionException(e);
        }

        try {
            C ret = cCallable.call();

            Savepoint s = savepoint;
            savepoint = null;
            connection.releaseSavepoint(s);

            connection.commit();
            return ret;
        } catch (Exception e) {
            e.printStackTrace(System.out);
            if(savepoint != null)
                try {
                    connection.rollback(savepoint);
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
        }

        return null;
    }

    @Override
    public Transaction newTransaction() {
        Connection connection = this.connection.getConnection();
        return new PostgresTransaction(connection);
    }
}
