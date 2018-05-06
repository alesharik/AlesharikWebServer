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

import com.alesharik.database.exception.DatabaseInternalException;
import com.alesharik.database.exception.DatabaseTransactionException;
import com.alesharik.database.transaction.Transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

final class PostgresTransaction implements Transaction {
    private final List<PostgresTransaction> children = new CopyOnWriteArrayList<>();
    private final Connection connection;
    private final Savepoint savepoint;
    private final BitSet state = new BitSet(3);

    public PostgresTransaction(Connection connection) {
        this(connection, false);
    }

    private PostgresTransaction(Connection connection, boolean children) {
        this.connection = connection;
        try {
            this.savepoint = connection.setSavepoint();
        } catch (SQLException e) {
            throw new DatabaseTransactionException("Can't create savepoint", e);
        }
        state.set(0, children);
    }

    @Override
    public Transaction newTransaction() {
        PostgresTransaction transaction = new PostgresTransaction(connection, true);
        children.add(transaction);
        return transaction;
    }

    @Override
    public boolean commit() {
        for(PostgresTransaction child : children) {
            if(child.isRolledBack()) {
                rollback();
                return false;
            } else if(child.isCommited())
                continue;
            if(!child.commit()) {
                rollback();
                return false;
            }
        }
        if(!state.get(0)) {
            try {
                connection.releaseSavepoint(savepoint);
                connection.commit();
            } catch (SQLException e) {
                throw new DatabaseInternalException("Can't commit", e);
            }
        }
        state.set(1, true);
        return true;
    }

    @Override
    public void rollback() {
        for(PostgresTransaction child : children) {
            if(child.isRolledBack())
                continue;
            child.rollback();
        }
        try {
            connection.rollback(savepoint);
        } catch (SQLException e) {
            throw new DatabaseInternalException("Rollback failed", e);
        }
        state.set(2, true);
    }

    @Override
    public boolean isCommited() {
        return state.get(1);
    }

    @Override
    public boolean isRolledBack() {
        return state.get(2);
    }

    @Override
    public int getId() {
        try {
            return savepoint.getSavepointId();
        } catch (SQLException e) {
            throw new DatabaseTransactionException(e);
        }
    }
}
