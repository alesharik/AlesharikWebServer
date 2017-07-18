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

package com.alesharik.database.transaction;

import javax.annotation.concurrent.ThreadSafe;
import java.sql.Connection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ThreadSafe
public class TransactionManagerImpl implements TransactionManager {
    private final Connection connection;
    private final List<Transaction> transactions;

    public TransactionManagerImpl(Connection connection) {
        this.connection = connection;
        this.transactions = new CopyOnWriteArrayList<>();
    }

    @Override
    public Transaction begin() {
        Transaction transaction = new TransactionImpl(connection);
        transactions.add(transaction);
        return transaction;
    }

    @Override
    public Transaction begin(String name) {
        Transaction transaction = new TransactionImpl(connection, name);
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
    public void commitAll() {
        transactions.forEach(Transaction::commit);
    }

    @Override
    public void rollbackAll() {
        transactions.forEach(Transaction::rollback);
    }
}
