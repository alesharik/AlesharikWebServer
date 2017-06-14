/*
 *  This file is part of AlesharikWebServer.
 *
 *     AlesharikWebServer is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     AlesharikWebServer is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with AlesharikWebServer.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

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
