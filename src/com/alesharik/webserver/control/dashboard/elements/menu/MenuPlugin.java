package com.alesharik.webserver.control.dashboard.elements.menu;

import com.alesharik.webserver.js.JSClass;
import org.w3c.dom.html.HTMLElement;

/**
 * This class used for create top menu plugins. This plugins have name, width and html HTMLCode.<br>
 * The name is unique name in dashboard. It must contains word "Plugin" at the end.
 * The width is <HTMLCode>width</HTMLCode> of html element in pixels. It translated in css as <HTMLCode>width: yourWidthValue + "px"</HTMLCode>,
 * where yourWidthValue is set width.
 * The HTML HTMLCode is HTMLCode of your element. The HTMLCode is contains in <HTMLCode>div</HTMLCode>, which holds in <HTMLCode>li</HTMLCode>
 * The element can be active. If it active, the frontend HTMLCode can use it(draw, load, etc.). The default state is not active.
 * WARNING! For use your plugin you must activate it!
 */
public final class MenuPlugin extends JSClass {
    private boolean isActive = false;
    private int width = 0;
    private HTMLElement HTMLCode;

    /**
     * Initialize class extends form "MenuPlugin"
     *
     * @param name if name no contains word "Plugin" at the end, constructor will place it.
     */
    public MenuPlugin(String name) {
        super(name, "MenuPlugin");
        if(name.lastIndexOf("Plugin") == -1) {
            this.name += "Plugin";
        }
    }

    private MenuPlugin(String name, String extend) {
        this(name);
    }

    /**
     * You can't set extends for this class!
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public void setExtends(String extend) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    public void enable() {
        this.isActive = true;
    }

    public void disable() {
        this.isActive = false;
    }

    /**
     * Set it's name. If name no contains word "Plugin" at the end, method will place it.
     */
    @Override
    public void setName(String name) {
        this.name = name;
        if(name.lastIndexOf("Plugin") == -1) {
            this.name += "Plugin";
        }
    }

    @Override
    public String getName() {
        return name;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHTMLCode(HTMLElement code) {
        this.HTMLCode = code;
    }

    /**
     * Add method. If method name is "isActive", "getWidth", "getCode", "getName" it do nothing.
     */
    @Override
    public void addMethod(String methodName, String arguments, String code) {
        if(methodName.equals("isActive") || methodName.equals("getWidth")
                || methodName.equals("getCode") || methodName.equals("getName")) {
            return;
        }
        super.addMethod(methodName, arguments, code);
    }

    /**
     * Remove method. If method name is "isActive", "getWidth", "getCode", "getName" it do nothing.
     */
    @Override
    public void removeMethod(String methodName) {
        if(methodName.equals("isActive") || methodName.equals("getWidth")
                || methodName.equals("getCode") || methodName.equals("getName")) {
            return;
        }
        super.removeMethod(methodName);
    }

    public String getCode() {
        methods.put("isActive", "", "return " + isActive + ";");
        methods.put("getWidth", "", "return " + width + ";");
        methods.put("getCode", "", "return " + HTMLCode + ";");
        constructor = "this.name = " + name + ";" + constructor;
        return super.getCode();
    }
}
