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

import javax.annotation.concurrent.Immutable;

/**
 * This class is the base item of menu
 */
@Immutable
@Deprecated
public abstract class MenuItem {
    private static final String NONE_TYPE = "none";

    private final String type;
    private final String fa;
    private final String text;

    /**
     * @param fa   the <code>fa-</code> bootstrap icon
     * @param text the displayed text
     */
    public MenuItem(String fa, String text) {
        this(fa, text, NONE_TYPE);
    }

    protected MenuItem(String fa, String text, String type) {
        this.fa = fa;
        this.text = text;
        this.type = type;
    }

    /**
     * Return the fa - bootstrap icon, started with <code>fa</code>
     */
    public final String getFa() {
        return fa;
    }

    /**
     * Return displayed text
     */
    public final String getText() {
        return text;
    }

    /**
     * Return type of item(text, dropdown, etc)
     */
    public final String getType() {
        return type;
    }

    /**
     * Finish serialization of the menu item
     * @param object the json object with filled type, fa and text
     * @param context used for serialize other objects
     */
    public abstract void serialize(JsonObject object, JsonSerializationContext context);

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof MenuItem)) return false;

        MenuItem menuItem = (MenuItem) o;

        if(getType() != null ? !getType().equals(menuItem.getType()) : menuItem.getType() != null) return false;
        if(getFa() != null ? !getFa().equals(menuItem.getFa()) : menuItem.getFa() != null) return false;
        return getText() != null ? getText().equals(menuItem.getText()) : menuItem.getText() == null;
    }

    @Override
    public int hashCode() {
        int result = getType() != null ? getType().hashCode() : 0;
        result = 31 * result + (getFa() != null ? getFa().hashCode() : 0);
        result = 31 * result + (getText() != null ? getText().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MenuItem{" +
                "type='" + type + '\'' +
                ", fa='" + fa + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
