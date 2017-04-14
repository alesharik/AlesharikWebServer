package com.alesharik.webserver.control.commands;

import com.alesharik.webserver.control.IServerConsoleCommand;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

@Deprecated
public class ViewCommand implements IServerConsoleCommand {
    private File root;
    private File main;
    private HashMap<String, String> map;

    @Override
    public String handle(String params) {
        if(params.startsWith("./")) {
            String path;
            if((path = map.get("currentPath")) != null) {
                root = new File(main.getPath() + path);
            }
            root = new File(root + params);
        } else if(params.startsWith("/")) {
            root = new File(main + params);
        } else {
            return "File not found!";
        }

        if(root.isDirectory()) {
            return "Can't read a directory!";
        }
        if(!root.exists()) {
            return "File not found!";
        }

        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader reader = Files.newBufferedReader(root.toPath());
            String line;
            while((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
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
