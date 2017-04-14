package com.alesharik.webserver.control.commands;

import com.alesharik.webserver.control.IServerConsoleCommand;

@Deprecated
public class TestCommand implements IServerConsoleCommand {
    @Override
    public String handle(String params) {
        return "test";
    }
}
