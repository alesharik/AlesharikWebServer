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

package com.alesharik.database.old.transaction;

import lombok.SneakyThrows;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

final class TransactionImpl implements Transaction {
    private final Connection connection;
    private final Savepoint savepoint;
    private final AtomicInteger state;
    private final List<Transaction> transactions;
    private final List<Synchronization> synchronizations;

    @SneakyThrows
    public TransactionImpl(Connection connection) {
        this.connection = connection;
        this.savepoint = connection.setSavepoint();
        this.state = new AtomicInteger(0);
        this.transactions = new CopyOnWriteArrayList<>();
        this.synchronizations = new CopyOnWriteArrayList<>();
    }

    @SneakyThrows
    public TransactionImpl(Connection connection, String name) {
        this.connection = connection;
        this.savepoint = connection.setSavepoint(name);
        this.state = new AtomicInteger(0);
        this.transactions = new CopyOnWriteArrayList<>();
        this.synchronizations = new CopyOnWriteArrayList<>();
    }


    @Override
    public void commit() {
        commitInternal();
        try {
            connection.commit();
        } catch (SQLException e) {
            rollback();
            throw new RuntimeException(e);
        }
        this.state.set(STATE_COMMITTED);
    }

    @Override
    public void rollback() {
        try {
            for(Synchronization synchronization : synchronizations) {
                try {
                    synchronization.beforeCompletion();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            connection.rollback(savepoint);
            rollbackInternal();
            for(Transaction transaction : transactions) {
                ((TransactionImpl) transaction).rollbackInternal();
            }
            for(Synchronization synchronization : synchronizations) {
                try {
                    synchronization.afterCompletion(Status.STATUS_ROLLEDBACK);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void rollbackInternal() {
        this.state.set(STATE_ROLLBACK);
    }

    private void commitInternal() {
        for(Synchronization synchronization : synchronizations) {
            try {
                synchronization.beforeCompletion();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for(Transaction transaction : transactions) {
            if(!transaction.isCommited()) {
                ((TransactionImpl) transaction).commitInternal();
            } else if(transaction.isRolledBack()) {
                for(Transaction transaction1 : transactions) {
                    transaction1.rollback();
                    for(Synchronization synchronization : synchronizations) {
                        try {
                            synchronization.afterCompletion(Status.STATUS_ROLLEDBACK);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                    this.state.set(STATE_ROLLBACK);
                }
                return;
            }
        }
        for(Synchronization synchronization : synchronizations) {
            try {
                synchronization.afterCompletion(Status.STATUS_COMMITTED);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        this.state.set(STATE_COMMITTED);
    }

    @Override
    public void registerSynchronization(Synchronization synchronization) {
        synchronizations.add(synchronization);
    }

    @Override
    public List<Transaction> getChild() {
        return Collections.unmodifiableList(transactions);
    }

    @Override
    public Transaction begin() {
        TransactionImpl transaction = new TransactionImpl(connection);
        transactions.add(transaction);
        return transaction;
    }

    @Override
    public Transaction begin(String name) {
        TransactionImpl transaction = new TransactionImpl(connection, name);
        transactions.add(transaction);
        return transaction;
    }

    @Override
    public void end(Transaction transaction) {
        if(transactions.contains(transaction)) {
            transaction.commit();
            transactions.remove(transaction);
        }
    }

    @Override
    public boolean isCommited() {
        return state.get() == STATE_COMMITTED;
    }

    @Override
    public boolean isRolledBack() {
        return state.get() == STATE_ROLLBACK;
    }

    private static final int STATE_COMMITTED = 1;
    private static final int STATE_ROLLBACK = 2;
}
