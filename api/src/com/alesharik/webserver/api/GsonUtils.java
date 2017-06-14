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

package com.alesharik.webserver.api;

import com.alesharik.webserver.control.dashboard.elements.menu.Menu;
import com.alesharik.webserver.control.dashboard.elements.menu.MenuItem;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * This class hold {@link Gson} with all {@link com.google.gson.TypeAdapter} for api of AlesharikWebServer
 */
public class GsonUtils {
    private static final Gson gson;

    static {
        gson = new GsonBuilder()
                .registerTypeAdapter(Menu.class, new MenuSerializer())
                .registerTypeHierarchyAdapter(MenuItem.class, new MenuItemSerializer())
                .create();
    }

    public static Gson getGson() {
        return gson;
    }

    private static final class MenuSerializer implements JsonSerializer<Menu> {

        @Override
        public JsonElement serialize(Menu src, Type typeOfSrc, JsonSerializationContext context) {
            JsonArray menuItems = new JsonArray();
            src.menuItemList().forEach(menuItem -> menuItems.add(context.serialize(menuItem)));
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("items", menuItems);
            return jsonObject;
        }
    }

    private static final class MenuItemSerializer implements JsonSerializer<MenuItem> {

        @Override
        public JsonElement serialize(MenuItem src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            object.addProperty("type", src.getType());
            object.addProperty("fa", src.getFa());
            object.addProperty("text", src.getText());

            src.serialize(object, context);

            return object;
        }
    }

}
