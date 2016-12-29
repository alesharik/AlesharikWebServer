package com.alesharik.webserver.control.dashboard.elements.menu;

import org.jsoup.nodes.Element;

import java.util.HashMap;

/**
 * This class used for build simple {@link MenuPlugin} with no problems.
 */
public class MenuPluginBuilder {
    private String name = "";
    private int width = -1;
    private Element element = null;
    private HashMap<String, String> defaultParameters = new HashMap<>();

    public MenuPluginBuilder() {
    }

    public MenuPluginBuilder(String name) {
        this.name = name;
    }

    public MenuPluginBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public MenuPluginBuilder setWidth(int width) {
        this.width = width;
        return this;
    }

    public MenuPluginBuilder setHTMLElement(Element element) {
        this.element = element;
        return this;
    }

    /**
     * @param parameter some js code
     */
    public MenuPluginBuilder addDefaultParameter(String name, String parameter) {
        defaultParameters.put(name, parameter);
        return this;
    }

    /**
     * Return activated plugin. Can throw {@link IllegalArgumentException} if one of argument is incorrect
     */
    public MenuPlugin build() {
        if(name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty!");
        }
        if(width < 0) {
            throw new IllegalArgumentException("Width cannot be less than zero!");
        }
        if(element == null) {
            throw new IllegalArgumentException("HTML element cannot be null!");
        }

        MenuPlugin plugin = new MenuPlugin(name);
        plugin.setWidth(width);
        plugin.setHTMLElement(element);
        defaultParameters.forEach(plugin::addDefaultParameter);
        plugin.enable();
        return plugin;
    }

    public void clear() {
        element = null;
        width = -1;
        name = "";
        defaultParameters.clear();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof MenuPluginBuilder)) return false;

        MenuPluginBuilder that = (MenuPluginBuilder) o;

        if(width != that.width) return false;
        if(name != null ? !name.equals(that.name) : that.name != null) return false;
        return element != null ? element.equals(that.element) : that.element == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + width;
        result = 31 * result + (element != null ? element.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MenuPluginBuilder{" +
                "name='" + name + '\'' +
                ", width=" + width +
                ", element=" + element +
                '}';
    }
}
