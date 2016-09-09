package com.alesharik.webserver.control.dashboard;

import com.alesharik.webserver.control.dashboard.elements.Menu;
import com.alesharik.webserver.control.dashboard.elements.MenuDropdown;
import com.alesharik.webserver.control.dashboard.elements.MenuItem;
import com.alesharik.webserver.control.dashboard.elements.MenuTextItem;
import org.json.simple.JSONArray;

import java.util.ArrayList;

public class PluginDataHolder {
    private Menu menu;
    private ArrayList<String> menuPlugins = new ArrayList<>();

    public PluginDataHolder() {
        menu = new Menu()
                .addItem(new MenuTextItem("dashboard", "Dashboard").setContentId("dashboard"))
                .addItem(new MenuDropdown("sitemap", "Servers").addItem(new MenuTextItem("plus", "Add server").setContentId("addServer")))
                .addItem(new MenuDropdown("plus", "Plugins"))
                .addItem(new MenuDropdown("wrench", "Settings")
                        .addItem(new MenuTextItem("sliders", "Main settings").setContentId("settings/mainSettings"))
                        .addItem(new MenuTextItem("edit", "Edit top menu plugins").setContentId("settings/editTopMenuPlugins")));
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

    /**
     * Add a js class to hold
     *
     * @param clazz js class as string. This class should extends MenuPlugin
     * @return index of element
     */
    public int addPluginItemClass(String clazz) {
        menuPlugins.add(clazz);
        return menuPlugins.indexOf(clazz);
    }

    public void remove(int index) {
        menuPlugins.remove(index);
    }

    public ArrayList<String> allMenuPluginClasses() {
        return (ArrayList<String>) menuPlugins.clone();
    }

    public String getAllMenuPluginsAsJSONArray() {
        JSONArray array = new JSONArray();
        menuPlugins.forEach(array::add);
        return array.toJSONString();
    }
}
