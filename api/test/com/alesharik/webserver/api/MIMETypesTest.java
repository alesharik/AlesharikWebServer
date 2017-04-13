package com.alesharik.webserver.api;

import com.alesharik.webserver.TestUtils;
import com.alesharik.webserver.exceptions.MIMETypeAlreadyExistsException;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class MIMETypesTest {
    @Test
    public void addType() throws Exception {
        MIMETypes.addType(".test", "test/test");
        assertTrue(MIMETypes.types.containsKey(".test"));
        assertTrue(MIMETypes.types.containsValue("test/test"));
    }

    @Test(expected = MIMETypeAlreadyExistsException.class)
    public void addExistsType() throws Exception {
        MIMETypes.addType(".x3d", "test/test");
    }

    @Test
    public void getMIMETypes() throws Exception {
        Set<String> types = new HashSet<>(MIMETypes.getMIMETypes());
        types.removeIf(MIMETypes.types::containsValue);
        assertTrue(types.isEmpty());
    }

    @Test
    public void getFileExtensions() throws Exception {
        Set<String> types = new HashSet<>(MIMETypes.getFileExtensions());
        types.removeIf(MIMETypes.types::containsKey);
        assertTrue(types.isEmpty());
    }

    @Test
    public void findType() throws Exception {
        assertEquals("text/plain", MIMETypes.findType(".txt"));
    }

    @Test
    public void contains() throws Exception {
        assertTrue(MIMETypes.contains(".txt"));
        assertFalse(MIMETypes.contains(".wtfIsThis"));
    }

    @Test
    public void testUtilsClass() throws Exception {
        TestUtils.assertUtilityClass(MIMETypes.class);
    }
}