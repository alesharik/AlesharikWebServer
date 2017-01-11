package com.alesharik.webserver.control.dashboard.elements.menu;

import com.alesharik.webserver.api.GsonUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Type;

import static org.junit.Assert.*;

public class TextMenuItemTest {
    private static final String FA = "asdf";
    private static final String TEXT = "fgsd";

    private static TextMenuItem menuItem;
    private static TextMenuItem read;
    private static TextMenuItem sameAsRead;

    @BeforeClass
    public static void setUp() throws Exception {
        menuItem = new TextMenuItem(FA, TEXT);
        read = new TextMenuItem(FA, TEXT);
        sameAsRead = new TextMenuItem(FA, TEXT);

        read.setContentId("test");
        sameAsRead.setContentId("test");
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
        assertTrue(object.has("contentId"));
        assertTrue(object.get("contentId").getAsString().equals("test"));
    }

    @Test
    public void setContentId() throws Exception {
        menuItem.setContentId("asd");
    }

    @Test
    public void getContentId() throws Exception {
        assertTrue(read.getContentId().equals("test"));
    }

    @Test
    public void equalsTest() throws Exception {
        assertTrue(sameAsRead.equals(read));
        assertFalse(sameAsRead.equals(menuItem));
    }

    @Test
    public void hashCodeTest() throws Exception {
        assertTrue(Integer.compare(read.hashCode(), sameAsRead.hashCode()) == 0);
        assertFalse(Integer.compare(read.hashCode(), menuItem.hashCode()) == 0);
    }

    @Test
    public void toStringTest() throws Exception {
        assertNotNull(read.toString());
    }

}