package com.alesharik.webserver.configuration;

import com.alesharik.webserver.control.AdminDataStorage;
import com.alesharik.webserver.exceptions.error.ConfigurationParseError;
import com.alesharik.webserver.module.server.SecuredStoreModule;
import lombok.experimental.UtilityClass;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.annotation.Nullable;
import java.io.File;

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
     * @param required if true, throw {@link ConfigurationParseError} if not found
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
     * @param required if true, throw {@link ConfigurationParseError} if not found
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
     * Get {@link java.io.File} form xml config
     *
     * @param nodeName name of node, which contains {@link java.io.File} address
     * @param config   config node
     * @param required if true, throw {@link ConfigurationParseError} if not found
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
}