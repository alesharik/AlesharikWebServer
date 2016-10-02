package com.alesharik.webserver.control.dashboard.elements.menu;

import com.alesharik.webserver.js.JSClass;
import org.jsoup.nodes.Element;

import java.util.HashMap;

/**
 * This class used for create top menu plugins. This plugins have name, width and html HTMLElement.<br>
 * The name is unique name in dashboard. It must contains word "Plugin" at the end.
 * The width is <HTMLElement>width</HTMLElement> of html element in pixels. It translated in css as <HTMLElement>width: yourWidthValue + "px"</HTMLElement>,
 * where yourWidthValue is set width.
 * The HTML HTMLElement is HTMLElement of your element. The HTMLElement is contains in <HTMLElement>div</HTMLElement>, which holds in <HTMLElement>li</HTMLElement>
 * The element can be active. If it active, the frontend HTMLElement can use it(draw, load, etc.). The default state is not active.
 * WARNING! For use your plugin you must activate it!
 * The default parameters used in frontend to setup settings. If your plugin need manually user setup, use it.
 * In frontend use the parameters variable to interact with parameters.
 * WARNING! If you use non-transient fields in your plugin, you must deserialize it by overriding <code>deserialize</code> method.
 * If you override this method, you need to call super, or deserialize all class manually!
 */
public final class MenuPlugin extends JSClass {
    private boolean isActive = false;
    private int width = 0;
    private Element HTMLElement;
    private HashMap<String, String> defaultParameters = new HashMap<>();

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

    public void setHTMLElement(Element element) {
        this.HTMLElement = element;
    }

    /**
     * Add method. If method name is "isActive", "getWidth", "getCode", "getName" it do nothing.
     */
    @Override
    public void addMethod(String methodName, String arguments, String code) {
        if(methodName.equals("isActive") || methodName.equals("getWidth")
                || methodName.equals("getCode") || methodName.equals("getName") || methodName.equals("setParameter")
                || methodName.equals("getDefaultParameters")) {
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
                || methodName.equals("getCode") || methodName.equals("getName") || methodName.equals("setParameter")
                || methodName.equals("getDefaultParameters")) {
            return;
        }
        super.removeMethod(methodName);
    }

    /**
     * @param parameter some js code
     */
    public void addDefaultParameter(String name, String parameter) {
        defaultParameters.put(name, parameter);
    }

    public void removeDefaultParameter(String name) {
        defaultParameters.remove(name);
    }

    public HashMap<String, String> getDefaultParameters() {
        return new HashMap<>(defaultParameters);
    }

    public String getCode() {
        methods.put("isActive", "", "return " + isActive + ";");
        methods.put("getWidth", "", "return " + width + ";");
        methods.put("getCode", "", "return \"" + HTMLElement.html().replace("\"", "\'").replace("\n", "") + "\";");
        StringBuilder defaultParametersObject = new StringBuilder();
        defaultParameters.forEach((s, s2) -> {
            defaultParametersObject.append(s);
            defaultParametersObject.append(": ");
            defaultParametersObject.append(s2);
            defaultParametersObject.append(",");
        });
        String defaultParametersString = defaultParametersObject.toString();
        int lastIndex = defaultParametersString.lastIndexOf(",") == -1 ? defaultParametersString.length() : defaultParametersString.lastIndexOf(",");
        String substring = defaultParametersString.substring(0, lastIndex);
        methods.put("getDefaultParameters", "", "return {" + substring + "};");
        if(!constructor.startsWith("super();this.name = \"" + name + "\";")) {
            constructor = "super();this.name = \"" + name + "\"; this.parameters = {" + substring + "}" + constructor;
        }
        return super.getCode();
    }
}
