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

/**
 * Transaction object manages a transaction
 */
public interface Transaction {
    /**
     * Create new sub-transaction
     *
     * @return new sub-transaction
     * @throws com.alesharik.database.exception.DatabaseTransactionException if transaction cannot be opened or rolled back
     */
    @Nonnull
    Transaction newTransaction();

    /**
     * Commit current transaction and all sub-transactions
     * @return commit failed
     * @throws com.alesharik.database.exception.DatabaseTransactionException if transaction cannot be commited
     */
    boolean commit();

    /**
     * Rollback this transaction and all sub-transactions
     * @throws com.alesharik.database.exception.DatabaseTransactionException if transaction cannot be rolled back
     */
    void rollback();

    /**
     * Return <code>true</code> if transaction already commited
     *
     * @return <code>true</code> - transaction commited, <code>false</code> - transaction alive or rolled back
     */
    boolean isCommited();

    /**
     * Return <code>true</code> if transaction rolled back
     * @return <code>true</code> - transaction rolled back, <code>false</code> - transaction alive or commited
     */
    boolean isRolledBack();
}
