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

package com.alesharik.webserver.serverless.quorum.member;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * Represents quorum data interface
 */
public interface QuorumMemberData {
    /**
     * Set public data object
     *
     * @param key   object key
     * @param value the value, <code>null</code> - delete data object
     * @param <O>   value type
     */
    <O extends Object & Serializable> void setPublicData(@Nonnull String key, @Nullable O value);

    /**
     * Return public data by key
     *
     * @param key   the key
     * @param clazz data type class
     * @param <O>   data type
     * @return saved object or <code>null</code>
     */
    @Nullable
    <O extends Object & Serializable> O getPublicData(@Nonnull String key, @Nonnull Class<?> clazz);

    /**
     * Set private quorum member only data object
     *
     * @param key   object key
     * @param value the value, <code>null</code> - delete data object
     * @param <O>   data type
     */
    <O extends Object & Serializable> void setPrivateData(@Nonnull String key, @Nullable O value);

    /**
     * Return private quorum member only data object
     *
     * @param key   the key
     * @param clazz data type class
     * @param <O>   data type
     * @return saved object or <code>null</code>
     */
    <O extends Object & Serializable> O getPrivateData(@Nonnull String key, @Nonnull Class<?> clazz);

    /**
     * Return revision number
     *
     * @return revision number
     */
    long getRevision();
}
