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

package com.alesharik.database.old.postgres;

import com.alesharik.database.old.EntityManager;
import com.alesharik.database.old.RefTable;
import lombok.Getter;
import lombok.SneakyThrows;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * key(UUID): value(V)
 */
final class PostgresRefTable<V> implements RefTable<V> {
    private final Connection connection;
    @Getter
    private final String name;
    private final Class<V> clazz;
    private final PostgresDriver driver;
    private final List<V> old = new CopyOnWriteArrayList<>();

    public PostgresRefTable(Connection connection, String name, Class<V> clazz, PostgresDriver driver) {
        this.connection = connection;
        this.name = name;
        this.clazz = clazz;
        this.driver = driver;
    }

    @SneakyThrows
    @Override
    public List<V> select(UUID key) {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement("SELECT * FROM " + name + " WHERE key = ?");
            preparedStatement.setObject(1, driver.translate(key));
            resultSet = preparedStatement.executeQuery();
            List<V> ret = new ArrayList<>();
            while(resultSet.next()) {
                ret.add(EntityManager.parseEntity(resultSet, driver, clazz));
            }
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
    }

    @SneakyThrows
    @Override
    public void update(UUID key, List<V> arr) {
        PreparedStatement preparedStatement = null;
        Array array = null;
        try {
            List<V> oldCopy = new ArrayList<>(old);
            List<V> copy = new ArrayList<>(arr);
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
                PreparedStatement statement = connection.prepareStatement("UPDATE " + name + " SET value = ? WHERE key = ? AND value = ?");
                try {
                    for(int i = 0; i < updateCount; i++) {
                        V next = iter.next();
                        V old = oldIter.next();
                        iter.remove();
                        oldIter.remove();
                        statement.setObject(1, driver.translate(next));
                        statement.setObject(2, driver.translate(key));
                        statement.setObject(3, driver.translate(old));
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
                PreparedStatement statement = connection.prepareStatement("DELETE FROM " + name + " WHERE key = ? AND value = ?");
                while(oldIter.hasNext()) {
                    V next = oldIter.next();
                    statement.setObject(1, driver.translate(key));
                    statement.setObject(2, driver.translate(next));
                    statement.addBatch();
                }
                statement.executeBatch();
            }
            if(copy.size() > oldCopy.size()) {//Insert required
                PreparedStatement statement = connection.prepareStatement("INSERT INTO " + name + "(key, value) VALUES (?, ?)");
                while(iter.hasNext()) {
                    V next = iter.next();
                    statement.setObject(1, driver.translate(key));
                    statement.setObject(2, driver.translate(next));
                    statement.addBatch();
                }
                statement.executeBatch();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(preparedStatement != null)
                preparedStatement.close();
            if(array != null)
                array.free();
        }
    }
}
