package com.alesharik.webserver.control.dashboard.elements.menu;

import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MenuItemTest {
    private static final String TEXT = "test";
    private static final String FA = "asdf";
    private static final String TYPE = "testElem";

    private static MenuItem menuItem;
    private static MenuItem sameAsMenuItem;

    @BeforeClass
    public static void setUp() throws Exception {
        menuItem = new TestMenuItem(FA, TEXT);
        sameAsMenuItem = new TestMenuItem(FA, TEXT);
    }

    @Test
    public void getFa() throws Exception {
        assertTrue(menuItem.getFa().equals(FA));
    }

    @Test
    public void getText() throws Exception {
        assertTrue(menuItem.getText().equals(TEXT));
    }

    @Test
    public void getType() throws Exception {
        assertTrue(menuItem.getType().equals(TYPE));
    }

    @Test
    public void serialize() throws Exception {
        JsonObject object = new JsonObject();
        menuItem.serialize(object, null); //Do not need serialization context
        assertTrue(object.has("ok") && object.get("ok").getAsBoolean());
    }

    @Test
    public void equalsTest() throws Exception {
        assertTrue(sameAsMenuItem.equals(menuItem));
    }

    @Test
    public void hashCodeTest() throws Exception {
        assertTrue(Integer.compare(sameAsMenuItem.hashCode(), menuItem.hashCode()) == 0);
    }

    @Test
    public void toStringTest() throws Exception {
        assertNotNull(menuItem.toString());
    }

    private static final class TestMenuItem extends MenuItem {
        public TestMenuItem(String fa, String text) {
            super(fa, text, TYPE);
        }

        @Override
        public void serialize(JsonObject object, JsonSerializationContext context) {
            object.addProperty("ok", true);
        }
    }

}