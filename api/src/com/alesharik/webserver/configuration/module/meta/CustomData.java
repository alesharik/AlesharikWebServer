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

package com.alesharik.webserver.configuration.module.meta;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores all {@link DataObject}s from custom processors
 */
@ThreadSafe
public final class CustomData {
    private final Map<String, DataObject> data = new ConcurrentHashMap<>();

    /**
     * Return containing {@link DataObject} or <code>null</code>
     *
     * @param type the object type
     * @return DataObject or <code>null</code>
     */
    @Nullable
    public DataObject getData(@Nonnull String type) {
        return data.get(type);
    }

    /**
     * Set data object
     *
     * @param type       the object type
     * @param dataObject the object
     */
    public void setData(@Nonnull String type, @Nullable DataObject dataObject) {
        if(dataObject == null)
            data.remove(type);
        else
            data.put(type, dataObject);
    }
}
