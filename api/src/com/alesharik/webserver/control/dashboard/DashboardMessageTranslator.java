package com.alesharik.webserver.control.dashboard;

import com.alesharik.webserver.control.dashboard.elements.menu.Menu;
import com.alesharik.webserver.control.dashboard.elements.menu.MenuPlugin;

import java.util.ArrayList;

/**
 * The DashboardMessageTranslator used for translate messages from java code to string commands, which send to dashboard
 */
public interface DashboardMessageTranslator {
    /**
     * This method return message which set menu in dashboard
     *
     * @param menu the menu
     */
    String createSetMenuMessage(Menu menu);

    /**
     * This method return message which set menu plugins in dashboard
     *
     * @param plugins the plugins
     */
    String createSetMenuPluginsMessage(ArrayList<MenuPlugin> plugins);
}
