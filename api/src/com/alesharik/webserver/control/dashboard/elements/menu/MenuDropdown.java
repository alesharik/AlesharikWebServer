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

import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This class used as dropdown in menu
 */
@ThreadSafe
@Deprecated
public final class MenuDropdown extends MenuItem {
    private static final String TYPE_NAME = "dropdown";

    private final CopyOnWriteArrayList<MenuItem> items;

    public MenuDropdown(String fa, String text) {
        super(fa, text, TYPE_NAME);
        items = new CopyOnWriteArrayList<>();
    }

    @Override
    public void serialize(JsonObject object, JsonSerializationContext context) {
        object.add("items", context.serialize(items));
    }

    /**
     * Add new item
     *
     * @param item the menu item to add
     * @return this
     */
    public MenuDropdown addItem(MenuItem item) {
        items.add(item);
        return this;
    }

    /**
     * Remove an item if it exists
     *
     * @param item the menu item to remove
     * @return this
     */
    public MenuDropdown removeItem(MenuItem item) {
        items.remove(item);
        return this;
    }

    /**
     * Return <code>true</code> if menu contains item
     *
     * @param item the menu item
     */
    public boolean containsItem(MenuItem item) {
        return items.contains(item);
    }

    /**
     * Return list of {@link MenuItem}s, which this dropdown contains
     *
     * @return unmodifiable list
     */
    public List<MenuItem> itemList() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof MenuDropdown)) return false;
        if(!super.equals(o)) return false;

        MenuDropdown that = (MenuDropdown) o;

        return items != null ? items.equals(that.items) : that.items == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (items != null ? items.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MenuDropdown{" +
                "type='" + getType() + '\'' +
                ", fa='" + getFa() + '\'' +
                ", text='" + getText() + '\'' +
                ", items=" + items +
                '}';
    }
}
