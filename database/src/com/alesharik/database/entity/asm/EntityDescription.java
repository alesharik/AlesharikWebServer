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

package com.alesharik.database.entity.asm;

import com.alesharik.database.entity.Entity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@EqualsAndHashCode
@ToString
public final class EntityDescription {
    @Getter
    private final Class<?> clazz;
    @Getter
    private final int version;
    @Getter
    private final List<EntityColumn> columns;
    @Getter
    private final boolean lazy;
    @Getter
    private final boolean bridge;
    private final AtomicReference<String> columnsListString = new AtomicReference<>("");

    private final AtomicBoolean isPrimaryKeyCacheOk = new AtomicBoolean(false);
    private final List<EntityColumn> primaryKey = new CopyOnWriteArrayList<>();
    private final AtomicReference<String> whereString = new AtomicReference<>(null);

    EntityDescription(Class<?> clazz, List<PreloadEntityColumn> columns, boolean lazy, boolean bridge) {
        this.clazz = clazz;
        this.version = clazz.getAnnotation(Entity.class).version();
        this.lazy = lazy;
        this.bridge = bridge;
        EntityDescription parent = null;
        if(clazz.getSuperclass().isAnnotationPresent(Entity.class)) {
            parent = EntityClassManager.getEntityDescription(clazz.getSuperclass());
        }
        List<EntityColumn> collect = columns.stream()
                .map(preloadEntityColumn -> preloadEntityColumn.build(clazz))
                .collect(Collectors.toList());
        if(parent != null)
            collect.addAll(parent.columns);
        this.columns = Collections.unmodifiableList(collect);
    }

    public boolean hasVersion() {
        return version != -1;
    }

    public String getColumnList() {
        String get = columnsListString.get();
        if(get.isEmpty()) {
            StringBuilder str = new StringBuilder();
            boolean notFirst = false;
            for(EntityColumn column : columns) {
                if(notFirst)
                    str.append(", ");
                else
                    notFirst = true;
                str.append(column.getColumnName());
            }
            String end = str.toString();
            if(!columnsListString.compareAndSet("", end))
                return columnsListString.get();
            return end;
        }
        return get;
    }

    public List<EntityColumn> getPrimaryKey() {
        if(isPrimaryKeyCacheOk.get())
            return primaryKey;
        else {
            List<EntityColumn> ret = new ArrayList<>();
            for(EntityColumn column : columns) {
                if(column.isPrimary())
                    ret.add(column);
            }
            if(isPrimaryKeyCacheOk.compareAndSet(false, true))
                primaryKey.addAll(ret);
        }
        return primaryKey;
    }

    /**
     * Return string for prepared <code>WHERE</code> part
     */
    public String getWhereString() {
        if(whereString.get() == null) {
            StringBuilder stringBuilder = new StringBuilder();
            boolean notFirst = false;
            for(EntityColumn entityColumn : getPrimaryKey()) {
                if(notFirst)
                    stringBuilder.append(" AND ");
                else
                    notFirst = true;
                stringBuilder.append(entityColumn.getColumnName());
                stringBuilder.append(" = ?");
            }
            String ret = stringBuilder.toString();
            if(!whereString.compareAndSet(null, ret))
                return whereString.get();
        }
        return whereString.get();
    }

    public EntityColumn getColumn(String fieldName) {
        for(EntityColumn column : columns) {
            if(column.getField().getName().equals(fieldName))
                return column;
        }
        return null;
    }
}
