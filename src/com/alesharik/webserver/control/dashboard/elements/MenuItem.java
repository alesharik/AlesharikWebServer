package com.alesharik.webserver.control.dashboard.elements;

import org.json.simple.JSONObject;

public class MenuItem {
    protected Type type;
    private String fa;
    private String name;

    public MenuItem(String fa, String name) {
        this.fa = fa;
        this.name = name;
    }

    public final String serialize() {
        JSONObject object = new JSONObject();
        object.put("type", type.getValue());
        object.put("fa", fa);
        object.put("text", name);
        addCustomTypes(object);
        return object.toJSONString();
    }

    public String getFa() {
        return fa;
    }

    public String getName() {
        return name;
    }

    protected void addCustomTypes(JSONObject object) {

    }

    protected enum Type {
        ITEM("item"),
        DROPDOWN("dropdown");

        private String value;

        Type(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
