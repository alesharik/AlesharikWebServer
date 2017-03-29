package com.alesharik.webserver.plugin;

import joptsimple.util.KeyValuePair;

import java.util.Arrays;
import java.util.HashMap;

/**
 * This class used as abstraction on meta file.<br>
 * Meta file is the key-value file with specific formatting rules:<br>
 * 1. All key-value pairs need to be divided form others with <code>\r\n</code><br>
 * 2. Key-value pair need to be divided by by <code>=</code> character. If in line used 2 or more <code>=</code>
 * characters, parser use first
 */
@Deprecated
class MetaFile {
    private MetaFile() {
    }

    private final HashMap<String, String> attributes = new HashMap<>();

    public String getAttribute(String key) {
        return attributes.get(key);
    }

    public static MetaFile parse(String str) {
        MetaFile ret = new MetaFile();
        String[] params = str.replace("\r\n", "\n").split("\n");
        Arrays.stream(params).map(KeyValuePair::valueOf).forEach(keyValuePair -> ret.attributes.put(keyValuePair.key, keyValuePair.value));
        return ret;
    }
}