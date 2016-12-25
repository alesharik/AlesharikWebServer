package com.alesharik.webserver.control.commands;

import com.alesharik.webserver.control.IServerConsoleCommand;

import java.util.HashMap;

public class DirCommand implements IServerConsoleCommand {
    private HashMap<String, String> map;

    @Override
    public String handle(String params) {
        if(map.get("currentPath") == null) {
            return "/";
        }
        return map.get("currentPath");
    }

    @Override
    public IServerConsoleCommand setTemporalyStore(HashMap<String, String> map) {
        this.map = map;
        return this;
    }
}
