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

import com.alesharik.database.data.EntityPreparedStatement;
import com.alesharik.database.entity.EntityManager;
import com.alesharik.database.entity.asm.EntityDescription;
import com.alesharik.database.exception.DatabaseCloseSQLException;
import com.alesharik.database.exception.DatabaseReadSQLException;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLType;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

final class PostgresEntityPreparedStatement<E> implements EntityPreparedStatement<E> {
    private final PreparedStatement wrapper;
    private final EntityDescription entityDescription;
    private final EntityManager<E> entityManager;

    public PostgresEntityPreparedStatement(PreparedStatement wrapper, EntityDescription entityDescription, EntityManager<E> entityManager) {
        this.wrapper = wrapper;
        this.entityDescription = entityDescription;
        this.entityManager = entityManager;
    }

    @Override
    public List<E> executeEntityQuery() {
        ResultSet resultSet = null;
        try {
            try {
                wrapper.closeOnCompletion();

                List<E> ret = new ArrayList<>();
                resultSet = executeQuery();
                while(resultSet.next())
                    ret.add(PostgresTypeTranslator.parseEntity(resultSet, entityDescription, entityManager));
                return ret;
            } catch (SQLException e) {
                throw new DatabaseReadSQLException(e);
            } finally {
                if(resultSet != null)
                    resultSet.close();
            }
        } catch (SQLException e) {
            throw new DatabaseCloseSQLException(e);
        }
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        return wrapper.executeQuery();
    }

    @Override
    public int executeUpdate() throws SQLException {
        return wrapper.executeUpdate();
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        wrapper.setNull(parameterIndex, sqlType);
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        wrapper.setBoolean(parameterIndex, x);
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        wrapper.setByte(parameterIndex, x);
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        wrapper.setShort(parameterIndex, x);
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        wrapper.setInt(parameterIndex, x);
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        wrapper.setLong(parameterIndex, x);
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
        wrapper.setFloat(parameterIndex, x);
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        wrapper.setDouble(parameterIndex, x);
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        wrapper.setBigDecimal(parameterIndex, x);
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        wrapper.setString(parameterIndex, x);
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        wrapper.setBytes(parameterIndex, x);
    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
        wrapper.setDate(parameterIndex, x);
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        wrapper.setTime(parameterIndex, x);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        wrapper.setTimestamp(parameterIndex, x);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        wrapper.setAsciiStream(parameterIndex, x, length);
    }

