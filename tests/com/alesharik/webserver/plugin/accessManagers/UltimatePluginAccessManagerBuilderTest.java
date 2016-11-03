package com.alesharik.webserver.plugin.accessManagers;

import junit.framework.Assert;
import org.junit.Test;

public class UltimatePluginAccessManagerBuilderTest extends Assert {
    private UltimatePluginAccessManagerBuilder builder = new UltimatePluginAccessManagerBuilder();

    @Test(expected = NullPointerException.class)
    public void setBaseAccessManager() throws Exception {
        builder.setBaseAccessManager(null);
    }

    @Test(expected = NullPointerException.class)
    public void setControlAccessManager() throws Exception {
        builder.setControlAccessManager(null);
    }

    @Test(expected = NullPointerException.class)
    public void setServerAccessManager() throws Exception {
        builder.setServerAccessManager(null);
    }

    @Test
    public void build() throws Exception {
        assertNotNull(builder.build());
    }
}