package com.alesharik.webserver.api.sharedStorage;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class SharedStorageManagerTest {
    @BeforeClass
    public static void setup() {
        SharedStorageManager.registerNewSharedStorage("denied", (AccessFilter) (clazz, type, field) -> false);
    }

    @Test
    public void registerNewSharedStorage() throws Exception {
        SharedStorageManager.registerNewSharedStorage("asd");
        try {
            SharedStorageManager.registerNewSharedStorage("asd");
            fail();
        } catch (IllegalStateException e) {
            //Do nothing
        }
    }

    @Test
    public void unregisterSharedStorage() throws Exception {
        SharedStorageManager.registerNewSharedStorage("test0");
        SharedStorageManager.unregisterSharedStorage("test0");
        try {
            SharedStorageManager.unregisterSharedStorage("test0");
            fail();
        } catch (IllegalStateException e) {
            //Do nothing
        }
        try {
            SharedStorageManager.unregisterSharedStorage("denied");

            SharedStorageManager.registerNewSharedStorage("denied", (AccessFilter) (clazz, type, filed) -> false);
            fail();
        } catch (IllegalAccessException e) {
            //Do nothing
        }
    }

    @Test
    public void addAccessFilter() throws Exception {
        SharedStorageManager.registerNewSharedStorage("test1");
        SharedStorageManager.addAccessFilter("test1", (clazz, type, field) -> true);
        try {
            SharedStorageManager.addAccessFilter("denied", (clazz, type, field) -> false);
            fail();
        } catch (IllegalAccessException e) {
            //Do nothing
        }
    }

    @Test
    public void setField() throws Exception {
        SharedStorageManager.registerNewSharedStorage("test2");
        SharedStorageManager.setField("test2", "asd", "test");

        assertTrue("test".equals(SharedStorageManager.getField("test2", "asd")));

        try {
            SharedStorageManager.setField("denied", "asd", "test");
            fail();
        } catch (IllegalAccessException e) {
            //Do nothing
        }
    }

    @Test
    public void getField() throws Exception {
        SharedStorageManager.registerNewSharedStorage("test3");
        SharedStorageManager.setField("test3", "asd", "test");

        assertTrue("test".equals(SharedStorageManager.getField("test3", "asd")));

        try {
            SharedStorageManager.getField("denied", "asd");
            fail();
        } catch (IllegalAccessException e) {
            //Do nothing
        }
        assertNull(SharedStorageManager.getField("test3", "ass"));
    }
}