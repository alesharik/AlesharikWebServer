package com.alesharik.webserver.control.dashboard.elements.menu;

import org.json.simple.JSONObject;

public final class MenuTextItem extends MenuItem {
    private String contentId;

    public MenuTextItem(String fa, String name) {
        super(fa, name);
        type = Type.ITEM;
    }

    public MenuTextItem setContentId(String contentId) {
        this.contentId = contentId;
        return this;
    }

    public String getContentId() {
        return contentId;
    }

    @Override
    protected void addCustomTypes(JSONObject object) {
        object.put("contentId", contentId);
    }
}
