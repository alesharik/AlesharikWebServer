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

package com.alesharik.webserver.control.dashboard.elements.menu;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class MenuTest {
    private static Menu menu;
    private static Menu read;
    private static Menu sameAsRead;

    private static MenuItem toRemove;
    private static MenuItem inRead;

    @BeforeClass
    public static void setUp() throws Exception {
        menu = new Menu();
        toRemove = new TextMenuItem("qwer", "y");
        menu.addItem(toRemove);
        menu.addItem(new TextMenuItem("ty", "yui"));
        read = new Menu();
        inRead = new TextMenuItem("none", "none");
        read.addItem(inRead);
        read.addItem(new TextMenuItem("asd", "sdf"));
        sameAsRead = new Menu();
        sameAsRead.addItem(new TextMenuItem("none", "none"));
        sameAsRead.addItem(new TextMenuItem("asd", "sdf"));
    }

    @Test
    public void addItem() throws Exception {
        menu.addItem(new TextMenuItem("asdd", "sdfga"));
    }

    @Test(expected = NullPointerException.class)
    public void addItemNull() throws Exception {
        menu.addItem(null);
    }

    @Test
    public void removeItem() throws Exception {
        menu.removeItem(toRemove);
    }

    @Test(expected = NullPointerException.class)
    public void removeItemNull() throws Exception {
        menu.removeItem(null);
    }

    @Test
    public void containsItem() throws Exception {
        assertTrue(read.containsItem(inRead));
        assertFalse(read.containsItem(new TextMenuItem("asdf", "sdf")));
        assertFalse(read.containsItem(null));
    }

    @Test
    public void menuItemList() throws Exception {
        List<MenuItem> object = read.menuItemList();
        assertNotNull(object);
        assertTrue(object.size() == 2);
    }

    @Test
    public void equalsTest() throws Exception {
        assertTrue(sameAsRead.equals(read));
        assertFalse(sameAsRead.equals(menu));
    }

    @Test
    public void hashCodeTest() throws Exception {
        assertTrue(Integer.compare(sameAsRead.hashCode(), read.hashCode()) == 0);
        assertFalse(Integer.compare(sameAsRead.hashCode(), menu.hashCode()) == 0);
    }

    @Test
    public void toStringTest() throws Exception {
        assertNotNull(read.toString());
    }
}