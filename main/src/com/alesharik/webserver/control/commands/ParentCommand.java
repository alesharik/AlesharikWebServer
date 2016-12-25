package com.alesharik.webserver.control.commands;

import com.alesharik.webserver.control.IServerConsoleCommand;

import java.util.HashMap;

public class ParentCommand implements IServerConsoleCommand {
    private HashMap<String, String> map;

    @Override
    public String handle(String params) {
        String path = map.get("currentPath");
        if(path.isEmpty()) {
            return "Can't move!";
        }
        path = path.substring(0, path.lastIndexOf("/"));
        map.put("currentPath", path);
        return path;
    }

    @Override
    public IServerConsoleCommand setTemporalyStore(HashMap<String, String> map) {
        this.map = map;
        return this;
    }
}
