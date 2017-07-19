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

package com.alesharik.database;

import com.alesharik.database.entity.Column;
import com.alesharik.database.entity.Ignore;
import com.alesharik.database.entity.TypeTranslator;
import lombok.experimental.UtilityClass;
import one.nio.util.JavaInternals;
import org.apache.commons.lang3.tuple.Pair;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@UtilityClass
public final class EntityManager {
    private static final Unsafe UNSAFE = JavaInternals.getUnsafe();

    private static final Map<Class<?>, Entity> entities = new ConcurrentHashMap<>();

    public static void createEntityTable(Connection connection, String name, TypeTranslator translator, Class<?> clazz) {
        if(entities.containsKey(clazz))
            entities.get(clazz).createTable(name, connection, translator);
        else {
            Entity entity = new Entity(clazz);
            entities.put(clazz, entity);
            entity.createTable(name, connection, translator);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T parseEntity(ResultSet resultSet, TypeTranslator typeTranslator, Class<T> entityClass) {
        if(entities.containsKey(entityClass))
            return (T) entities.get(entityClass).parse(resultSet, typeTranslator);
        else {
            Entity entity = new Entity(entityClass);
            entities.put(entityClass, entity);
            return (T) entity.parse(resultSet, typeTranslator);
        }
    }

    public static <T> Object[] serialiseEntity(TypeTranslator typeTranslator, Class<T> clazz, T entityInstance) {
        if(entities.containsKey(clazz))
            return entities.get(clazz).serializeEntity(entityInstance, typeTranslator);
        else {
            Entity entity = new Entity(clazz);
            entities.put(clazz, entity);
            return entity.serializeEntity(entityInstance, typeTranslator);
        }
    }

    public static String[] getEntityDatabaseColumns(Class<?> clazz) {
        if(entities.containsKey(clazz))
            return entities.get(clazz).getNames();
        else {
            Entity entity = new Entity(clazz);
            entities.put(clazz, entity);
            return entity.getNames();
        }
    }

    public static <T, R extends T> Object[] getPrimaryKeyPair(Class<T> clazz, R realisation, TypeTranslator typeTranslator) {
        if(entities.containsKey(clazz))
            return entities.get(clazz).getPrimaryKeyPair(realisation, typeTranslator);
        else {
            Entity entity = new Entity(clazz);
            entities.put(clazz, entity);
            return entity.getPrimaryKeyPair(realisation, typeTranslator);
        }
    }

    private static final class Entity {
        private final Class<?> clazz;
        private final Map<String, EntityField> fields;

        public Entity(Class<?> clazz) {
            this.clazz = clazz;
            this.fields = new ConcurrentHashMap<>();
            Stream.concat(Stream.of(clazz.getFields()), Stream.of(clazz.getDeclaredFields()))
                    .filter(field -> !Modifier.isTransient(field.getModifiers()))
                    .filter(field -> !Modifier.isStatic(field.getModifiers()))
                    .filter(field -> field.isAnnotationPresent(Column.class))
                    .filter(field -> !field.isAnnotationPresent(Ignore.class))
                    .map(field -> Pair.of(field, field.getAnnotation(Column.class)))
                    .map(fieldColumnPair -> new EntityField(fieldColumnPair.getKey(), fieldColumnPair.getValue()))
                    .forEach(entityField -> fields.put(entityField.name, entityField));
        }

        public void createTable(String name, Connection connection, TypeTranslator typeTranslator) {
            try {
                StringBuilder request = new StringBuilder("CREATE TABLE IF NOT EXISTS " + name + " (");
                int i = 0;
                for(EntityField field : fields.values()) {
                    request.append(field.getColumnType(typeTranslator));
                    if(i + 1 < fields.size())
                        request.append(", ");
                    i++;
                }
                request.append(");");
                PreparedStatement preparedStatement = connection.prepareStatement(request.toString());
                preparedStatement.execute();

                for(EntityField field : fields.values()) {
                    if(field.hasIndex())
                        field.createIndex(name, connection);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public Object parse(ResultSet current, TypeTranslator typeTranslator) {
            try {
                Object instance = UNSAFE.allocateInstance(clazz);
                for(EntityField field : fields.values()) {
                    field.setField(instance, current, typeTranslator);
                }
                return instance;
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            }
        }

        public String[] getNames() {
            return fields.keySet().toArray(new String[0]);
        }

        public Object[] serializeEntity(Object o, TypeTranslator typeTranslator) {
            Object[] ret = new Object[fields.size()];
            Collection<EntityField> values = fields.values();
            Iterator<EntityField> iterator = values.iterator();
            int i = 0;
            while(iterator.hasNext()) {
                EntityField next = iterator.next();
                ret[i] = typeTranslator.translate(next.getField(o));
                i++;
            }
            return ret;
        }

        public Object[] getPrimaryKeyPair(Object realisation, TypeTranslator typeTranslator) {
            for(EntityField field : fields.values()) {
                if(field.annotation.primaryKey()) {
                    return new Object[]{field.name, typeTranslator.translate(field.getField(realisation))};
                }
            }
            return new Object[0];
        }

        private static final class EntityField {
            private final String name;
            private final Column annotation;
            private final Field field;
            private final long offset;

            public EntityField(Field field, Column annotation) {
                this.annotation = annotation;
                this.name = annotation.name();
                this.field = field;
                this.field.setAccessible(true);
                this.offset = UNSAFE.objectFieldOffset(field);
            }

            public String getColumnType(TypeTranslator typeTranslator) {
                return name + ' ' + typeTranslator.translate(field.getType())
                        + (annotation.primaryKey() ? " PRIMARY KEY" : "")
                        + (annotation.nullable() ? " NULL" : " NOT NULL")
                        + (annotation.unique() ? " UNIQUE" : "")
                        + (annotation.foreignKey() ? " FOREIGN KEY REFERENCES " + annotation.refTable() : "");
            }

            public boolean hasIndex() {
                return annotation.hasIndex();
            }

            public void createIndex(String table, Connection connection) throws SQLException {
                PreparedStatement preparedStatement = connection.prepareStatement("CREATE UNIQUE INDEX IF NOT EXISTS ? ON ? (?)");
                preparedStatement.setString(1, name + "_idx");
                preparedStatement.setString(2, table);
                preparedStatement.setString(3, name);
                preparedStatement.execute();
            }

            public void setField(Object obj, ResultSet current, TypeTranslator typeTranslator) {
                UNSAFE.putObject(obj, offset, typeTranslator.getObject(current, name, field.getType()));
            }

            public Object getField(Object o) {
                try {
                    return field.get(o);
                } catch (IllegalAccessException e) {
                    return null;
                }
            }
        }
    }
}
