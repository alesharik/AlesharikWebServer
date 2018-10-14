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
import com.alesharik.database.data.EntityPreparedStatement;
import com.alesharik.database.data.Schema;
import com.alesharik.database.data.Table;
import com.alesharik.database.entity.ForeignKey;
import com.alesharik.database.entity.asm.EntityColumn;
import com.alesharik.database.entity.asm.EntityDescription;
import com.alesharik.database.exception.DatabaseCloseSQLException;
import com.alesharik.database.exception.DatabaseInternalException;
import com.alesharik.database.exception.DatabaseReadSQLException;
import com.alesharik.database.exception.DatabaseStoreSQLException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

final class PostgresTable<E> implements Table<E> {
    private final ConnectionProvider connection;
    @Getter
    private final String name;
    @Getter
    private final Schema schema;
    @Getter(AccessLevel.PACKAGE)
    private final EntityDescription entityDescription;
    private final Map<EntityColumn, ArrayTable> arrays;

    public PostgresTable(ConnectionProvider connection, String name, Schema schema, EntityDescription entityDescription) {
        this.connection = connection;
        this.name = name;
        this.schema = schema;
        this.entityDescription = entityDescription;
        this.arrays = new ConcurrentHashMap<>();

        for(EntityColumn entityColumn : entityDescription.getColumns())
            if(entityColumn.isArray())
                this.arrays.put(entityColumn, new ArrayTable(connection, getArrayTableName(name, schema, entityColumn), getRealName(), entityColumn));
    }

    private PostgresTable(ConnectionProvider connection, String name, Schema schema, EntityDescription entityDescription, Map<EntityColumn, ArrayTable> arrays) {
        this.connection = connection;
        this.name = name;
        this.schema = schema;
        this.entityDescription = entityDescription;
        this.arrays = arrays;
    }

    public static <E> PostgresTable<E> create(ConnectionProvider connection, String name, Schema schema, EntityDescription entityDescription) {
        PreparedStatement statement = null;
        try {
            try {
                StringBuilder request = new StringBuilder("CREATE TABLE ");
                request.append(schema.getName());
                request.append('.');
                request.append(name);
                request.append('(');
                boolean notFirst = false;
                List<EntityColumn> primaryKeys = new ArrayList<>();
                Map<EntityColumn, ArrayTable> arrays = new HashMap<>();
                for(EntityColumn entityColumn : entityDescription.getColumns()) {
                    if(notFirst)
                        request.append(", ");
                    else
                        notFirst = true;

                    request.append(entityColumn.getColumnName());
                    request.append(' ');
                    if(entityColumn.isArray()) {
                        request.append(" uuid UNIQUE ");
                    } else { //Normal column
                        request.append(PostgresTypeTranslator.getColumnType(entityColumn));
                        if(entityColumn.isForeign()) {
                            request.append(" REFERENCES ");
                            request.append(entityColumn.getForeignTable());
                            if(!entityColumn.getForeignColumn().isEmpty()) {
                                request.append('(');
                                request.append(entityColumn.getForeignColumn());
                                request.append(')');
                            }
                            request.append(" ON DELETE ");
                            request.append(convertAction(entityColumn.getDeleteAction()));
                            request.append(" ON UPDATE ");
                            request.append(convertAction(entityColumn.getUpdateAction()));
                        }
                        request.append(entityColumn.isNullable() ? " NULL" : " NOT NULL");
                        if(entityColumn.isUnique())
                            request.append(" UNIQUE");
                        if(!entityColumn.getConstraint().isEmpty()) {
                            if(!entityColumn.getConstraintName().isEmpty()) {
                                request.append(" CONSTRAINT ");
                                request.append(entityColumn.getConstraintName());
                            }
                            request.append(" CHECK( ");
                            request.append(entityColumn.getConstraint());
                            request.append(" )");
                        }
                    }
                    if(entityColumn.isPrimary())
                        primaryKeys.add(entityColumn);
                }
                if(primaryKeys.size() > 0) {
                    request.append(", PRIMARY KEY (");
                    boolean notFirst1 = false;
                    for(EntityColumn primaryKey : primaryKeys) {
                        if(notFirst1)
                            request.append(", ");
                        else
                            notFirst1 = true;
                        request.append(primaryKey.getColumnName());
                    }
                    request.append(')');
                }

                request.append(");");
                statement = connection.getConnection().prepareStatement(request.toString());
                statement.executeUpdate();
                for(EntityColumn entityColumn : entityDescription.getColumns()) {
                    if(entityColumn.isArray()) {
                        String arrayTableName = getArrayTableName(name, schema, entityColumn);
                        ArrayTable arrayTable = ArrayTable.create(connection, arrayTableName, entityColumn, schema.getName() + '.' + name);
                        arrays.put(entityColumn, arrayTable);
                    } else if(entityColumn.isIndexed()) {
                        createIndex(connection, entityColumn, schema.getName() + '.' + name);
                    }
                }
                return new PostgresTable<>(connection, name, schema, entityDescription, arrays);
            } catch (SQLException e) {
                throw new DatabaseStoreSQLException(e);
            } finally {
                if(statement != null)
                    statement.close();
            }
        } catch (SQLException e) {
            throw new DatabaseCloseSQLException(e);
        }
    }

