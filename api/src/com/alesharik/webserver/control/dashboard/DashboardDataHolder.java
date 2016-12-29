package com.alesharik.webserver.control.dashboard;

import com.alesharik.webserver.control.dashboard.elements.menu.Menu;
import com.alesharik.webserver.control.dashboard.elements.menu.MenuDropdown;
import com.alesharik.webserver.control.dashboard.elements.menu.MenuItem;
import com.alesharik.webserver.control.dashboard.elements.menu.MenuPlugin;
import com.alesharik.webserver.control.dashboard.elements.menu.MenuPluginBuilder;
import com.alesharik.webserver.control.dashboard.elements.menu.TextMenuItem;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;

import java.util.ArrayList;

public final class DashboardDataHolder {
    private final Menu menu;
    private final ArrayList<MenuPlugin> menuPlugins;

    public DashboardDataHolder() {
        menu = new Menu()
                .addItem(new TextMenuItem("dashboard", "Dashboard").setContentId("dashboard"))
                .addItem(new MenuDropdown("sitemap", "Servers").addItem(new TextMenuItem("plus", "Add server").setContentId("addServer")))
                .addItem(new MenuDropdown("plus", "Plugins"))
                .addItem(new MenuDropdown("wrench", "Settings")
                        .addItem(new TextMenuItem("sliders", "Main settings").setContentId("settings/mainSettings"))
                        .addItem(new TextMenuItem("edit", "Edit top menu plugins").setContentId("settings/editTopMenuPlugins")));
        menuPlugins = new ArrayList<>();
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
        if(pluginName.equals(item.getText())) {
            menu.menuItemList().stream()
                    .filter(menuItem -> menuItem.getText().equals("plugins"))
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
