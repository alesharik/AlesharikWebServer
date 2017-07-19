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
import com.alesharik.database.entity.Entity;
import com.alesharik.database.postgres.PostgresDriver;
import lombok.Getter;

import java.util.UUID;

@Deprecated
public class Test {
    public static void main(String[] args) {
        Database database = new Database("jdbc:postgresql://localhost:1234/", "postgres", "ale456456", new PostgresDriver(), false);
        database.connect();
        Schema schema = database.getSchema("test", true);
        database.createTable("test.table", TestEntity.class);
        Table<TestEntity> table = schema.getTable("table", TestEntity.class);
        table.disableCaching();
        table.add(new TestEntity(UUID.randomUUID(), "test", 123));
        database.disconnect();
    }

    @Getter
    @Entity(1)
    private static final class TestEntity {
        @Column(name = "id", primaryKey = true, unique = true)
        private final UUID id;
        @Column(name = "ast")
        private String ast;
        @Column(name = "test")
        private int test;

        public TestEntity(UUID id, String ast, int test) {
            this.id = id;
            this.ast = ast;
            this.test = test;
        }
    }
}
