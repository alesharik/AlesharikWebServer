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

package com.alesharik.webserver.serverless.quorum;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * Contains "public" shared data from quorum. Can be cached on local agent, but must be retrieved from quorum member
 */
public interface QuorumData {
    /**
     * Return public data object
     *
     * @param name data key
     * @param <O>  object type
     * @return the object or <code>null</code> if it isn't found
     */
    @Nullable
    <O extends Object & Serializable> O getPublicData(String name, Class<O> clazz);

    /**
     * Return true if data retrieved from quorum member, false - from file cache
     *
     * @return true if data retrieved from quorum member, false - from file cache
     */
    boolean canTrust();

    /**
     * Return current quorum data revision number
     *
     * @return current revision number
     */
    @Nonnegative
    long getRevision();
}
