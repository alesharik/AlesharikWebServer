package com.alesharik.webserver.control.commands;

import com.alesharik.webserver.control.IServerConsoleCommand;

/**
 * Created by alesharik on 24.05.16.
 */
public class TestCommand implements IServerConsoleCommand {
    @Override
    public String handle(String params) {
        return "test";
    }
}
