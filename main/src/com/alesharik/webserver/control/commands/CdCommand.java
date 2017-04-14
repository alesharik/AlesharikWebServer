package com.alesharik.webserver.control.commands;

import com.alesharik.webserver.control.IServerConsoleCommand;

import java.io.File;
import java.util.HashMap;

@Deprecated
public class CdCommand implements IServerConsoleCommand {
    File root;
    File main;
    HashMap<String, String> map;

    @Override
    public String handle(String params) {
        root = new File(main.getPath() + "/" + map.get("currentPath"));
        if(params.startsWith("/")) {
            map.put("currentPath", params);
            root = new File(main.getPath() + "/" + map.get("currentPath"));
            if(!root.exists()) {
                return "Folder not found";
            } else if(root.isFile()) {
                return "Can't move in file!";
            }
            return map.get("currentPath");
        } else if(params.startsWith("./")) {
            root = new File(root.getPath() + params.substring(1));
            if(!root.exists()) {
                return "Folder not found";
            } else if(root.isFile()) {
                return "Can't move in file!";
            }
            map.put("currentPath", root.getPath().substring(main.getPath().length()));
            return root.getPath().substring(main.getPath().length());
        }
        return "/";
    }

    @Override
    public IServerConsoleCommand setRootFolder(File folder) {
        this.root = folder;
        this.main = folder;
        return this;
    }

    @Override
    public IServerConsoleCommand setTemporalyStore(HashMap<String, String> map) {
        this.map = map;
        return this;
    }
}
