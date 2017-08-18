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

import com.alesharik.database.transaction.Transaction;
import com.alesharik.database.transaction.TransactionManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.concurrent.Callable;

final class PostgresTransactionManager implements TransactionManager {
    private final Connection connection;

    public PostgresTransactionManager(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void executeTransaction(Runnable runnable) {
        Savepoint savepoint = null;
        try {
            savepoint = connection.setSavepoint();
            runnable.run();
            connection.commit();
            connection.releaseSavepoint(savepoint);
        } catch (Exception e) {
            if(savepoint != null)
                try {
                    connection.rollback(savepoint);
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
        }
    }

    @Override
    public <C> C executeTransaction(Callable<C> cCallable) {
        Savepoint savepoint = null;
        C ret = null;
        try {
            savepoint = connection.setSavepoint();
            ret = cCallable.call();
            connection.commit();
            connection.releaseSavepoint(savepoint);
        } catch (Exception e) {
            if(savepoint != null)
                try {
                    connection.rollback(savepoint);
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
        }
        return ret;
    }

    @Override
    public Transaction newTransaction() {
        return new PostgresTransaction(connection);
    }
}
