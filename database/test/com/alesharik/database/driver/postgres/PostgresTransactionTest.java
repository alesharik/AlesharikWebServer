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

import com.alesharik.database.exception.DatabaseTransactionException;
import com.alesharik.database.transaction.Transaction;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PostgresTransactionTest {
    @Test
    public void getIDOk() throws SQLException {
        Connection connection = mock(Connection.class);
        when(connection.setSavepoint()).then(invocation -> {
            Savepoint savepoint = mock(Savepoint.class);
            when(savepoint.getSavepointId()).thenReturn(1234);
            return savepoint;
        });
        Transaction transaction = new PostgresTransaction(connection);
        assertEquals(1234, transaction.getId());
    }

    @Test(expected = DatabaseTransactionException.class)
    public void getIDError() throws SQLException {
        Connection connection = mock(Connection.class);
        when(connection.setSavepoint()).then(invocation -> {
            Savepoint savepoint = mock(Savepoint.class);
            when(savepoint.getSavepointId()).thenThrow(new SQLException());
            return savepoint;
        });
        Transaction transaction = new PostgresTransaction(connection);
        transaction.getId();
        fail();
    }

    @Test(expected = DatabaseTransactionException.class)
    public void createTransactionError() throws SQLException {
        Connection connection = mock(Connection.class);
        when(connection.setSavepoint()).thenThrow(new SQLException());
        new PostgresTransaction(connection);
        fail();
    }

    @Test
    public void commit() throws SQLException {
        Connection connection = mock(Connection.class);

        PostgresTransaction postgresTransaction = new PostgresTransaction(connection);
        Transaction child1 = postgresTransaction.newTransaction();
        Transaction child2 = postgresTransaction.newTransaction();
        Transaction child21 = child2.newTransaction();

        assertFalse(postgresTransaction.isCommited());
        assertFalse(postgresTransaction.isRolledBack());
        assertFalse(child1.isCommited());
        assertFalse(child1.isRolledBack());
        assertFalse(child2.isCommited());
        assertFalse(child2.isRolledBack());
        assertFalse(child21.isCommited());
        assertFalse(child21.isRolledBack());

        child1.commit();

        assertTrue(child1.isCommited());
        assertFalse(child1.isRolledBack());

        assertTrue(postgresTransaction.commit());

        assertTrue(postgresTransaction.isCommited());
        assertTrue(child1.isCommited());
        assertTrue(child2.isCommited());
        assertTrue(child21.isCommited());

        verify(connection, times(1)).commit();
        verify(connection, times(4)).releaseSavepoint(any());
    }

    @Test
    public void commitWithOneRollback() {
        Connection connection = mock(Connection.class);

        PostgresTransaction postgresTransaction = new PostgresTransaction(connection);
        Transaction child1 = postgresTransaction.newTransaction();
        Transaction child2 = postgresTransaction.newTransaction();
        Transaction child21 = child2.newTransaction();

        child21.rollback();
        assertTrue(child21.isRolledBack());

        assertFalse(postgresTransaction.commit());

        assertFalse(postgresTransaction.isCommited());

        assertTrue(postgresTransaction.isRolledBack());
        assertTrue(child1.isRolledBack());
        assertTrue(child2.isRolledBack());
    }

    @Test
    public void rollback() throws SQLException {
        Connection connection = mock(Connection.class);

        PostgresTransaction postgresTransaction = new PostgresTransaction(connection);
        Transaction child1 = postgresTransaction.newTransaction();
        Transaction child2 = postgresTransaction.newTransaction();
        Transaction child21 = child2.newTransaction();

        child1.rollback();

        assertTrue(child1.isRolledBack());
        assertFalse(postgresTransaction.isRolledBack());

        postgresTransaction.rollback();

        assertTrue(postgresTransaction.isRolledBack());
        assertTrue(child1.isRolledBack());
        assertTrue(child2.isRolledBack());
        assertTrue(child21.isRolledBack());

        verify(connection, times(4)).releaseSavepoint(any());
        verify(connection, times(4)).rollback(any());
    }

    @Test(expected = DatabaseTransactionException.class)
    public void rollbackWithException() throws SQLException {
        Connection connection = mock(Connection.class);
        doThrow(new SQLException()).when(connection).releaseSavepoint(any());

        PostgresTransaction postgresTransaction = new PostgresTransaction(connection);
        Transaction child1 = postgresTransaction.newTransaction();
        Transaction child2 = postgresTransaction.newTransaction();
        Transaction child21 = child2.newTransaction();

        postgresTransaction.rollback();

        fail();
    }

    @Test(expected = DatabaseTransactionException.class)
    public void commitExceptionOnReleaseSavepoint() throws SQLException {
        Connection connection = mock(Connection.class);
        doThrow(new SQLException()).when(connection).releaseSavepoint(any());

        PostgresTransaction postgresTransaction = new PostgresTransaction(connection);
        Transaction child1 = postgresTransaction.newTransaction();
        Transaction child2 = postgresTransaction.newTransaction();
        Transaction child21 = child2.newTransaction();

        postgresTransaction.commit();

        fail();
    }

    @Test(expected = DatabaseTransactionException.class)
    public void commitExceptionOnCommit() throws SQLException {
        Connection connection = mock(Connection.class);
        doThrow(new SQLException()).when(connection).commit();

        PostgresTransaction postgresTransaction = new PostgresTransaction(connection);
        Transaction child1 = postgresTransaction.newTransaction();
        Transaction child2 = postgresTransaction.newTransaction();
        Transaction child21 = child2.newTransaction();

        postgresTransaction.commit();

        fail();
    }
}