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

package com.alesharik.webserver.api.server.wrapper;

import com.alesharik.webserver.api.server.wrapper.addon.AddOnManager;
import com.alesharik.webserver.api.server.wrapper.addon.ConditionException;
import lombok.experimental.UtilityClass;
import org.glassfish.grizzly.http.server.AddOn;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;

@UtilityClass
public final class GrizzlyConverter {
    public static PortRange getPortRange(org.glassfish.grizzly.PortRange portRange) {
        return new PortRange(portRange.getLower(), portRange.getUpper());
    }

    public static org.glassfish.grizzly.PortRange getPortRange(PortRange portRange) {
        return new org.glassfish.grizzly.PortRange(portRange.getLower(), portRange.getUpper());
    }

    public static NetworkListener configureNetworkListener(NetworkListenerConfiguration configuration) {
        NetworkListener networkListener = new NetworkListener(configuration.getName(), configuration.getHost(), getPortRange(configuration.getPortRange()));
        if(configuration.getAddonNames().size() > 0) {
            for(String s : configuration.getAddonNames()) {
                try {
                    AddOn addOn = AddOnManager.getAddonForName(s, configuration, AddOn.class);
                    networkListener.registerAddOn(addOn);
                } catch (ClassCastException e) {
                    System.err.println("Http server module " + s + " can't be casted to grizzly AddOn");
                } catch (ConditionException e) {
                    if(e.getCause() == null)
                        System.err.println("Http server module condition deny class creation!");
                    else {
                        System.err.println("Http server module condition throw an exception!");
                        e.getCause().printStackTrace();
                    }
                } catch (IllegalArgumentException e) {
                    System.err.println("Http server module " + s + " not found!");
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
        }

        if(configuration.getCompression() != null)
            networkListener.setCompression(configuration.getCompression());
        if(configuration.getCompressableMimeTypes().size() > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            configuration.getCompressableMimeTypes().forEach(s -> {
                if(stringBuilder.length() != 0)
                    stringBuilder.append(',').append(s);
                else
                    stringBuilder.append(s);
            });
        }

        if(configuration.getSslConfiguration() != null) {
            SSLConfiguration sslConfiguration = configuration.getSslConfiguration();

            SSLContextConfigurator configurator = new SSLContextConfigurator();

            configurator.setKeyStoreFile(sslConfiguration.getConfiguration().getFile().getPath());
            configurator.setKeyStorePass(sslConfiguration.getConfiguration().getPassword());

            SSLEngineConfigurator engineConfigurator = new SSLEngineConfigurator(configurator);
            networkListener.setSecure(true);
            networkListener.setSSLEngineConfig(engineConfigurator);
        }
        return networkListener;
    }
}
