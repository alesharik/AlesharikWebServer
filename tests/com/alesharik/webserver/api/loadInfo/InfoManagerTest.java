package com.alesharik.webserver.api.loadInfo;

import org.junit.Test;

import java.util.NoSuchElementException;

import static org.junit.Assert.fail;

//TODO write this
public class InfoManagerTest {
    @Test
    public void registerUnregisterNewInfo() throws Exception {
        Info info = () -> {
        };
        InfoManager.registerNewInfo("asd", info);
        InfoManager.unregisterInfo("asd", info);

        try {
            InfoManager.unregisterInfo("asd", info);
            fail();
        } catch (NoSuchElementException e) {
            //OK
        }
    }

    @Test
    public void getInfo() throws Exception {

    }

    @Test
    public void getInfo1() throws Exception {

    }

    @Test
    public void getFirstInfo() throws Exception {

    }

    @Test
    public void getFirstInfo1() throws Exception {

    }
}