package com.alesharik.webserver.control;

import java.io.File;
import java.util.HashMap;

//TODO send it to plugin
public interface IServerConsoleCommand {

    String handle(String params);

    default IServerConsoleCommand setRootFolder(File folder) {
        return this;
    }

    default IServerConsoleCommand setTemporalyStore(HashMap<String, String> map) {
        return this;
    }
}
