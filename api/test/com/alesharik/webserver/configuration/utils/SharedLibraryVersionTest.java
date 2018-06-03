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

import static org.junit.Assert.assertEquals;

public class SharedLibraryVersionTest {
    @Test
    public void parseVersion() {
        SharedLibraryVersion version = new SharedLibraryVersion("1.2.3.4");
        assertEquals(1, version.getMajorVersion());
        assertEquals(2, version.getMinorVersion());
        assertEquals(3, version.getPatch());
        assertEquals(4, version.getVersion()[3]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseNone() {
        new SharedLibraryVersion("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void createNone() {
        new SharedLibraryVersion(new int[0]);
    }

    @Test
    public void isolation() {
        int[] a = new int[]{1, 2, 3, 4};
        SharedLibraryVersion version = new SharedLibraryVersion(a);
        assertEquals(1, version.getMajorVersion());
        assertEquals(2, version.getMinorVersion());
        assertEquals(3, version.getPatch());
        assertEquals(4, version.getVersion()[3]);

        a[0] = 100;
        a[1] = 101;
        a[2] = 102;
        a[3] = 103;

        assertEquals(1, version.getMajorVersion());
        assertEquals(2, version.getMinorVersion());
        assertEquals(3, version.getPatch());
        assertEquals(4, version.getVersion()[3]);

        a = version.getVersion();
        a[0] = 100;
        a[1] = 101;
        a[2] = 102;
        a[3] = 103;

        assertEquals(1, version.getMajorVersion());
        assertEquals(2, version.getMinorVersion());
        assertEquals(3, version.getPatch());
        assertEquals(4, version.getVersion()[3]);
    }

    @Test
    public void withoutMinor() {
        SharedLibraryVersion version = new SharedLibraryVersion("1");
        assertEquals(1, version.getMajorVersion());
        assertEquals(-1, version.getMinorVersion());
        assertEquals(-1, version.getPatch());
        assertEquals(1, version.getVersion().length);
    }

    @Test
    public void compare() {
        SharedLibraryVersion version = new SharedLibraryVersion("9.9.9.9.9.9");
        SharedLibraryVersion version1 = new SharedLibraryVersion("9.9.9.9");
        SharedLibraryVersion version2 = new SharedLibraryVersion("8.9.9.9");
        SharedLibraryVersion version3 = new SharedLibraryVersion("9.9.9.9");
        SharedLibraryVersion version4 = new SharedLibraryVersion("9.9.9.9.0.0");
        SharedLibraryVersion version5 = new SharedLibraryVersion("9.9.9.9.0.0.1");

        assertEquals(-1, version1.compareTo(version));
        assertEquals(1, version.compareTo(version1));

        assertEquals(-1, version2.compareTo(version));

        assertEquals(0, version3.compareTo(version1));
        assertEquals(0, version3.compareTo(version4));
        assertEquals(-1, version4.compareTo(version5));
    }
}