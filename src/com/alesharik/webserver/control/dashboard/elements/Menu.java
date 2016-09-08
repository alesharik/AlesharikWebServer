package com.alesharik.webserver.control.dashboard.elements;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * This class used for build dashboard menu
 */
public final class Menu {
    private ArrayList<MenuItem> items = new ArrayList<>();

    public Menu addItem(MenuItem item) {
        items.add(item);
        return this;
    }

    public List<MenuItem> list() {
        return (List<MenuItem>) items.clone();
    }

    public String serialize() {
        JSONObject jsonObject = new JSONObject();
        JSONArray array = new JSONArray();
        items.forEach(item -> array.add(item.serialize()));
        jsonObject.put("items", array);
        return jsonObject.toJSONString();
    }
}
