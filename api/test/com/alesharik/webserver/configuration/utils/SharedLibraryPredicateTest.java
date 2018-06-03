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

package com.alesharik.webserver.configuration.utils;

import org.junit.Test;

import javax.annotation.Nonnull;
import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SharedLibraryPredicateTest {
    @Test
    public void less() {
        SharedLibrary.SharedLibraryPredicate predicate = new SharedLibrary.SharedLibraryPredicate("<a-2");
        assertTrue(predicate.test(lib("a", 1)));
        assertFalse(predicate.test(lib("a", 2)));
        assertFalse(predicate.test(lib("a", 3)));
        assertFalse(predicate.test(lib("b", 1)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void lessWithoutVersion() {
        new SharedLibrary.SharedLibraryPredicate("<a");
    }

    @Test
    public void lessEq() {
        SharedLibrary.SharedLibraryPredicate predicate = new SharedLibrary.SharedLibraryPredicate("<=a-2");
        assertTrue(predicate.test(lib("a", 1)));
        assertTrue(predicate.test(lib("a", 2)));
        assertFalse(predicate.test(lib("a", 3)));
        assertFalse(predicate.test(lib("b", 1)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void lessEqWithoutVersion() {
        new SharedLibrary.SharedLibraryPredicate("<=a");
    }

    @Test
    public void more() {
        SharedLibrary.SharedLibraryPredicate predicate = new SharedLibrary.SharedLibraryPredicate(">a-2");
        assertFalse(predicate.test(lib("a", 1)));
        assertFalse(predicate.test(lib("a", 2)));
        assertTrue(predicate.test(lib("a", 3)));
        assertFalse(predicate.test(lib("b", 1)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void moreWithoutVersion() {
        new SharedLibrary.SharedLibraryPredicate(">a");
    }

    @Test
    public void moreEq() {
        SharedLibrary.SharedLibraryPredicate predicate = new SharedLibrary.SharedLibraryPredicate(">=a-2");
        assertFalse(predicate.test(lib("a", 1)));
        assertTrue(predicate.test(lib("a", 2)));
        assertTrue(predicate.test(lib("a", 3)));
        assertFalse(predicate.test(lib("b", 1)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void moreEqWithoutVersion() {
        new SharedLibrary.SharedLibraryPredicate(">=a");
    }

    @Test
    public void eq() {
        SharedLibrary.SharedLibraryPredicate predicate = new SharedLibrary.SharedLibraryPredicate("=a-2");
        assertFalse(predicate.test(lib("a", 1)));
        assertTrue(predicate.test(lib("a", 2)));
        assertFalse(predicate.test(lib("a", 3)));
        assertFalse(predicate.test(lib("b", 1)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void eqWithoutVersion() {
        new SharedLibrary.SharedLibraryPredicate("=a");
    }

    @Test
    public void all() {
        SharedLibrary.SharedLibraryPredicate predicate = new SharedLibrary.SharedLibraryPredicate("a-2");
        assertTrue(predicate.test(lib("a", 1)));
        assertTrue(predicate.test(lib("a", 2)));
        assertTrue(predicate.test(lib("a", 3)));
        assertFalse(predicate.test(lib("b", 1)));
    }

    @Test
    public void allWithoutVersion() {
        SharedLibrary.SharedLibraryPredicate predicate = new SharedLibrary.SharedLibraryPredicate("a");
        assertTrue(predicate.test(lib("a", 1)));
        assertTrue(predicate.test(lib("a", 2)));
        assertTrue(predicate.test(lib("a", 3)));
        assertFalse(predicate.test(lib("b", 1)));
    }

    private SharedLibrary lib(String name, int v) {
        return new SharedLibrary() {
            @Nonnull
            @Override
            public String getName() {
                return name;
            }

            @Nonnull
            @Override
            public SharedLibraryVersion getVersion() {
                return new SharedLibraryVersion(new int[]{v});
            }

            @Nonnull
            @Override
            public File getFile() {
                return null;
            }
        };
    }
}