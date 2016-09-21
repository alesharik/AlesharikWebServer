package com.alesharik.webserver.control.dashboard.elements.menu;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;

public final class MenuDropdown extends MenuItem {
    private ArrayList<MenuItem> items = new ArrayList<>();

    public MenuDropdown(String fa, String name) {
        super(fa, name);
        type = Type.DROPDOWN;
    }

    public MenuDropdown addItem(MenuItem item) {
        items.add(item);
        return this;
    }

    public ArrayList<MenuItem> listItems() {
        return (ArrayList<MenuItem>) items.clone();
    }

    @Override
    protected void addCustomTypes(JSONObject object) {
        JSONArray array = new JSONArray();
        items.forEach(item -> array.add(item.serialize()));
        object.put("items", array);
    }
}
