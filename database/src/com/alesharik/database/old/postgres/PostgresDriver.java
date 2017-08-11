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

import com.alesharik.database.old.DBDriver;
import com.alesharik.database.old.EntityManager;
import com.alesharik.database.old.RefTable;
import com.alesharik.database.old.Schema;
import com.alesharik.database.old.Table;
import com.alesharik.database.old.entity.RefTableProvider;
import com.alesharik.database.old.entity.TypeTranslator;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.SneakyThrows;
import org.postgresql.util.PGobject;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.Reader;
import java.net.InetAddress;
import java.sql.Array;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class PostgresDriver extends DBDriver implements TypeTranslator, RefTableProvider {
    private static final Gson GSON = new Gson();

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new Error(e);
        }
    }
    public PostgresDriver() {
    }

    @Override
    public void init() {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.execute("CREATE OR REPLACE FUNCTION get_schema(schema_name_string TEXT, createnew BOOLEAN) RETURNS TABLE(schema_name information_schema.SQL_IDENTIFIER, schema_owner information_schema.SQL_IDENTIFIER)\n" +
                    "LANGUAGE plpgsql " +
                    "AS $$" +
                    "DECLARE " +
                    "BEGIN " +
                    "  IF NOT exists(SELECT * FROM information_schema.schemata WHERE information_schema.schemata.schema_name = schema_name_string LIMIT 1) AND createNew THEN " +
                    "    EXECUTE format('CREATE SCHEMA %I', schema_name_string); " +
                    "  END IF; " +
                    "  RETURN QUERY SELECT information_schema.schemata.schema_name, information_schema.schemata.schema_owner FROM information_schema.schemata WHERE information_schema.schemata.schema_name = schema_name_string LIMIT 1; " +
                    "END; " +
                    "$$;");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(statement != null)
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
        }
    }

    @Override
    public Schema[] getSchemas() throws SQLException {
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT schema_name, schema_owner FROM information_schema.schemata");
            List<Schema> schemas = new ArrayList<>();
            while(resultSet.next()) {
                schemas.add(new PostgresSchema(this, resultSet.getString(1), resultSet.getString(2), connection, this));
            }

            return schemas.toArray(new Schema[schemas.size()]);
        } finally {
            if(resultSet != null)
                resultSet.close();
            if(statement != null)
                statement.close();
        }
    }

    @Override
    public Schema getSchema(String name, boolean createIfNotExists) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement("SELECT schema_name, schema_owner FROM get_schema(?, ?)");
            statement.setString(1, name);
            statement.setBoolean(2, createIfNotExists);
            resultSet = statement.executeQuery();
            resultSet.next();

            return new PostgresSchema(this, resultSet.getString(1), resultSet.getString(2), connection, this);
        } finally {
            if(resultSet != null)
                resultSet.close();
            if(statement != null)
                statement.close();
        }
    }

    @SneakyThrows
    @Override
    public <T> Table<T> getTable(String name, Class<T> entity) {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement("SELECT EXISTS (" +
                    "   SELECT 1" +
                    "   FROM   information_schema.tables " +
                    "   WHERE  table_name = ?" +
                    "   );");
            preparedStatement.setString(1, name);
            resultSet = preparedStatement.executeQuery();
            resultSet.next();
            if(resultSet.getBoolean(1))
                return new PostgresTable<>(connection, name, "", entity, this);
            else
                return null;
        } finally {
            if(preparedStatement != null)
                preparedStatement.close();
            if(resultSet != null)
                resultSet.close();
        }
    }

    @Override
    public <T> Table<T> createTable(String name, Class<T> entity) {
        EntityManager.createEntityTable(connection, name, this, entity);
        return new PostgresTable<>(connection, name, "", entity, this);
    }

    @SneakyThrows
    @Override
    public <V> RefTable<V> createRefTable(String name, Class<V> value, boolean isForeign, String ref) {
        PreparedStatement preparedStatement = null;
        try {
            String realName = name + "_ref_table";
            preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + realName + " (" +
                    "key uuid," +
                    "value " + translate(value) + ((isForeign && !ref.isEmpty()) ? " REFERENCES " + ref + ' ' : ' ') +
                    " PRIMARY KEY (key, value) " +
                    ")");
            preparedStatement.executeUpdate();
            return new PostgresRefTable<>(connection, realName, value, this);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(preparedStatement != null)
                preparedStatement.close();
        }
        return null;
    }

    @Override
    public <V> void linkTable(RefTable<V> ref, String name) {

    }

    @Override
    public String translate(Class<?> testClazz) {
        String s = "";
        Class<?> clazz = testClazz.isArray() ? testClazz.getComponentType() : testClazz;
        if(testClazz == byte[].class || testClazz == Byte[].class)
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
        else if(clazz == String.class)
            s = "text";
        else if(clazz == Time.class)
            s = "timestamp";
        else if(clazz == UUID.class)
            s = "uuid";
        else if(clazz == Node.class)
            s = "xml";
        else if(clazz == Date.class)
            s = "date";

        if(testClazz.isArray() && !(testClazz == byte[].class || testClazz == Byte[].class))
            s += "[]";
        return s;
    }

    @Override
    public Class<?> translate(String s) {
        boolean isArray = s.endsWith("[]");
        if(isArray)
            s = s.substring(0, s.length() - 2);
        switch (s) {
            case "smallint":
                return isArray ? short[].class : short.class;
            case "integer":
                return isArray ? int[].class : int.class;
            case "bigint":
                return isArray ? long[].class : long.class;
            case "boolean":
                return isArray ? boolean[].class : boolean.class;
            case "bytea":
                return isArray ? byte[][].class : byte[].class;
            case "character":
                return isArray ? char[].class : char.class;
            case "double precision":
                return isArray ? double[].class : double.class;
            case "inet":
                return isArray ? InetAddress[].class : InetAddress.class;
            case "json":
                return isArray ? JsonObject[].class : JsonObject.class;
            case "real":
                return isArray ? float[].class : float.class;
            case "text":
                return isArray ? String[].class : String.class;
            case "uuid":
                return isArray ? UUID[].class : UUID.class;
            case "xml":
                return isArray ? Node[].class : Node.class;
            case "date":
                return isArray ? Date[].class : Date.class;
        }
        return null;
    }

    private static final JsonParser JSON_PARSER = new JsonParser();

    @SneakyThrows
    @Override
    public Object getObject(ResultSet resultSet, String name, Class<?> type) {
        Class<?> component = type.isArray() ? type.getComponentType() : type;
        if(component == byte[].class || component == Byte[].class) {
            Array array = resultSet.getArray(name);
            Object ret = array.getArray();
            array.free();
            return ret;
        }


        if(!type.isArray()) {
            if(type == short.class || type == Short.class) {
                return resultSet.getShort(name);
            } else if(type == int.class || type == Integer.class) {
                return resultSet.getInt(name);
            } else if(type == long.class || type == Long.class) {
                return resultSet.getLong(name);
            } else if(type == boolean.class || type == Boolean.class) {
                return resultSet.getBoolean(name);
            } else if(type == char.class || type == Character.class) {
                Reader reader = resultSet.getCharacterStream(name);
                char c = (char) reader.read();
                reader.close();
                return c;
            } else if(type == double.class || type == Double.class) {
                return resultSet.getDouble(name);
            } else if(type == float.class || type == Float.class) {
                return resultSet.getFloat(name);
            } else if(type == InetAddress.class) {
                return InetAddress.getByName(resultSet.getString(name));
            } else if(type == JsonObject.class) {
                String json = resultSet.getString(name);
                return type.cast(JSON_PARSER.parse(json));
            } else if(type == UUID.class) {
                return UUID.fromString(resultSet.getString(name));
            } else if(type == String.class) {
                return resultSet.getString(name);
            } else if(type == Time.class) {
                return resultSet.getTime(name);
            } else if(type == Date.class) {
                return resultSet.getDate(name);
            } else if(type == Node.class) {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                return dBuilder.parse(resultSet.getString(name));
            }
        } else {
            Array array = resultSet.getArray(name);
            Object ret = array.getArray();
            array.free();
            return ret;
        }
        return null;
    }

    @SneakyThrows
    @Override
    public Object translate(Object o) {
        if(o.getClass() == JsonObject.class) {
            PGobject pGobject = new PGobject();
            pGobject.setType("json");
            pGobject.setValue(GSON.toJson((JsonObject) o));
            return pGobject;
        }
        return o;
    }
}
