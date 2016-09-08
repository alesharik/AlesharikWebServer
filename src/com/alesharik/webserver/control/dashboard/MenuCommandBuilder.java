package com.alesharik.webserver.control.dashboard;

import com.alesharik.webserver.control.dashboard.elements.Menu;

public class MenuCommandBuilder {
    private StringBuilder stringBuilder = new StringBuilder();

    MenuCommandBuilder() {
    }

    public MenuCommandBuilder setMenu(Menu menu) {
        stringBuilder.append("menu:set:");
        stringBuilder.append(menu.serialize());
        stringBuilder.append("\n");
        return this;
    }

    public MenuCommandBuilder render() {
        stringBuilder.append("menu:render");
        stringBuilder.append("\n");
        return this;
    }

    /**
     * On the end of the output is <code>\n</code>!
     */
    public String build() {
        return stringBuilder.toString();
    }

    public void clear() {
        stringBuilder = new StringBuilder();
    }
}
