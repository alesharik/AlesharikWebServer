package com.alesharik.webserver.plugin;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Objects;

public enum AccessPermissions {
    BASE("base"),
    CONTROL("control"),
    DASHBOARD("dashboard"),
    MICROSERVICES("microservices"),
    PLUGIN("plugin"),
    SERVER("server");

    @Getter
    private final String name;

    AccessPermissions(String name) {
        this.name = name;
    }

    /**
     * If str contains unknown access permission, send <code>new AccessPermissions[0]</code>
     */
    public static AccessPermissions[] fromString(String str, String delim) {
        Objects.requireNonNull(str);
        Objects.requireNonNull(delim);

        String[] parts = str.split(delim);
        ArrayList<AccessPermissions> list = new ArrayList<>();

        for(String part : parts) {
            switch (part) {
                case "base":
                    list.add(BASE);
                    break;
                case "control":
                    list.add(CONTROL);
                    break;
                case "dashboard":
                    list.add(DASHBOARD);
                    break;
                case "microservices":
                    list.add(MICROSERVICES);
                    break;
                case "plugin":
                    list.add(PLUGIN);
                    break;
                case "server":
                    list.add(SERVER);
                    break;
                default:
                    return new AccessPermissions[0];
            }
        }
        return list.toArray(new AccessPermissions[0]);
    }
}
