package com.alesharik.webserver.control.commands;

import com.alesharik.webserver.control.IServerConsoleCommand;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

public class LsCommand implements IServerConsoleCommand {
    private File root;
    private File main;
    private HashMap<String, String> map;

    @Override
    public String handle(String params) {
        String path;
        if((path = map.get("currentPath")) != null) {
            root = new File(main.getPath() + path);
        }
        StringBuilder sb = new StringBuilder();

        Arrays.asList(root.listFiles()).forEach(file -> {
            sb.append(file.getName());
            sb.append("\n");
        });
        String str = sb.toString();
        if(!root.equals(main)) {
            str = map.get("currentPath") + "\n" + str;
        }
        return str;
    }

    @Override
    public IServerConsoleCommand setRootFolder(File folder) {
        this.root = this.main = folder;
        return this;
    }

    @Override
    public IServerConsoleCommand setTemporalyStore(HashMap<String, String> map) {
        this.map = map;
        return this;
    }
}
