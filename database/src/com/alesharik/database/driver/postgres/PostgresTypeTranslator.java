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

import com.alesharik.database.entity.EntityManager;
import com.alesharik.database.entity.asm.EntityClassTransformer;
import com.alesharik.database.entity.asm.EntityColumn;
import com.alesharik.database.entity.asm.EntityDescription;
import com.alesharik.database.exception.DatabaseInternalException;
import com.alesharik.webserver.internals.instance.ClassInstantiator;
import com.alesharik.webserver.internals.reflect.FieldAccessor;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.postgresql.util.PGobject;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.ParameterizedType;
import java.net.InetAddress;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

final class PostgresTypeTranslator {
    public static String getColumnType(EntityColumn column) {
        if(!column.getOverrideDomain().isEmpty())
            return column.getOverrideDomain();

        String s = "";
        Class<?> clazz = column.getField().getType();
        if(clazz.isAssignableFrom(Collection.class))
            clazz = ((Class<?>) ((ParameterizedType) column.getField().getGenericType()).getActualTypeArguments()[0]);
        if(clazz == byte[].class || clazz == Byte[].class || clazz == Blob.class)
            s = "bytea";
        else if(clazz == short.class || clazz == Short.class)
            s = "smallint";
        else if(clazz == int.class || clazz == Integer.class)
            s = "integer";
        else if(clazz == long.class || clazz == Long.class)
            s = "bigint";
        else if(clazz == boolean.class || clazz == Boolean.class)
            s = "boolean";
        else if(clazz == char.class || clazz == Character.class)
            s = "character";
        else if(clazz == double.class || clazz == Double.class)
            s = "double precision";
        else if(clazz == InetAddress.class)
            s = "inet";
        else if(clazz == JsonObject.class)
            s = "json";
        else if(clazz == float.class || clazz == Float.class)
            s = "real";
        else if(clazz == String.class || clazz == Clob.class)
            s = "text";
        else if(clazz == Time.class)
            s = "timestamp";
        else if(clazz == UUID.class)
            s = "uuid";
        else if(clazz == Node.class)
            s = "xml";
        else if(clazz == Date.class)
            s = "date";
        return s;
    }

    public static void setObject(PreparedStatement preparedStatement, int index, Object o) throws SQLException {
        if(o != null && o.getClass().isAssignableFrom(JsonObject.class)) {
            PGobject x = new PGobject();
            x.setType("json");
            x.setValue(o.toString());
            preparedStatement.setObject(index, x);
        } else
            preparedStatement.setObject(index, o);
    }

    private static final JsonParser JSON_PARSER = new JsonParser();

    /**
     * Do not pass arrays here!
     */
    public static Object readObject(ResultSet resultSet, EntityColumn column) throws SQLException {
        return readObject(resultSet, column, column.getColumnName());
    }

    private static Object readObject(ResultSet resultSet, EntityColumn column, String nameOverride) throws SQLException {
        Class<?> type = column.getField().getType();
        if(type.isAssignableFrom(Collection.class))
            type = ((Class<?>) ((ParameterizedType) column.getField().getGenericType()).getActualTypeArguments()[0]);
        if(type == short.class || type == Short.class) {
            return resultSet.getShort(nameOverride);
        } else if(type == int.class || type == Integer.class) {
            return resultSet.getInt(nameOverride);
        } else if(type == long.class || type == Long.class) {
            return resultSet.getLong(nameOverride);
        } else if(type == boolean.class || type == Boolean.class) {
            return resultSet.getBoolean(nameOverride);
        } else if(type == char.class || type == Character.class) {
            char c;
            try {
                Reader reader = resultSet.getCharacterStream(nameOverride);
                c = (char) reader.read();
                reader.close();
            } catch (IOException e) {
                throw new DatabaseInternalException("Error while parsing character stream!", e);
            }
            return c;
        } else if(type == double.class || type == Double.class) {
            return resultSet.getDouble(nameOverride);
        } else if(type == float.class || type == Float.class) {
            return resultSet.getFloat(nameOverride);
        } else if(type == InetAddress.class) {
            try {
                return InetAddress.getByName(resultSet.getString(nameOverride));
            } catch (IOException e) {
                throw new DatabaseInternalException("Error while parsing InetAddress!", e);
            }
        } else if(type == JsonObject.class) {
            String json = resultSet.getString(nameOverride);
            return type.cast(JSON_PARSER.parse(json));
        } else if(type == UUID.class) {
            return UUID.fromString(resultSet.getString(nameOverride));
        } else if(type == String.class) {
            return resultSet.getString(nameOverride);
        } else if(type == Time.class) {
            return resultSet.getTime(nameOverride);
        } else if(type == Date.class) {
            return resultSet.getDate(nameOverride);
        } else if(type == Node.class) {
            try {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                return dBuilder.parse(resultSet.getString(nameOverride));
            } catch (ParserConfigurationException | SAXException | IOException e) {
                throw new DatabaseInternalException("Exception while parsing XML string!", e);
            }
        } else if(type == Clob.class)
            return resultSet.getClob(nameOverride);
        else if(type == Blob.class)
            return resultSet.getBlob(nameOverride);
        return null;
    }

    /**
     * Read <code>value</code> column
     */
    public static Object readArrayObject(ResultSet resultSet, EntityColumn column) throws SQLException {
        return readObject(resultSet, column, "value");
    }

    @SuppressWarnings("unchecked")
    public static <E> E parseEntity(ResultSet resultSet, EntityDescription description, EntityManager<E> entityManager, Map<EntityColumn, PostgresTable.ArrayTable> arrays) throws SQLException {
        E instance = (E) ClassInstantiator.instantiate(description.getClazz());
        FieldAccessor.setField(instance, entityManager, EntityClassTransformer.ENTITY_MANAGER_FIELD_NAME);
        if(description.isLazy() || description.isBridge()) {//Load only primary key
            for(EntityColumn column : description.getPrimaryKey()) {
                if(column.isArray())
                    column.setValue(instance, (description.isBridge())
                            ? new PostgresTable.ArrayTable.BridgeCollectionWrapper<>(new ArrayList<>(), arrays.get(column), instance, resultSet.getStatement().getConnection(), description)
                            : new PostgresTable.ArrayTable.LazyCollectionWrapper<>(new ArrayList<>(), arrays.get(column), instance, resultSet.getStatement().getConnection(), description));
                else
                    column.setValue(instance, readObject(resultSet, column));
            }
            return instance;
        }

        for(EntityColumn column : description.getColumns()) {
            if(column.isArray())
                column.setValue(instance, new PostgresTable.ArrayTable.LazyCollectionWrapper<>(new ArrayList<>(), arrays.get(column), instance, resultSet.getStatement().getConnection(), description));
            else
                column.setValue(instance, readObject(resultSet, column));
        }
        return instance;
    }
}
