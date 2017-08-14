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

import javax.annotation.concurrent.ThreadSafe;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This class hold dashboard navigation menu
 */
@ThreadSafe
public final class Menu {
    private final CopyOnWriteArrayList<MenuItem> items;

    public Menu() {
        items = new CopyOnWriteArrayList<>();
    }

    /**
     * Add new item
     *
     * @param item the menu item to add
     * @return this
     */
    public Menu addItem(MenuItem item) {
        items.add(Objects.requireNonNull(item));
        return this;
    }

    /**
     * Remove an item if it exists
     *
     * @param item the menu item to remove
     * @return this
     */
    public Menu removeItem(MenuItem item) {
        items.remove(Objects.requireNonNull(item));
        return this;
    }

    /**
     * Return <code>true</code> if menu contains item
     *
     * @param item the menu item
     */
    public boolean containsItem(MenuItem item) {
        if(item == null) {
            return false;
        }
        return items.contains(item);
    }

    /**
     * Return list of {@link MenuItem}s, which this menu contains
     *
     * @return unmodifiable list
     */
    public List<MenuItem> menuItemList() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof Menu)) return false;

        Menu menu = (Menu) o;

        return items != null ? items.equals(menu.items) : menu.items == null;
    }

    @Override
    public int hashCode() {
        return items != null ? items.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Menu{" +
                "items=" + items +
                '}';
    }
}