    private static String convertAction(ForeignKey.Action action) {
        switch (action) {
            case RESTRICT:
                return "RESTRICT";
            case CASCADE:
                return "CASCADE";
            case SET_NULL:
                return "SET NULL";
            case NO_ACTION:
                return "NO ACTION";
            case SET_DEFAULT:
                return "SET DEFAULT";
            default:
                return "";
        }
    }

    private static String getArrayTableName(String name, Schema schema, EntityColumn entityColumn) {
        return schema.getName() + '.' + name + "_array_" + entityColumn.getColumnName();
    }

    private static void createIndex(ConnectionProvider connection, EntityColumn column, String tableName) {
        if(!column.isIndexed())
            return;
        PreparedStatement statement = null;
        try {
            try {
                statement = connection.getConnection().prepareStatement("CREATE UNIQUE INDEX " + tableName.replaceAll("\\.", "_") + '_' + column.getColumnName() + "_idx ON " + tableName + '(' + column.getColumnName() + ')');
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new DatabaseReadSQLException(e);
            } finally {
                if(statement != null)
                    statement.close();
            }
        } catch (SQLException e) {
            throw new DatabaseCloseSQLException(e);
        }
    }

    @Override
    public E selectByPrimaryKey(E select) {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            try {
                preparedStatement = connection.getConnection().prepareStatement("SELECT * FROM " + getRealName() + " WHERE " + entityDescription.getWhereString());
                int i = 1;
                for(EntityColumn entityColumn : entityDescription.getPrimaryKey()) {
                    PostgresTypeTranslator.setObject(preparedStatement, i, entityColumn.getValue(select));
                    i++;
                }
                resultSet = preparedStatement.executeQuery();
                if(!resultSet.next())
                    return null;
                return PostgresTypeTranslator.parseEntity(connection, resultSet, entityDescription, this, arrays);
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            } finally {
                if(preparedStatement != null)
                    preparedStatement.close();
                if(resultSet != null)
                    resultSet.close();
            }
        } catch (SQLException e) {
            throw new DatabaseCloseSQLException(e);
        }
    }

    @Override
    public List<E> select(String request) {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            try {
                preparedStatement = connection.getConnection().prepareStatement(request);
                resultSet = preparedStatement.executeQuery();
                ArrayList<E> ret = new ArrayList<>();
                while(resultSet.next())
                    ret.add(PostgresTypeTranslator.parseEntity(connection, resultSet, entityDescription, this, arrays));
                return ret;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            } finally {
                if(preparedStatement != null)
                    preparedStatement.close();
                if(resultSet != null)
                    resultSet.close();
            }
        } catch (SQLException e) {
            throw new DatabaseCloseSQLException(e);
        }
    }

    @Override
    public List<E> select(int limit) {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            try {
                preparedStatement = connection.getConnection().prepareStatement("SELECT * FROM " + getRealName() + (limit > 0 ? " LIMIT ?" : ""));
                if(limit > 0)
                    preparedStatement.setInt(1, limit);
                resultSet = preparedStatement.executeQuery();
                ArrayList<E> ret = new ArrayList<>();
                while(resultSet.next())
                    ret.add(PostgresTypeTranslator.parseEntity(connection, resultSet, entityDescription, this, arrays));
                return ret;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            } finally {
                if(preparedStatement != null)
                    preparedStatement.close();
                if(resultSet != null)
                    resultSet.close();
            }
        } catch (SQLException e) {
            throw new DatabaseCloseSQLException(e);
        }
    }

    @Override
    public List<E> select(int limit, String sortBy, boolean desc) {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            try {
                preparedStatement = connection.getConnection().prepareStatement("SELECT * FROM " + getRealName() + (limit > 0 ? " LIMIT ?" : "") + " ORDER BY " + sortBy + (desc ? " DESC " : " ASC "));
                if(limit > 0)
                    preparedStatement.setInt(1, limit);
                resultSet = preparedStatement.executeQuery();
                ArrayList<E> ret = new ArrayList<>();
                while(resultSet.next())
                    ret.add(PostgresTypeTranslator.parseEntity(connection, resultSet, entityDescription, this, arrays));
                return ret;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            } finally {
                if(preparedStatement != null)
                    preparedStatement.close();
                if(resultSet != null)
                    resultSet.close();
            }
        } catch (SQLException e) {
            throw new DatabaseCloseSQLException(e);
        }
    }

    @Override
    public EntityPreparedStatement<E> prepareStatement(String statement) {
        try {
            return new PostgresEntityPreparedStatement<>(connection, connection.getConnection().prepareStatement(statement), entityDescription, this, arrays);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void drop(boolean sure) {
        if(sure) {
            System.out.println("Dropping table " + schema.getName() + '.' + name);
            PreparedStatement statement = null;
            try {
                try {
                    statement = connection.getConnection().prepareStatement("DROP TABLE " + name);
                    statement.executeUpdate();
                } catch (SQLException e) {
                    throw new DatabaseReadSQLException(e);
                } finally {
                    if(statement != null)
                        statement.close();
                }
            } catch (SQLException e) {
                throw new DatabaseCloseSQLException(e);
            }
        }
    }

    @Override
    public E createEntity(E entity, EntityDescription description) {
        PreparedStatement statement = null;
        try {
            try {
                statement = connection.getConnection().prepareStatement("INSERT INTO " + getRealName() + "(" + description.getColumnList() + ") VALUES (" + QuestionMarkGenerator.getQuestionMarks(description.getColumns().size()) + ")");
                int i = 1;
                for(EntityColumn entityColumn : description.getColumns()) {
                    if(entityColumn.isArray())
                        PostgresTypeTranslator.setObject(statement, i, UUID.randomUUID());
                    else
                        PostgresTypeTranslator.setObject(statement, i, entityColumn.getValue(entity));
                    i++;
                }
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new DatabaseReadSQLException(e);
            } finally {
                if(statement != null)
                    statement.close();
            }
        } catch (SQLException e) {
            throw new DatabaseCloseSQLException(e);
        }
        return entity;
    }

    @Override
    public void deleteEntity(E entity, EntityDescription description) {
        PreparedStatement statement = null;
        try {
            try {
                statement = connection.getConnection().prepareStatement("DELETE FROM " + getRealName() + " WHERE " + description.getWhereString());
                int i = 1;
                for(EntityColumn entityColumn : description.getPrimaryKey()) {
                    PostgresTypeTranslator.setObject(statement, i, entityColumn.getValue(entity));
                    i++;
                }
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new DatabaseReadSQLException(e);
            } finally {
                if(statement != null)
                    statement.close();
            }
        } catch (SQLException e) {
            throw new DatabaseCloseSQLException(e);
        }
    }

    @Override
    public void updateEntity(E entity, String field, EntityDescription description, Object fieldObj) {
        EntityColumn column = description.getColumn(field);
        if(column == null)
            return;

        if(column.isArray()) {
            arrays.get(column).updateValue(entity, description, fieldObj);
        } else {
            PreparedStatement statement = null;
            try {
                try {
                    statement = connection.getConnection().prepareStatement("UPDATE " + getRealName() + " SET " + column.getColumnName() + " = ? WHERE " + description.getWhereString());
                    PostgresTypeTranslator.setObject(statement, 1, fieldObj);
                    int i = 2;
                    for(EntityColumn entityColumn : description.getPrimaryKey()) {
                        PostgresTypeTranslator.setObject(statement, i, entityColumn.getValue(entity));
                        i++;
                    }
                    statement.executeUpdate();
                } catch (SQLException e) {
                    throw new DatabaseReadSQLException(e);
                } finally {
                    if(statement != null)
                        statement.close();
                }
            } catch (SQLException e) {
                throw new DatabaseCloseSQLException(e);
            }
        }
    }

    @SneakyThrows
    @Override
    public Object getEntityField(E entity, String field, EntityDescription description) {
        EntityColumn column = description.getColumn(field);
        if(column.isArray() && column.getField().getType().isAssignableFrom(Collection.class))
            return arrays.get(column).getValue(entity, description, column.getField().get(entity));
        else if(column.isArray())
            return arrays.get(column).getValue(entity, description, null);
        else {
            PreparedStatement statement = null;
            ResultSet resultSet = null;
            try {
                try {
                    statement = connection.getConnection().prepareStatement("SELECT " + column.getColumnName() + " FROM " + getRealName() + " WHERE " + description.getWhereString());
                    int i = 1;
                    for(EntityColumn entityColumn : description.getPrimaryKey()) {
                        PostgresTypeTranslator.setObject(statement, i, entityColumn.getValue(entity));
                        i++;
                    }
                    resultSet = statement.executeQuery();
                    if(!resultSet.next())
                        throw new DatabaseInternalException("Entity not found!");
                    return PostgresTypeTranslator.readObject(resultSet, column);
                } catch (SQLException e) {
                    throw new DatabaseReadSQLException(e);
                } finally {
                    if(statement != null)
                        statement.close();
                    if(resultSet != null)
                        resultSet.close();
                }
            } catch (SQLException e) {
                throw new DatabaseCloseSQLException(e);
            }
        }
    }

    private String getRealName() {
        return schema.getName() + '.' + name;
    }

    static final class ArrayTable {
        private final ConnectionProvider connection;
        private final String name;
        private final String parentTable;
        private final EntityColumn column;
        private final ArrayType arrayType;

        public ArrayTable(ConnectionProvider connection, String name, String parentTable, EntityColumn column) {
            this.connection = connection;
            this.name = name;
            this.parentTable = parentTable;
            this.column = column;
            if(column.getField().getType().isArray())
                arrayType = ArrayType.ARRAY;
            else if(column.getField().getType().isAssignableFrom(Collection.class))
                arrayType = ArrayType.COLLECTION;
            else
                arrayType = ArrayType.NOT_DEFINED;
        }

        public static ArrayTable create(ConnectionProvider connection, String name, EntityColumn column, String parentTable) {
            PreparedStatement statement = null;
            try {
                try {
                    StringBuilder valueColumnAdding = new StringBuilder();
                    if(column.isUnique()) {

                        valueColumnAdding.append(" UNIQUE ");
                    }
                    if(column.isForeign()) {
                        valueColumnAdding.append(" REFERENCES ");
                        valueColumnAdding.append(column.getForeignTable());
                        if(!column.getForeignColumn().isEmpty()) {
                            valueColumnAdding.append('(');
                            valueColumnAdding.append(column.getForeignColumn());
                            valueColumnAdding.append(')');
                        }
                    }
                    if(!column.getConstraint().isEmpty()) {
                        if(!column.getConstraintName().isEmpty()) {
                            valueColumnAdding.append(" CONSTRAINT ");
                            valueColumnAdding.append(column.getConstraintName());
                        }
                        valueColumnAdding.append(" CHECK( ");
                        valueColumnAdding.append(column.getConstraint());
                        valueColumnAdding.append(" )");
                    }
                    {
                        valueColumnAdding.append(column.isNullable() ? " NULL " : " NOT NULL ");
                    }
                    statement = connection.getConnection().prepareStatement("CREATE TABLE " + name + "(" +
                            "key uuid REFERENCES " + parentTable + "(" + column.getColumnName() + ")," +
                            "value " + PostgresTypeTranslator.getColumnType(column) + ' ' + valueColumnAdding + ", " +
                            "PRIMARY KEY (key, value)" +
                            ")");
                    statement.executeUpdate();
                    return new ArrayTable(connection, name, parentTable, column);
                } catch (SQLException e) {
                    throw new DatabaseStoreSQLException(e);
                } finally {
                    if(statement != null)
                        statement.close();
                }
            } catch (SQLException e) {
                throw new DatabaseCloseSQLException(e);
            }
        }

        /**
         * @param last required only if array is collection
         */
        @SuppressWarnings("unchecked")
        public Object getValue(Object entity, EntityDescription entityDescription, Object last) {
            if(arrayType == ArrayType.NOT_DEFINED)
                return null;

            if(arrayType == ArrayType.COLLECTION) {
                if(entityDescription.isBridge())
                    return new BridgeCollectionWrapper<>((Collection) last, this, entity, connection, entityDescription);
                else
                    return new LazyCollectionWrapper<>((Collection) last, this, entity, connection, entityDescription);
            }

            PreparedStatement statement = null;
            ResultSet resultSet = null;
            try {
                try {
                    statement = connection.getConnection().prepareStatement("SELECT * FROM " + name + " INNER JOIN " + parentTable + " ON " + name + ".key = " + parentTable + '.' + column.getColumnName() + " WHERE " + entityDescription.getWhereString());
                    int i = 1;
                    for(EntityColumn entityColumn : entityDescription.getPrimaryKey()) {
                        PostgresTypeTranslator.setObject(statement, i, entityColumn.getValue(entity));
                        i++;
                    }
                    resultSet = statement.executeQuery();

                    List ret = new ArrayList();
                    while(resultSet.next())
                        ret.add(PostgresTypeTranslator.readArrayObject(resultSet, column));

                    return ret.toArray();
                } catch (SQLException e) {
                    throw new DatabaseReadSQLException(e);
                } finally {
                    if(statement != null)
                        statement.close();
                    if(resultSet != null)
                        resultSet.close();
                }
            } catch (SQLException e) {
                throw new DatabaseCloseSQLException(e);
            }
        }

        public <V> void updateValue(Object entity, EntityDescription entityDescription, Object value) {
            V[] arr = (V[]) value;
            if(arrayType == ArrayType.ARRAY) {

                UUID id = getEntityArrayId(entity, entityDescription);
                try {
                    PreparedStatement preparedStatement = null;
                    Array array = null;
                    try {
                        List<V> oldCopy = Arrays.asList((V[]) getValue(entity, entityDescription, null));
                        List<V> copy = Arrays.asList(arr);

                        ListIterator<V> iter = copy.listIterator();
                        ListIterator<V> oldIter = oldCopy.listIterator();
                        while(iter.hasNext()) {//Remove all same elements
                            V next = iter.next();
                            if(oldCopy.contains(next)) {
                                iter.remove();
                                oldCopy.remove(next);
                            }
                        }
                        int updateCount = Math.min(oldCopy.size(), copy.size());
                        if(updateCount > 0) {//Can update smth
                            PreparedStatement statement = connection.getConnection().prepareStatement("UPDATE " + name + " SET value = ? WHERE key = ? AND value = ?");
                            try {
                                for(int i = 0; i < updateCount; i++) {
                                    V next = iter.next();
                                    V old = oldIter.next();
                                    iter.remove();
                                    oldIter.remove();
                                    PostgresTypeTranslator.setObject(statement, 1, next);
                                    PostgresTypeTranslator.setObject(statement, 2, id);
                                    PostgresTypeTranslator.setObject(statement, 3, old);
                                    statement.addBatch();
                                }
                                statement.executeBatch();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            } finally {
                                if(statement != null)
                                    statement.close();
                            }
                        }
                        if(oldCopy.size() > copy.size()) {//Delete required
                            PreparedStatement statement = null;
                            try {
                                statement = connection.getConnection().prepareStatement("DELETE FROM " + name + " WHERE key = ? AND value = ?");
                                while(oldIter.hasNext()) {
                                    V next = oldIter.next();
                                    PostgresTypeTranslator.setObject(statement, 1, id);
                                    PostgresTypeTranslator.setObject(statement, 2, next);
                                    statement.addBatch();
                                }
                                statement.executeBatch();
                            } catch (SQLException e) {
                                throw new DatabaseStoreSQLException(e);
                            } finally {
                                if(statement != null)
                                    statement.close();
                            }
                        }
                        if(copy.size() > oldCopy.size()) {//Insert required
                            PreparedStatement statement = null;
                            try {
                                statement = connection.getConnection().prepareStatement("INSERT INTO " + name + "(key, value) VALUES (?, ?)");
                                while(iter.hasNext()) {
                                    V next = iter.next();
                                    PostgresTypeTranslator.setObject(statement, 1, id);
                                    PostgresTypeTranslator.setObject(statement, 2, next);
                                    statement.addBatch();
                                }
                                statement.executeBatch();
                            } catch (SQLException e) {
                                throw new DatabaseStoreSQLException(e);
                            } finally {
                                if(statement != null)
                                    statement.close();
                            }

                        }
                    } catch (SQLException e) {
                        throw new DatabaseStoreSQLException(e);
                    } finally {
                        if(preparedStatement != null)
                            preparedStatement.close();
                        if(array != null)
                            array.free();

                    }
                } catch (SQLException e1) {
                    throw new DatabaseCloseSQLException(e1);
                }
            }
        }

        private UUID getEntityArrayId(Object entity, EntityDescription entityDescription) {
            PreparedStatement statement = null;
            ResultSet resultSet = null;
            try {
                try {
                    statement = connection.getConnection().prepareStatement("SELECT " + column.getColumnName() + " FROM " + parentTable + " WHERE " + entityDescription.getWhereString());
                    int i = 1;
                    for(EntityColumn entityColumn : entityDescription.getPrimaryKey()) {
                        PostgresTypeTranslator.setObject(statement, i, entityColumn.getValue(entity));
                        i++;
                    }
                    resultSet = statement.executeQuery();

                    if(!resultSet.next())
                        throw new DatabaseInternalException("Array table not found!");

                    return UUID.fromString(resultSet.getString(1));
                } catch (SQLException e) {
                    throw new DatabaseReadSQLException(e);
                } finally {
                    if(statement != null)
                        statement.close();
                    if(resultSet != null)
                        resultSet.close();
                }
            } catch (SQLException e) {
                throw new DatabaseCloseSQLException(e);
            }
        }

        private enum ArrayType {
            ARRAY,
            COLLECTION,
            NOT_DEFINED
        }

        static class BridgeCollectionWrapper<E> implements Collection<E> {
            protected final Collection<E> wrap;
            protected final ArrayTable table;
            protected final Object entity;
            protected final ConnectionProvider connection;
            protected final EntityDescription entityDescription;
            protected final UUID id;

            public BridgeCollectionWrapper(Collection<E> wrap, ArrayTable table, Object entity, ConnectionProvider connection, EntityDescription entityDescription) {
                this.connection = connection;
                this.entityDescription = entityDescription;
                if(wrap == null)
                    this.wrap = new ArrayList<>();
                else
                    this.wrap = wrap;
                this.table = table;
                this.entity = entity;
                this.id = getId();
            }

            protected UUID getId() {
                PreparedStatement statement = null;
                ResultSet resultSet = null;
                try {
                    try {
                        statement = connection.getConnection().prepareStatement("SELECT " + table.column.getColumnName() + " FROM " + table.parentTable + " WHERE " + entityDescription.getWhereString());
                        int i = 1;
                        for(EntityColumn entityColumn : entityDescription.getPrimaryKey()) {
                            PostgresTypeTranslator.setObject(statement, i, entityColumn.getValue(entity));
                            i++;
                        }
                        resultSet = statement.executeQuery();

                        if(!resultSet.next())
                            throw new DatabaseInternalException("Array table not found!");

                        return UUID.fromString(resultSet.getString(1));
                    } catch (SQLException e) {
                        throw new DatabaseReadSQLException(e);
                    } finally {
                        if(statement != null)
                            statement.close();
                        if(resultSet != null)
                            resultSet.close();
                    }
                } catch (SQLException e) {
                    throw new DatabaseCloseSQLException(e);
                }
            }

            @Override
            public int size() {
                PreparedStatement statement = null;
                ResultSet resultSet = null;
                try {
                    try {
                        statement = connection.getConnection().prepareStatement("SELECT count(*) FROM " + table.name + " WHERE key = ?");
                        statement.setObject(1, id);
                        resultSet = statement.executeQuery();

                        if(!resultSet.next())
                            throw new DatabaseInternalException("`SELECT count()` request returns nothing");

                        return resultSet.getInt(1);
                    } catch (SQLException e) {
                        throw new DatabaseReadSQLException(e);
                    } finally {
                        if(statement != null)
                            statement.close();
                        if(resultSet != null)
                            resultSet.close();
                    }
                } catch (SQLException e) {
                    throw new DatabaseCloseSQLException(e);
                }
            }

            @Override
            public boolean isEmpty() {
                PreparedStatement statement = null;
                ResultSet resultSet = null;
                try {
                    try {
                        statement = connection.getConnection().prepareStatement("SELECT exists(SELECT * FROM " + table.name + " WHERE key = ? LIMIT 1)");
                        statement.setObject(1, id);
                        resultSet = statement.executeQuery();

                        if(!resultSet.next())
                            throw new DatabaseInternalException("`SELECT exists()` request returns nothing");

                        return resultSet.getBoolean(1);
                    } catch (SQLException e) {
                        throw new DatabaseReadSQLException(e);
                    } finally {
                        if(statement != null)
                            statement.close();
                        if(resultSet != null)
                            resultSet.close();
                    }
                } catch (SQLException e) {
                    throw new DatabaseCloseSQLException(e);
                }
            }

            @Override
            public boolean contains(Object o) {
                PreparedStatement statement = null;
                ResultSet resultSet = null;
                try {
                    try {
                        statement = connection.getConnection().prepareStatement("SELECT exists(SELECT * FROM " + table.name + " WHERE key = ? AND value = ? LIMIT 1)");
                        statement.setObject(1, id);
                        PostgresTypeTranslator.setObject(statement, 2, o);
                        resultSet = statement.executeQuery();

                        if(!resultSet.next())
                            throw new DatabaseInternalException("`SELECT exists()` request returns nothing");

                        return resultSet.getBoolean(1);
                    } catch (SQLException e) {
                        throw new DatabaseReadSQLException(e);
                    } finally {
                        if(statement != null)
                            statement.close();
                        if(resultSet != null)
                            resultSet.close();
                    }
                } catch (SQLException e) {
                    throw new DatabaseCloseSQLException(e);
                }
            }

            @Override
            public Iterator<E> iterator() {
                return new Iter<>(toArray());
            }

            @SuppressWarnings("unchecked")
            @Override
            public Object[] toArray() {
                PreparedStatement statement = null;
                ResultSet resultSet = null;
                try {
                    try {
                        statement = connection.getConnection().prepareStatement("SELECT * FROM " + table.name + " WHERE key = ?");
                        statement.setObject(1, id);
                        resultSet = statement.executeQuery();

                        List ret = new ArrayList();
                        while(resultSet.next())
                            ret.add(PostgresTypeTranslator.readArrayObject(resultSet, table.column));

                        return ret.toArray();
                    } catch (SQLException e) {
                        throw new DatabaseReadSQLException(e);
                    } finally {
                        if(statement != null)
                            statement.close();
                        if(resultSet != null)
                            resultSet.close();
                    }
                } catch (SQLException e) {
                    throw new DatabaseCloseSQLException(e);
                }
            }

            @SuppressWarnings("unchecked")
            @Override
            public <T> T[] toArray(T[] a) {
                PreparedStatement statement = null;
                ResultSet resultSet = null;
                try {
                    try {
                        statement = connection.getConnection().prepareStatement("SELECT * FROM " + table.name + " WHERE key = ?");
                        statement.setObject(1, id);
                        resultSet = statement.executeQuery();

                        List<T> ret = new ArrayList<>();
                        while(resultSet.next())
                            ret.add((T) PostgresTypeTranslator.readArrayObject(resultSet, table.column));

                        return ret.toArray(a);
                    } catch (SQLException e) {
                        throw new DatabaseReadSQLException(e);
                    } finally {
                        if(statement != null)
                            statement.close();
                        if(resultSet != null)
                            resultSet.close();
                    }
                } catch (SQLException e) {
                    throw new DatabaseCloseSQLException(e);
                }
            }

            @Override
            public boolean add(E object) {
                PreparedStatement statement = null;
                try {
                    try {
                        statement = connection.getConnection().prepareStatement("INSERT INTO " + table.name + "(key, value) VALUES (?, ?)");
                        statement.setObject(1, id);
                        PostgresTypeTranslator.setObject(statement, 2, object);
                        statement.executeUpdate();

                        return wrap.add(object);
                    } catch (SQLException e) {
                        throw new DatabaseReadSQLException(e);
                    } finally {
                        if(statement != null)
                            statement.close();
                    }
                } catch (SQLException e) {
                    throw new DatabaseCloseSQLException(e);
                }
            }

            @Override
            public boolean remove(Object o) {
                PreparedStatement statement = null;
                try {
                    try {
                        statement = connection.getConnection().prepareStatement("DELETE FROM " + table.name + " WHERE key = ? AND value = ?");
                        statement.setObject(1, id);
                        PostgresTypeTranslator.setObject(statement, 2, o);
                        statement.executeUpdate();

                        return wrap.remove(o);
                    } catch (SQLException e) {
                        throw new DatabaseReadSQLException(e);
                    } finally {
                        if(statement != null)
                            statement.close();
                    }
                } catch (SQLException e) {
                    throw new DatabaseCloseSQLException(e);
                }
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                return isEmpty() ? c.isEmpty() : c.stream()
                        .map(this::contains)
                        .reduce((r, r2) -> r && r2)
                        .get();
            }

            @Override
            public boolean addAll(Collection<? extends E> c) {
                PreparedStatement statement = null;
                try {
                    try {
                        statement = connection.getConnection().prepareStatement("INSERT INTO " + table.name + "(key, value) VALUES (?, ?)");
                        for(E e : c) {
                            statement.setObject(1, id);
                            PostgresTypeTranslator.setObject(statement, 2, e);
                            statement.addBatch();
                        }

                        statement.executeBatch();
                        return wrap.addAll(c);
                    } catch (SQLException e) {
                        throw new DatabaseReadSQLException(e);
                    } finally {
                        if(statement != null)
                            statement.close();
                    }
                } catch (SQLException e) {
                    throw new DatabaseCloseSQLException(e);
                }
            }

            @Override
            public boolean removeAll(Collection<?> c) {
                PreparedStatement statement = null;
                try {
                    try {
                        statement = connection.getConnection().prepareStatement("DELETE FROM " + table.name + " WHERE key = ? AND value = ?");
                        for(Object obj : c) {
                            statement.setObject(1, id);
                            PostgresTypeTranslator.setObject(statement, 2, obj);
                            statement.executeBatch();
                        }

                        statement.executeUpdate();
                        return wrap.removeAll(c);
                    } catch (SQLException e) {
                        throw new DatabaseReadSQLException(e);
                    } finally {
                        if(statement != null)
                            statement.close();
                    }
                } catch (SQLException e) {
                    throw new DatabaseCloseSQLException(e);
                }
            }

            @Override
            public boolean retainAll(Collection<?> c) {
                Collection<Object> remove = new ArrayList<>();
                for(Object obj : toArray()) {
                    if(!c.contains(obj))
                        remove.add(obj);
                }
                return removeAll(remove);
            }

            @Override
            public void clear() {
                PreparedStatement statement = null;
                try {
                    try {
                        statement = connection.getConnection().prepareStatement("DELETE FROM " + table.name + " WHERE key = ?");
                        statement.setObject(1, id);

                        statement.executeUpdate();
                        wrap.clear();
                    } catch (SQLException e) {
                        throw new DatabaseReadSQLException(e);
                    } finally {
                        if(statement != null)
                            statement.close();
                    }
                } catch (SQLException e) {
                    throw new DatabaseCloseSQLException(e);
                }
            }

            static class Iter<E> implements Iterator<E> {
                private final Object[] list;
                private int cursor = 0;

                public Iter(Object[] list) {
                    this.list = list;
                }

                @Override
                public boolean hasNext() {
                    return cursor < list.length;
                }

                @SuppressWarnings("unchecked")
                @Override
                public E next() {
                    try {
                        return (E) list[cursor];
                    } finally {
                        cursor++;
                    }
                }
            }
        }

        static final class LazyCollectionWrapper<E> extends BridgeCollectionWrapper<E> {
            private final AtomicInteger countCache = new AtomicInteger(-1);
            private final AtomicBoolean syncCaches = new AtomicBoolean(false);

            public LazyCollectionWrapper(Collection<E> wrap, ArrayTable table, Object entity, ConnectionProvider connection, EntityDescription entityDescription) {
                super(wrap, table, entity, connection, entityDescription);
            }

            @SuppressWarnings("unchecked")
            private void loadCache() {
                if(syncCaches.get())
                    return;
                wrap.clear();
                for(Object o : super.toArray())
                    wrap.add((E) o);
                syncCaches.set(true);
            }

            @Override
            public int size() {
                if(countCache.get() == -1) {
                    int cache = super.size();
                    if(!countCache.compareAndSet(-1, cache))
                        return countCache.get();
                }
                return countCache.get();
            }

            @Override
            public boolean isEmpty() {
                return size() > 0;
            }

            @Override
            public boolean contains(Object o) {
                loadCache();
                return wrap.contains(o);
            }

            @Override
            public Iterator<E> iterator() {
                loadCache();
                return wrap.iterator();//FIXME iterator not working with bd, only with wrapper
            }

            @Override
            public Object[] toArray() {
                loadCache();
                return wrap.toArray();
            }

            @Override
            public <T> T[] toArray(T[] a) {
                loadCache();
                return wrap.toArray(a);
            }

            @Override
            public boolean add(E e) {
                loadCache();
                return super.add(e);
            }

            @Override
            public boolean remove(Object o) {
                loadCache();
                return super.remove(o);
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                loadCache();
                return wrap.containsAll(c);
            }

            @Override
            public boolean addAll(Collection<? extends E> c) {
                loadCache();
                return super.addAll(c);
            }

            @Override
            public boolean removeAll(Collection<?> c) {
                loadCache();
                return super.removeAll(c);
            }

            @Override
            public boolean retainAll(Collection<?> c) {
                loadCache();
                return super.retainAll(c);
            }

            @Override
            public void clear() {
                if(!syncCaches.get())
                    syncCaches.set(true);
                super.clear();
            }
        }
    }
}
