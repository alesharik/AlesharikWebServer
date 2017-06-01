package com.alesharik.webserver.configuration;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ModuleHolderTest {
    private ConfigurationImpl.ModuleHolder moduleHolder;

    @Before
    public void setUp() throws Exception {
        Module module = mock(Module.class);
        when(module.getName()).thenReturn("test");
        moduleHolder = new ConfigurationImpl.ModuleHolder(module, "test");
    }

    @Test
    public void startShutdownTest() throws Exception {
        assertFalse(moduleHolder.isRunning());

        moduleHolder.start();
        assertTrue(moduleHolder.isRunning());
        verify(moduleHolder.getModule(), times(1)).start();

        moduleHolder.start();
        assertTrue(moduleHolder.isRunning());
        verify(moduleHolder.getModule(), times(1)).start();

        moduleHolder.shutdown();
        assertFalse(moduleHolder.isRunning());
        verify(moduleHolder.getModule(), times(1)).shutdown();

        moduleHolder.shutdown();
        assertFalse(moduleHolder.isRunning());
        verify(moduleHolder.getModule(), times(1)).shutdown();
    }

    @Test
    public void startShutdownNowTest() throws Exception {
        assertFalse(moduleHolder.isRunning());

        moduleHolder.start();
        assertTrue(moduleHolder.isRunning());
        verify(moduleHolder.getModule(), times(1)).start();

        moduleHolder.start();
        assertTrue(moduleHolder.isRunning());
        verify(moduleHolder.getModule(), times(1)).start();

        moduleHolder.shutdownNow();
        assertFalse(moduleHolder.isRunning());
        verify(moduleHolder.getModule(), times(1)).shutdownNow();

        moduleHolder.shutdownNow();
        assertFalse(moduleHolder.isRunning());
        verify(moduleHolder.getModule(), times(1)).shutdownNow();
    }

    @Test
    public void checkUncheck() throws Exception {
        assertFalse(moduleHolder.isChecked());

        moduleHolder.check();
        assertTrue(moduleHolder.isChecked());

        moduleHolder.uncheck();
        assertFalse(moduleHolder.isChecked());
    }

    @Test
    public void getType() throws Exception {
        assertEquals(moduleHolder.getType(), "test");
    }

    @Test
    public void mainCheckUncheck() throws Exception {
        assertFalse(moduleHolder.mainIsChecked());

        moduleHolder.mainCheck();
        assertTrue(moduleHolder.mainIsChecked());

        moduleHolder.mainUncheck();
        assertFalse(moduleHolder.mainIsChecked());
    }
}