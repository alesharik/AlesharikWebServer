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

import com.alesharik.database.postgres.PostgresDriver;
import com.alesharik.database.transaction.reflect.TransactionMethod;
import com.alesharik.database.transaction.reflect.Transactional;

@Deprecated
public class Test {
    public static void main(String[] args) {
        Database database = new Database("jdbc:postgresql://localhost:1234/", "postgres", "ale456456", new PostgresDriver(), true);
        database.connect();
        TestTransaction testTransaction = database.wrapTransaction(TestTransaction.class, new TestTransactionImpl());
        testTransaction.asd();
        database.disconnect();
    }

    @Transactional
    private interface TestTransaction {
        @TransactionMethod
        void asd();
    }

    private static final class TestTransactionImpl implements TestTransaction {
        public void asd() {
            System.out.println("In transaction!");
        }
    }
}
