/*
 *  This file is part of AlesharikWebServer.
 *
 *     AlesharikWebServer is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     AlesharikWebServer is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with AlesharikWebServer.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.alesharik.webserver.configuration;

import com.alesharik.webserver.api.control.ControlSocketClientModule;
import com.alesharik.webserver.api.control.ControlSocketServerModule;
import com.alesharik.webserver.api.server.wrapper.server.HttpServer;
import com.alesharik.webserver.control.AdminDataStorage;
import com.alesharik.webserver.control.dashboard.DashboardDataHolder;
import com.alesharik.webserver.exceptions.error.ConfigurationParseError;
import com.alesharik.webserver.module.server.SecuredStoreModule;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.experimental.UtilityClass;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@UtilityClass
public class XmlHelper {
    private static Configuration configuration = null;

    public static void setConfiguration(Configuration config) {
        if(configuration == null) {
            configuration = config;
        }
    }

    /**
     * Get {@link SecuredStoreModule} form xml config
     *
     * @param nodeName name of node, which contains {@link SecuredStoreModule} name
     * @param config   config node
     * @param required if true, throw {@link ConfigurationParseError} if node not found
     * @return null if required == false and value not found, overwise {@link SecuredStoreModule} instance
     */
    @Nullable
    public static SecuredStoreModule getSecuredStore(String nodeName, Element config, boolean required) {
        Node nameNode = config.getElementsByTagName(nodeName).item(0);
        if(nameNode == null) {
            if(required) {
                throw new ConfigurationParseError("Node " + nodeName + " not found!");
            } else {
                return null;
            }
        } else {
            try {
                return (SecuredStoreModule) configuration.getModuleByName(nameNode.getTextContent());
            } catch (ClassCastException e) {
                throw new ConfigurationParseError("Node " + nodeName + " type not expected!", e);
            }
        }
    }

    /**
     * Get {@link AdminDataStorage} form xml config
     *
     * @param nodeName name of node, which contains {@link AdminDataStorage} name
     * @param config   config node
     * @param required if true, throw {@link ConfigurationParseError} if node not found
     * @return null if required == false and value not found, overwise {@link AdminDataStorage} instance
     */
    @Nullable
    public static AdminDataStorage getAdminDataStorage(String nodeName, Element config, boolean required) {
        Node nameNode = config.getElementsByTagName(nodeName).item(0);
        if(nameNode == null) {
            if(required) {
                throw new ConfigurationParseError("Node " + nodeName + " not found!");
            } else {
                return null;
            }
        } else {
            try {
                return (AdminDataStorage) configuration.getModuleByName(nameNode.getTextContent());
            } catch (ClassCastException e) {
                throw new ConfigurationParseError("Node " + nodeName + " type not expected!", e);
            }
        }
    }

    /**
     * Get {@link DashboardDataHolder} form xml config
     *
     * @param nodeName name of node, which contains {@link DashboardDataHolder} name
     * @param config   config node
     * @param required if true, throw {@link ConfigurationParseError} if node not found
     * @return null if required == false and value not found, overwise {@link DashboardDataHolder} instance
     */
    @Nullable
    public static DashboardDataHolder getDashboardDataHolder(String nodeName, Element config, boolean required) {
        Node nameNode = config.getElementsByTagName(nodeName).item(0);
        if(nameNode == null) {
            if(required) {
                throw new ConfigurationParseError("Node " + nodeName + " not found!");
            } else {
                return null;
            }
        } else {
            try {
                return (DashboardDataHolder) configuration.getModuleByName(nameNode.getTextContent());
            } catch (ClassCastException e) {
                throw new ConfigurationParseError("Node " + nodeName + " type not expected!", e);
            }
        }
    }

    /**
     * Get {@link ControlSocketClientModule} form xml config
     *
     * @param nodeName name of node, which contains {@link ControlSocketClientModule} name
     * @param config   config node
     * @param required if true, throw {@link ConfigurationParseError} if node not found
     * @return null if required == false and value not found, overwise {@link ControlSocketClientModule} instance
     */
    @Nullable
    public static ControlSocketClientModule getControlSocketClient(String nodeName, Element config, boolean required) {
        Node nameNode = config.getElementsByTagName(nodeName).item(0);
        if(nameNode == null) {
            if(required) {
                throw new ConfigurationParseError("Node " + nodeName + " not found!");
            } else {
                return null;
            }
        } else {
            try {
                return (ControlSocketClientModule) configuration.getModuleByName(nameNode.getTextContent());
            } catch (ClassCastException e) {
                throw new ConfigurationParseError("Node " + nodeName + " type not expected!", e);
            }
        }
    }


    /**
     * Get {@link ControlSocketClientModule} form xml config
     *
     * @param nodeName name of node, which contains {@link ControlSocketClientModule} name
     * @param config   config node
     * @param required if true, throw {@link ConfigurationParseError} if node not found
     * @return null if required == false and value not found, overwise {@link ControlSocketClientModule} instance
     */
    @Nullable
    public static ControlSocketServerModule getControlSocketServer(String nodeName, Element config, boolean required) {
        Node nameNode = config.getElementsByTagName(nodeName).item(0);
        if(nameNode == null) {
            if(required) {
                throw new ConfigurationParseError("Node " + nodeName + " not found!");
            } else {
                return null;
            }
        } else {
            try {
                return (ControlSocketServerModule) configuration.getModuleByName(nameNode.getTextContent());
            } catch (ClassCastException e) {
                throw new ConfigurationParseError("Node " + nodeName + " type not expected!", e);
            }
        }
    }

    /**
     * Get {@link HttpServer} form xml config
     *
     * @param nodeName name of node, which contains {@link HttpServer} name
     * @param config   config node
     * @param required if true, throw {@link ConfigurationParseError} if node not found
     * @return null if required == false and value not found, overwise {@link HttpServer} instance
     */
    @Nullable
    public static HttpServer getHttpServer(String nodeName, Element config, boolean required) {
        Node nameNode = config.getElementsByTagName(nodeName).item(0);
        if(nameNode == null) {
            if(required) {
                throw new ConfigurationParseError("Node " + nodeName + " not found!");
            } else {
                return null;
            }
        } else {
            try {
                return (HttpServer) configuration.getModuleByName(nameNode.getTextContent());
            } catch (ClassCastException e) {
                throw new ConfigurationParseError("Node " + nodeName + " type not expected!", e);
            }
        }
    }

    /**
     * Get {@link java.io.File} form xml config
     *
     * @param nodeName name of node, which contains {@link java.io.File} address
     * @param config   config node
     * @param required if true, throw {@link ConfigurationParseError} if node not found
     * @return null if required == false and value not found, overwise {@link File} instance
     */
    @Nullable
    public static File getFile(String nodeName, Element config, boolean required) {
        Node nameNode = config.getElementsByTagName(nodeName).item(0);
        if(nameNode == null) {
            if(required) {
                throw new ConfigurationParseError("Node " + nodeName + " not found!");
            } else {
                return null;
            }
        } else {
            return new File(nameNode.getTextContent());
        }
    }

    /**
     * Return list form xml
     *
     * @param containerNodeName list container node name
     * @param listNode          list item node name
     * @param config            config node
     * @param required          if true, throw {@link ConfigurationParseError} if node not found
     * @return modifiable list
     */
    @SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION") //FindBugs bug :(
    @Nonnull
    public List<String> getList(String containerNodeName, String listNode, Element config, boolean required) {
        Element containerNode = (Element) config.getElementsByTagName(containerNodeName).item(0);
        if(containerNode == null) {
            if(required) {
                throw new ConfigurationParseError("Node " + containerNodeName + " not found!");
            } else {
                return Collections.emptyList();
            }
        } else {
            List<String> list = new ArrayList<>();
            NodeList elements = containerNode.getElementsByTagName(listNode);
            for(int i = 0; i < elements.getLength(); i++) {
                list.add(elements.item(i).getTextContent());
            }
            return list;
        }
    }

    /**
     * Return list form xml
     *
     * @param containerNodeName list container node name
     * @param listNode          list item node name
     * @param config            config node
     * @param required          if true, throw {@link ConfigurationParseError} if node not found
     * @return modifiable list
     */
    @SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION") //FindBugs bug :(
    @Nonnull
    public List<Element> getElementList(String containerNodeName, String listNode, Element config, boolean required) {
        Element containerNode = (Element) config.getElementsByTagName(containerNodeName).item(0);
        if(containerNode == null) {
            if(required) {
                throw new ConfigurationParseError("Node " + containerNodeName + " not found!");
            } else {
                return Collections.emptyList();
            }
        } else {
            List<Element> list = new ArrayList<>();
            NodeList elements = containerNode.getElementsByTagName(listNode);
            for(int i = 0; i < elements.getLength(); i++) {
                list.add((Element) elements.item(i));
            }
            return list;
        }
    }


    /**
     * Get {@link Element} form xml config
     *
     * @param nodeName name of node
     * @param config   config node
     * @param required if true, throw {@link ConfigurationParseError} if node not found
     * @return null if required == false and value not found, overwise {@link Element} instance
     */
    @Nullable
    public static Element getXmlElement(String nodeName, Element config, boolean required) {
        Node nameNode = config.getElementsByTagName(nodeName).item(0);
        if(nameNode == null) {
            if(required) {
                throw new ConfigurationParseError("Node " + nodeName + " not found!");
            } else {
                return null;
            }
        } else {
            return (Element) nameNode;
        }
    }


    /**
     * Get string content form xml config
     *
     * @param nodeName name of node
     * @param config   config node
     * @param required if true, throw {@link ConfigurationParseError} if node not found
     * @return null if required == false and value not found, overwise {@link String}
     */
    @Nullable
    public static String getString(String nodeName, Element config, boolean required) {
        Node nameNode = config.getElementsByTagName(nodeName).item(0);
        if(nameNode == null) {
            if(required) {
                throw new ConfigurationParseError("Node " + nodeName + " not found!");
            } else {
                return null;
            }
        } else {
            return nameNode.getTextContent();
        }
    }

    /**
     * Get int content form xml config
     *
     * @param nodeName name of node
     * @param config   config node
     * @param required if true, throw {@link ConfigurationParseError} if node not found
     * @return 0 if required == false and value not found, overwise int
     * @throws NumberFormatException if can't parse number
     */
    @Nullable
    public static int getInteger(String nodeName, Element config, boolean required, int def) {
        Node nameNode = config.getElementsByTagName(nodeName).item(0);
        if(nameNode == null) {
            if(required) {
                throw new ConfigurationParseError("Node " + nodeName + " not found!");
            } else {
                return def;
            }
        } else {
            return Integer.parseInt(nameNode.getTextContent());
        }
    }
}