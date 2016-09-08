package com.alesharik.webserver.control.dashboard;

import com.alesharik.webserver.control.dashboard.elements.Menu;
import com.alesharik.webserver.control.dashboard.elements.MenuDropdown;
import com.alesharik.webserver.control.dashboard.elements.MenuItem;
import com.alesharik.webserver.control.dashboard.elements.MenuTextItem;

public class PluginDataHolder {
    private Menu menu;

    public PluginDataHolder() {
        menu = new Menu()
                .addItem(new MenuTextItem("dashboard", "Dashboard").setContentId("dashboard"))
                .addItem(new MenuDropdown("sitemap", "Servers").addItem(new MenuTextItem("plus", "Add server").setContentId("addServer")))
                .addItem(new MenuDropdown("plus", "Plugins"));
    }

    public Menu getMenu() {
        return menu;
    }

    public void addItem(MenuItem item) {
        menu.addItem(item);
    }

    /**
     * Warning! Plugin item must have name, which equals plugin name
     */
    public void addPluginItem(String pluginName, MenuItem item) {
        if(pluginName.equals(item.getName())) {
            menu.list().stream()
                    .filter(menuItem -> menuItem.getName().equals("plugins"))
                    .forEach(menuItem -> ((MenuDropdown) menuItem).addItem(item));
        }
    }
}
