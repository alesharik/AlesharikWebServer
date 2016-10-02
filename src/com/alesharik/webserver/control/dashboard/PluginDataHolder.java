package com.alesharik.webserver.control.dashboard;

import com.alesharik.webserver.control.dashboard.elements.menu.Menu;
import com.alesharik.webserver.control.dashboard.elements.menu.MenuDropdown;
import com.alesharik.webserver.control.dashboard.elements.menu.MenuItem;
import com.alesharik.webserver.control.dashboard.elements.menu.MenuPlugin;
import com.alesharik.webserver.control.dashboard.elements.menu.MenuPluginBuilder;
import com.alesharik.webserver.control.dashboard.elements.menu.MenuTextItem;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;

import java.util.ArrayList;

public class PluginDataHolder {
    private Menu menu;
    private ArrayList<MenuPlugin> menuPlugins = new ArrayList<>();

    public PluginDataHolder() {
        menu = new Menu()
                .addItem(new MenuTextItem("dashboard", "Dashboard").setContentId("dashboard"))
                .addItem(new MenuDropdown("sitemap", "Servers").addItem(new MenuTextItem("plus", "Add server").setContentId("addServer")))
                .addItem(new MenuDropdown("plus", "Plugins"))
                .addItem(new MenuDropdown("wrench", "Settings")
                        .addItem(new MenuTextItem("sliders", "Main settings").setContentId("settings/mainSettings"))
                        .addItem(new MenuTextItem("edit", "Edit top menu plugins").setContentId("settings/editTopMenuPlugins")));
        menuPlugins.add(new MenuPluginBuilder("Time")
                .setWidth(60)
                .setHTMLElement(Jsoup.parse("<p data-clock=\"true\"></p>"))
                .build());
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
     * @param menuPlugin js class as string. This class should extends MenuPlugin
     * @return index of element
     */
    public int addPluginItemClass(MenuPlugin menuPlugin) {
        menuPlugins.add(menuPlugin);
        return menuPlugins.indexOf(menuPlugin);
    }

    public void remove(int index) {
        menuPlugins.remove(index);
    }

    public ArrayList<MenuPlugin> allMenuPluginClasses() {
        return (ArrayList<MenuPlugin>) menuPlugins.clone();
    }

    //TODO send minimized code
    public String getAllMenuPluginsAsJSONArray() {
        JSONArray array = new JSONArray();
        menuPlugins.forEach(menuPlugin -> {
            JSONObject object = new JSONObject();
            object.put("value", menuPlugin.getCode());
            array.add(object);
        });
        return array.toJSONString();
    }
}
