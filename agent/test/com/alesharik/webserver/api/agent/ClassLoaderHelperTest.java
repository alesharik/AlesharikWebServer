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

package com.alesharik.webserver.api.agent;

import com.alesharik.webserver.test.TestUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ClassLoaderHelperTest {
    private RescanableClassLoader rescanableClassLoader;
    private IgnoredClassLoader ignoredClassLoader;
    private CloseClassLoader closeClassLoader;
    private ClassLoader classLoader;

    @Before
    public void setUp() throws Exception {
        rescanableClassLoader = new RescanableClassLoader();
        ignoredClassLoader = new IgnoredClassLoader();
        closeClassLoader = new CloseClassLoader();
        classLoader = new ClassLoader() {
        };
    }

    @Test
    public void testRescanable() throws Exception {
        assertTrue(ClassLoaderHelper.isRescanable(rescanableClassLoader));
        assertFalse(ClassLoaderHelper.isRescanable(ignoredClassLoader));
        assertFalse(ClassLoaderHelper.isRescanable(closeClassLoader));
        assertFalse(ClassLoaderHelper.isRescanable(classLoader));
    }

    @Test
    public void testIgnored() throws Exception {
        assertFalse(ClassLoaderHelper.isIgnored(rescanableClassLoader));
        assertTrue(ClassLoaderHelper.isIgnored(ignoredClassLoader));
        assertFalse(ClassLoaderHelper.isIgnored(closeClassLoader));
        assertFalse(ClassLoaderHelper.isIgnored(classLoader));
    }

    @Test
    public void testCloseable() throws Exception {
        assertFalse(ClassLoaderHelper.isCloseable(rescanableClassLoader));
        assertFalse(ClassLoaderHelper.isCloseable(ignoredClassLoader));
        assertTrue(ClassLoaderHelper.isCloseable(closeClassLoader));
        assertFalse(ClassLoaderHelper.isCloseable(classLoader));
    }

    @Test
    public void testOpen() throws Exception {
        closeClassLoader.open();

        assertTrue(ClassLoaderHelper.isOpen(closeClassLoader));

        closeClassLoader.close();
        assertFalse(ClassLoaderHelper.isOpen(closeClassLoader));
    }

    @Test
    public void testClose() throws Exception {
        closeClassLoader.open();

        assertFalse(ClassLoaderHelper.isClosed(closeClassLoader));

        closeClassLoader.close();
        assertTrue(ClassLoaderHelper.isClosed(closeClassLoader));
    }

    @Test
    public void testDefaultOpenCloseBehavior() throws Exception {
        assertTrue(ClassLoaderHelper.isOpen(classLoader));
        assertFalse(ClassLoaderHelper.isClosed(classLoader));
    }

    @Test
    public void testUtilityClass() throws Exception {
        TestUtils.assertUtilityClass(ClassLoaderHelper.class);
    }

    @Rescanable
    private static final class RescanableClassLoader extends ClassLoader {

    }

    @IgnoreClassLoader
    private static final class IgnoredClassLoader extends ClassLoader {

    }

    private static final class CloseClassLoader extends ClassLoader implements CloseableClassLoader {
        private boolean closed;

        @Override
        public void close() {
            closed = true;
        }

        public void open() {
            closed = false;
        }

        @Override
        public boolean isClosed() {
            return closed;
        }
    }
}