    @Override
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        wrapper.setUnicodeStream(parameterIndex, x, length);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        wrapper.setBinaryStream(parameterIndex, x, length);
    }

    @Override
    public void clearParameters() throws SQLException {
        wrapper.clearParameters();
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        wrapper.setObject(parameterIndex, x, targetSqlType);
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        wrapper.setObject(parameterIndex, x);
    }

    @Override
    public boolean execute() throws SQLException {
        return wrapper.execute();
    }

    @Override
    public void addBatch() throws SQLException {
        wrapper.addBatch();
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        wrapper.setCharacterStream(parameterIndex, reader, length);
    }

    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {
        wrapper.setRef(parameterIndex, x);
    }

    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException {
        wrapper.setBlob(parameterIndex, x);
    }

    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {
        wrapper.setClob(parameterIndex, x);
    }

    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException {
        wrapper.setArray(parameterIndex, x);
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return wrapper.getMetaData();
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        wrapper.setDate(parameterIndex, x, cal);
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        wrapper.setTime(parameterIndex, x, cal);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        wrapper.setTimestamp(parameterIndex, x, cal);
    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        wrapper.setNull(parameterIndex, sqlType, typeName);
    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
        wrapper.setURL(parameterIndex, x);
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return wrapper.getParameterMetaData();
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        wrapper.setRowId(parameterIndex, x);
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        wrapper.setNString(parameterIndex, value);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        wrapper.setNCharacterStream(parameterIndex, value, length);
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        wrapper.setNClob(parameterIndex, value);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        wrapper.setClob(parameterIndex, reader, length);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        wrapper.setBlob(parameterIndex, inputStream, length);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        wrapper.setNClob(parameterIndex, reader, length);
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        wrapper.setSQLXML(parameterIndex, xmlObject);
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        wrapper.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        wrapper.setAsciiStream(parameterIndex, x, length);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        wrapper.setBinaryStream(parameterIndex, x, length);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        wrapper.setCharacterStream(parameterIndex, reader, length);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        wrapper.setAsciiStream(parameterIndex, x);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        wrapper.setBinaryStream(parameterIndex, x);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        wrapper.setCharacterStream(parameterIndex, reader);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        wrapper.setNCharacterStream(parameterIndex, value);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        wrapper.setClob(parameterIndex, reader);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        wrapper.setBlob(parameterIndex, inputStream);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        wrapper.setNClob(parameterIndex, reader);
    }

    @Override
    public void setObject(int parameterIndex, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        wrapper.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
    }

    @Override
    public void setObject(int parameterIndex, Object x, SQLType targetSqlType) throws SQLException {
        wrapper.setObject(parameterIndex, x, targetSqlType);
    }

    @Override
    public long executeLargeUpdate() throws SQLException {
        return wrapper.executeLargeUpdate();
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        return wrapper.executeQuery(sql);
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        return wrapper.executeUpdate(sql);
    }

    @Override
    public void close() throws SQLException {
        wrapper.close();
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        return wrapper.getMaxFieldSize();
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        wrapper.setMaxFieldSize(max);
    }

    @Override
    public int getMaxRows() throws SQLException {
        return wrapper.getMaxRows();
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
        wrapper.setMaxRows(max);
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        wrapper.setEscapeProcessing(enable);
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        return wrapper.getQueryTimeout();
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        wrapper.setQueryTimeout(seconds);
    }

    @Override
    public void cancel() throws SQLException {
        wrapper.cancel();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return wrapper.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        wrapper.clearWarnings();
    }

    @Override
    public void setCursorName(String name) throws SQLException {
        wrapper.setCursorName(name);
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        return wrapper.execute();
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        return wrapper.getResultSet();
    }

    @Override
    public int getUpdateCount() throws SQLException {
        return wrapper.getUpdateCount();
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        return wrapper.getMoreResults();
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return wrapper.getFetchDirection();
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        wrapper.setFetchDirection(direction);
    }

    @Override
    public int getFetchSize() throws SQLException {
        return wrapper.getFetchSize();
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        wrapper.setFetchSize(rows);
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        return wrapper.getResultSetConcurrency();
    }

    @Override
    public int getResultSetType() throws SQLException {
        return wrapper.getResultSetType();
    }

    @Override
    public void addBatch(String sql) throws SQLException {
        wrapper.addBatch(sql);
    }

    @Override
    public void clearBatch() throws SQLException {
        wrapper.clearBatch();
    }

    @Override
    public int[] executeBatch() throws SQLException {
        return wrapper.executeBatch();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return wrapper.getConnection();
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        return wrapper.getMoreResults(current);
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return wrapper.getGeneratedKeys();
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return wrapper.executeUpdate(sql, autoGeneratedKeys);
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return wrapper.executeUpdate(sql, columnIndexes);
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        return wrapper.executeUpdate(sql, columnNames);
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        return wrapper.execute(sql, autoGeneratedKeys);
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        return wrapper.execute(sql, columnIndexes);
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        return wrapper.execute(sql, columnNames);
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return wrapper.getResultSetHoldability();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return wrapper.isClosed();
    }

    @Override
    public boolean isPoolable() throws SQLException {
        return wrapper.isPoolable();
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        wrapper.setPoolable(poolable);
    }

    @Override
    public void closeOnCompletion() throws SQLException {
        wrapper.closeOnCompletion();
    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return wrapper.isCloseOnCompletion();
    }

    @Override
    public long getLargeUpdateCount() throws SQLException {
        return wrapper.getLargeUpdateCount();
    }

    @Override
    public long getLargeMaxRows() throws SQLException {
        return wrapper.getLargeMaxRows();
    }

    @Override
    public void setLargeMaxRows(long max) throws SQLException {
        wrapper.setLargeMaxRows(max);
    }

    @Override
    public long[] executeLargeBatch() throws SQLException {
        return wrapper.executeLargeBatch();
    }

    @Override
    public long executeLargeUpdate(String sql) throws SQLException {
        return wrapper.executeLargeUpdate(sql);
    }

    @Override
    public long executeLargeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return wrapper.executeLargeUpdate(sql, autoGeneratedKeys);
    }

    @Override
    public long executeLargeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return wrapper.executeLargeUpdate(sql, columnIndexes);
    }

    @Override
    public long executeLargeUpdate(String sql, String[] columnNames) throws SQLException {
        return wrapper.executeLargeUpdate(sql, columnNames);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return wrapper.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return wrapper.isWrapperFor(iface);
    }
}
