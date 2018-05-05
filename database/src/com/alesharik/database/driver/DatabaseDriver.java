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

package com.alesharik.database.driver;

import com.alesharik.database.cache.DatabaseCache;
import com.alesharik.database.connection.ConnectionProvider;
import com.alesharik.database.data.Schema;
import com.alesharik.database.transaction.TransactionManager;
import com.alesharik.database.user.UserManager;

/**
 * Driver can use caches for schemas
 */
public interface DatabaseDriver {
    void init(ConnectionProvider connection);

    /**
     * Work with cache only allowed
     */
    Schema getSchema(String name, boolean createIfNotExists);

    Schema[] getSchemas();

    void update();

    TransactionManager getTransactionManager();

    void updateTransactional(boolean is);

    UserManager<?, ?, ?> getUserManager();

    DatabaseCache<?> getCache();
}
