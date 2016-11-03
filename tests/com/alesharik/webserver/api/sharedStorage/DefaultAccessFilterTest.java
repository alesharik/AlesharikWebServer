package com.alesharik.webserver.api.sharedStorage;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DefaultAccessFilterTest {
    @Test
    public void test() {
        DefaultAccessFilter defaultAccessFilter = new DefaultAccessFilter();
        assertTrue(defaultAccessFilter.canAccess(null, AccessFilter.Type.ADD_FILTER));
        assertTrue(defaultAccessFilter.canAccess(null, AccessFilter.Type.SET));
        assertTrue(defaultAccessFilter.canAccess(null, AccessFilter.Type.GET));
        assertTrue(defaultAccessFilter.canAccess(null, AccessFilter.Type.GET_EXTERNAL));
        assertTrue(defaultAccessFilter.canAccess(null, AccessFilter.Type.CLEAR));
        assertFalse(defaultAccessFilter.canAccess(null, AccessFilter.Type.SET_EXTERNAL));
    }
}