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
