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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Callable;

/**
 * Transaction manager manages all trnasactions
 */
public interface TransactionManager {
    /**
     * Execute runnable in transaction
     *
     * @param runnable the runnable
     * @return <code>true</code> - transaction commited, <code>false</code> - transaction rolled back
     * @throws com.alesharik.database.exception.DatabaseTransactionException if transaction cannot be opened or rolled back
     */
    boolean executeTransaction(@Nonnull Runnable runnable);

    /**
     * Execute callable in transaction
     *
     * @param cCallable the callable
     * @param <C>       the return type
     * @return <code>null</code> - transaction rolled back, result - transaction commited
     * @throws com.alesharik.database.exception.DatabaseTransactionException if transaction cannot be opened or rolled back
     */
    @Nullable
    <C> C executeTransaction(@Nonnull Callable<C> cCallable);

    /**
     * Create new transaction object
     *
     * @return the transaction object
     * @throws com.alesharik.database.exception.DatabaseTransactionException if transaction cannot be opened
     */
    @Nonnull
    Transaction newTransaction();
}
