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

import com.alesharik.webserver.api.GsonUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.List;

import static org.junit.Assert.*;

public class MenuDropdownTest {
    private static final String FA = "sad";
    private static final String TEXT = "dsagf";

    private static MenuDropdown dropdown;
    private static MenuDropdown read;
    private static MenuDropdown sameAsRead;
    private static MenuItem removeItem;
    private static MenuItem dude;
    private static TextMenuItem item;

    @BeforeClass
    public static void setUp() throws Exception {
        dropdown = new MenuDropdown(FA, TEXT);
        read = new MenuDropdown(FA, TEXT);
        read.addItem(new TextMenuItem("asd", "asd"));
        read.addItem(new TextMenuItem("asd", "asd"));
        read.addItem(new TextMenuItem("asd", "asd"));
        sameAsRead = new MenuDropdown(FA, TEXT);
        item = new TextMenuItem("asd", "asd");
        sameAsRead.addItem(item);
        sameAsRead.addItem(new TextMenuItem("asd", "asd"));
        sameAsRead.addItem(new TextMenuItem("asd", "asd"));

        removeItem = new TextMenuItem("asdd", "fdsf");
        dropdown.addItem(removeItem);
        dude = new TextMenuItem("gfdsdf", "54666665");
    }

    @Test
    public void serialize() throws Exception {
        JsonObject object = new JsonObject();
        read.serialize(object, new JsonSerializationContext() {
            @Override
            public JsonElement serialize(Object src) {
                return GsonUtils.getGson().toJsonTree(src);
            }

            @Override
            public JsonElement serialize(Object src, Type typeOfSrc) {
                return GsonUtils.getGson().toJsonTree(src, typeOfSrc);
            }
        });
        assertTrue(object.has("items") && object.get("items").getAsJsonArray().size() == 3);
    }

    @Test
    public void addItem() throws Exception {
        dropdown.addItem(new TextMenuItem("asd", "fsad"));
    }

    @Test
    public void removeItem() throws Exception {
        dropdown.removeItem(removeItem);
    }

    @Test
    public void containsItem() throws Exception {
        assertTrue(read.containsItem(item));
        assertFalse(read.containsItem(dude));
    }

    @Test
    public void itemList() throws Exception {
        List<MenuItem> menuItems = read.itemList();
        assertNotNull(menuItems);
        assertTrue(menuItems.size() == 3);
    }

    @Test
    public void equalsTest() throws Exception {
        assertTrue(sameAsRead.equals(read));
        assertFalse(sameAsRead.equals(dropdown));
    }

    @Test
    public void hashCodeTest() throws Exception {
        assertTrue(Integer.compare(sameAsRead.hashCode(), read.hashCode()) == 0);
        assertFalse(Integer.compare(sameAsRead.hashCode(), dropdown.hashCode()) == 0);
    }

    @Test
    public void toStringTest() throws Exception {
        assertNotNull(read.toString());
    }

}