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

package com.alesharik.database.postgres;

import com.alesharik.database.EntityManager;
import com.alesharik.database.Table;
import com.alesharik.database.entity.TypeTranslator;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.concurrent.NotThreadSafe;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

@NotThreadSafe
final class PostgresTable<T> implements Table<T> {
    private final Connection connection;
    private final String name;
    private final String schemaName;
    private final Class<T> clazz;
    private final TypeTranslator typeTranslator;

    private final AtomicBoolean caching = new AtomicBoolean(true);
    private final AtomicBoolean cacheSync = new AtomicBoolean(false);
    private volatile T[] cache;

    public PostgresTable(Connection connection, String name, String schemaName, Class<T> clazz, TypeTranslator typeTranslator) {
        this.connection = connection;
        this.name = name;
        this.schemaName = schemaName;
        this.clazz = clazz;
        this.typeTranslator = typeTranslator;

        enableCaching();
    }

    @Override
    public T[] getEntities() {
        if(caching.get() && cacheSync.get())
            return cache.clone();
        if(caching.get()) {
            cache = getEntitiesInternal();
            return cache.clone();
        } else {
            return getEntitiesInternal();
        }
    }

    @SneakyThrows
    @Override
    public void add(T t) {
        PreparedStatement preparedStatement = null;
        try {
            String[] entityDatabaseColumns = EntityManager.getEntityDatabaseColumns(clazz);
            preparedStatement = connection.prepareStatement("INSERT INTO " + getName() + "(" + String.join(", ", entityDatabaseColumns) +
                    ") VALUES (" + getQuestionMarks(entityDatabaseColumns.length) + ")");
            Object[] serialized = EntityManager.serialiseEntity(typeTranslator, clazz, t);
            for(int i = 0; i < serialized.length; i++) {
                preparedStatement.setObject(1 + i, serialized[i]);
            }
            preparedStatement.executeUpdate();
            if(caching.get()) {
                cache = Arrays.copyOf(cache, cache.length + 1);
                cache[cache.length - 1] = t;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(preparedStatement != null)
                preparedStatement.close();
        }

    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    @Override
    public boolean contains(T t) {
        if(caching.get()) {
            if(!cacheSync.get()) {
                cache = getEntitiesInternal();
            }
            for(T t1 : cache) {
                if(t1.equals(t))
                    return true;
            }
            return false;
        } else {
            PreparedStatement preparedStatement = null;
            ResultSet resultSet = null;
            try {
                preparedStatement = connection.prepareStatement("SELECT exists(SELECT 1 FROM " + getName() + " WHERE ? = ?)");
                Object[] pair = EntityManager.getPrimaryKeyPair((Class<T>) t.getClass(), t, typeTranslator);
                preparedStatement.setString(1, (String) pair[0]);
                preparedStatement.setObject(2, pair[1]);
                resultSet = preparedStatement.executeQuery();
                resultSet.next();
                return resultSet.getBoolean(1);
            } finally {
                if(resultSet != null)
                    resultSet.close();
                if(preparedStatement != null)
                    preparedStatement.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    @Override
    public boolean remove(T t) {
        PreparedStatement preparedStatement = null;
        try {
            Object[] serial = EntityManager.serialiseEntity(typeTranslator, clazz, t);
            String[] fields = EntityManager.getEntityDatabaseColumns(clazz);

            preparedStatement = connection.prepareStatement("DELETE FROM ? WHERE " + getEqualsQuestionMarks(fields.length) + ";");
            preparedStatement.setString(1, getName());
            for(int i = 0, cursor = 2; i < fields.length; i++, cursor += 2) {
                preparedStatement.setString(cursor, fields[i]);
                preparedStatement.setObject(cursor + 1, serial[i]);
            }
            preparedStatement.executeUpdate();
            if(caching.get())
                ArrayUtils.removeElement(cache, t);
            return true;
        } finally {
            if(preparedStatement != null)
                preparedStatement.close();
        }
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    @Override
    public void update(T t) {
        PreparedStatement preparedStatement = null;
        try {
            Object[] serial = EntityManager.serialiseEntity(typeTranslator, clazz, t);
            preparedStatement = connection.prepareStatement("UPDATE ? SET (?) = (" + getQuestionMarks(serial.length) + ") WHERE ? = ?;");
            preparedStatement.setString(1, getName());
            Object[] pair = EntityManager.getPrimaryKeyPair((Class<T>) t.getClass(), t, typeTranslator);

            preparedStatement.setString(2, String.join(", ", EntityManager.getEntityDatabaseColumns(clazz)));
            int counter = 3;
            for(Object o : serial) {
                preparedStatement.setObject(counter, o);
                counter++;
            }

            preparedStatement.setString(counter, (String) pair[0]);
            preparedStatement.setObject(counter + 1, pair[1]);
            preparedStatement.executeUpdate();
            if(caching.get()) {
                cache[ArrayUtils.indexOf(cache, t)] = t;
            }
        } finally {
            if(preparedStatement != null)
                preparedStatement.close();
        }
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    @Override
    public T selectForKey(T incomplete) {
        if(caching.get()) {
            if(!cacheSync.get())
                cache = getEntitiesInternal();
            Object value = EntityManager.getPrimaryKeyPair((Class<T>) incomplete.getClass(), incomplete, typeTranslator)[1];
            for(T t : cache) {
                Object another = EntityManager.getPrimaryKeyPair((Class<T>) t.getClass(), t, typeTranslator)[1];
                if(another.equals(value))
                    return t;
            }
        } else {
            PreparedStatement preparedStatement = null;
            ResultSet resultSet = null;
            try {
                preparedStatement = connection.prepareStatement("SELECT * FROM ? WHERE ? = ?");
                Object[] vals = EntityManager.getPrimaryKeyPair((Class<T>) incomplete.getClass(), incomplete, typeTranslator);
                preparedStatement.setString(1, getName());
                preparedStatement.setString(2, (String) vals[0]);
                preparedStatement.setObject(3, vals[1]);
                resultSet = preparedStatement.executeQuery();
                resultSet.next();
                return EntityManager.parseEntity(resultSet, typeTranslator, (Class<T>) incomplete.getClass());
            } finally {
                if(preparedStatement != null)
                    preparedStatement.close();
                if(resultSet != null)
                    resultSet.close();
            }
        }
        return null;
    }

    @Override
    public void updateCache() {
        if(caching.get())
            cache = getEntitiesInternal();
    }

    @Override
    public void enableCaching() {
        caching.set(true);
        updateCache();
    }

    @Override
    public void disableCaching() {
        caching.set(false);
        cache = null;
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    private T[] getEntitiesInternal() {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement("SELECT * FROM " + getName());
            resultSet = preparedStatement.executeQuery();
            List<T> ret = new ArrayList<>();
            while(resultSet.next()) {
                ret.add(EntityManager.parseEntity(resultSet, typeTranslator, clazz));
            }
            return (T[]) ret.toArray();
        } finally {
            if(preparedStatement != null)
                preparedStatement.close();
            if(resultSet != null)
                resultSet.close();
        }

    }

    private String getQuestionMarks(int count) {
        return IntStream.range(0, count)
                .mapToObj(value -> "?")
                .reduce((s, s2) -> s + ", " + s2)
                .get();
    }

    private String getEqualsQuestionMarks(int count) {
        return IntStream.range(0, count)
                .mapToObj(value -> "? = ?")
                .reduce((s, s2) -> s + ", " + s2)
                .get();
    }

    private String getName() {
        return schemaName.isEmpty() ? name : schemaName + '.' + name;
    }
}
