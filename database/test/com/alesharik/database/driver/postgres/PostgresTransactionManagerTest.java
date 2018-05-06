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
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.concurrent.Callable;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PostgresTransactionManagerTest {
    private static ConnectionProvider provider(Connection connection) {
        return new ConnectionProvider() {
            @Override
            public Connection getConnection() {
                return connection;
            }

            @Override
            public boolean isTransactional() {
                return true;
            }

            @Override
            public void setTransactional(boolean transactional) {

            }

            @Override
            public void shutdown() {

            }

            @Override
            public void requestNewConnection() {

            }

            @Override
            public void requestNewDedicatedConnection() {

            }

            @Override
            public int getTransactionLevel() {
                return Connection.TRANSACTION_REPEATABLE_READ;
            }

            @Override
            public void setTransactionLevel(int level) {

            }

            @Override
            public void init() {

            }
        };
    }

    @Test
    public void executeRunnableTransactionOK() throws SQLException {
        Connection connection = mock(Connection.class);
        when(connection.setSavepoint(anyString()))
                .thenReturn(mock(Savepoint.class));
        doAnswer(invocation -> {
            assertTrue(mockingDetails(invocation.getArgument(0)).isMock());
            return null;
        }).when(connection).releaseSavepoint(any());

        PostgresTransactionManager transactionManager = new PostgresTransactionManager(provider(connection));

        Runnable runnable = mock(Runnable.class);
        assertTrue(transactionManager.executeTransaction(runnable));

        verify(runnable, times(1)).run();
        verify(connection, times(1)).setSavepoint(anyString());
        verify(connection, times(1)).releaseSavepoint(any());
    }

    @Test(expected = DatabaseTransactionException.class)
    public void executeRunnableTransactionSetSavepointException() throws SQLException {
        Connection connection = mock(Connection.class);
        when(connection.setSavepoint(anyString())).thenThrow(new SQLException());

        PostgresTransactionManager manager = new PostgresTransactionManager(provider(connection));
        manager.executeTransaction(() -> {
        });

        fail();
    }

    @Test
    public void executeRunnableTransactionExecuteException() throws SQLException {
        Connection connection = mock(Connection.class);
        when(connection.setSavepoint(anyString()))
                .thenReturn(mock(Savepoint.class));
        doAnswer(invocation -> {
            assertTrue(mockingDetails(invocation.getArgument(0)).isMock());
            return null;
        }).when(connection).releaseSavepoint(any());
        doAnswer(invocation -> {
            assertTrue(mockingDetails(invocation.getArgument(0)).isMock());
            return true;
        }).when(connection).rollback(any());


        PostgresTransactionManager manager = new PostgresTransactionManager(provider(connection));

        assertFalse(manager.executeTransaction(((Runnable) () -> {
            throw new RuntimeException("a");
        })));

        verify(connection, times(1)).setSavepoint(anyString());
        verify(connection, times(1)).rollback(any());
        verify(connection, never()).commit();
    }

    @Test
    public void executeRunnableTransactionReleaseSavepointException() throws SQLException {
        Connection connection = mock(Connection.class);
        when(connection.setSavepoint(anyString()))
                .thenReturn(mock(Savepoint.class));
        doThrow(new SQLException()).when(connection).releaseSavepoint(any());
        doAnswer(invocation -> {
            assertTrue(mockingDetails(invocation.getArgument(0)).isMock());
            return true;
        }).when(connection).rollback(any());

        PostgresTransactionManager manager = new PostgresTransactionManager(provider(connection));
        Runnable runnable = mock(Runnable.class);
        try {
            assertFalse(manager.executeTransaction(runnable));
            fail();
        } catch (DatabaseTransactionException ignored) {

        }

        verify(connection, never()).commit();
        verify(connection, never()).rollback(any());
        verify(connection, times(1)).releaseSavepoint(any());
        verify(connection, times(1)).setSavepoint(anyString());
        verify(runnable, times(1)).run();
    }

    @Test
    public void executeRunnableTransactionCommitException() throws SQLException {
        Connection connection = mock(Connection.class);
        when(connection.setSavepoint(anyString()))
                .thenReturn(mock(Savepoint.class));
        doThrow(new SQLException()).when(connection).commit();
        doAnswer(invocation -> {
            assertTrue(mockingDetails(invocation.getArgument(0)).isMock());
            return true;
        }).when(connection).releaseSavepoint(any());

        PostgresTransactionManager manager = new PostgresTransactionManager(provider(connection));
        Runnable runnable = mock(Runnable.class);
        try {
            assertFalse(manager.executeTransaction(runnable));
            fail();
        } catch (DatabaseTransactionException ignored) {

        }

        verify(connection, times(1)).commit();
        verify(connection, never()).rollback(any());
        verify(connection, times(1)).releaseSavepoint(any());
        verify(connection, times(1)).setSavepoint(anyString());
        verify(runnable, times(1)).run();
    }

    @Test
    public void executeCallableTransactionOK() throws SQLException {
        Connection connection = mock(Connection.class);
        when(connection.setSavepoint(anyString()))
                .thenReturn(mock(Savepoint.class));
        doAnswer(invocation -> {
            assertTrue(mockingDetails(invocation.getArgument(0)).isMock());
            return null;
        }).when(connection).releaseSavepoint(any());

        PostgresTransactionManager transactionManager = new PostgresTransactionManager(provider(connection));

        assertEquals("a", transactionManager.executeTransaction(() -> "a"));

        verify(connection, times(1)).setSavepoint(anyString());
        verify(connection, times(1)).releaseSavepoint(any());
    }

    @Test(expected = DatabaseTransactionException.class)
    public void executeCallableTransactionSetSavepointException() throws SQLException {
        Connection connection = mock(Connection.class);
        when(connection.setSavepoint(anyString())).thenThrow(new SQLException());

        PostgresTransactionManager manager = new PostgresTransactionManager(provider(connection));
        manager.executeTransaction(() -> "");

        fail();
    }

    @Test
    public void executeCallableTransactionExecuteException() throws SQLException {
        Connection connection = mock(Connection.class);
        when(connection.setSavepoint(anyString()))
                .thenReturn(mock(Savepoint.class));
        doAnswer(invocation -> {
            assertTrue(mockingDetails(invocation.getArgument(0)).isMock());
            return null;
        }).when(connection).releaseSavepoint(any());
        doAnswer(invocation -> {
            assertTrue(mockingDetails(invocation.getArgument(0)).isMock());
            return true;
        }).when(connection).rollback(any());


        PostgresTransactionManager manager = new PostgresTransactionManager(provider(connection));

        assertNull(manager.executeTransaction((() -> { //Callable
            throw new RuntimeException("a");
        })));

        verify(connection, times(1)).setSavepoint(anyString());
        verify(connection, times(1)).rollback(any());
        verify(connection, never()).commit();
    }

    @Test
    public void executeCallableTransactionReleaseSavepointException() throws Exception {
        Connection connection = mock(Connection.class);
        when(connection.setSavepoint(anyString()))
                .thenReturn(mock(Savepoint.class));
        doThrow(new SQLException()).when(connection).releaseSavepoint(any());
        doAnswer(invocation -> {
            assertTrue(mockingDetails(invocation.getArgument(0)).isMock());
            return true;
        }).when(connection).rollback(any());

        PostgresTransactionManager manager = new PostgresTransactionManager(provider(connection));
        //noinspection unchecked
        Callable<String> runnable = mock(Callable.class);
        when(runnable.call()).thenReturn("a");
        try {
            assertNull(manager.executeTransaction(runnable));
            fail();
        } catch (DatabaseTransactionException ignored) {

        }

        verify(connection, never()).commit();
        verify(connection, never()).rollback(any());
        verify(connection, times(1)).releaseSavepoint(any());
        verify(connection, times(1)).setSavepoint(anyString());
        verify(runnable, times(1)).call();
    }

    @Test
    public void executeCallableTransactionCommitException() throws Exception {
        Connection connection = mock(Connection.class);
        when(connection.setSavepoint(anyString()))
                .thenReturn(mock(Savepoint.class));
        doThrow(new SQLException()).when(connection).commit();
        doAnswer(invocation -> {
            assertTrue(mockingDetails(invocation.getArgument(0)).isMock());
            return true;
        }).when(connection).releaseSavepoint(any());

        PostgresTransactionManager manager = new PostgresTransactionManager(provider(connection));
        //noinspection unchecked
        Callable<String> runnable = mock(Callable.class);
        when(runnable.call()).thenReturn("a");
        try {
            assertNull(manager.executeTransaction(runnable));
            fail();
        } catch (DatabaseTransactionException ignored) {

        }

        verify(connection, times(1)).commit();
        verify(connection, never()).rollback(any());
        verify(connection, times(1)).releaseSavepoint(any());
        verify(connection, times(1)).setSavepoint(anyString());
        verify(runnable, times(1)).call();
    }
}