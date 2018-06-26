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

package com.alesharik.webserver.extension.module;

import com.alesharik.webserver.configuration.config.lang.ConfigurationEndpoint;
import com.alesharik.webserver.configuration.config.lang.CustomEndpointSection;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationTypedObject;
import com.alesharik.webserver.configuration.extension.message.AbstractMessageManager;
import com.alesharik.webserver.configuration.extension.message.Message;
import com.alesharik.webserver.extension.module.extension.message.ChangeModuleStateMessage;
import com.alesharik.webserver.extension.module.extension.message.GetModuleManagerMessage;
import com.alesharik.webserver.extension.module.extension.message.GetSharedLibrariesManagerMessage;
import com.alesharik.webserver.extension.module.extension.message.ModulePreStateChangedMessage;
import com.alesharik.webserver.extension.module.extension.message.ModuleStateChangedMessage;
import com.alesharik.webserver.extension.module.meta.ModuleAdapter;
import com.alesharik.webserver.extension.module.meta.ScriptElementConverter;
import com.alesharik.webserver.extension.module.util.Module;
import com.alesharik.webserver.extension.module.util.SharedLibraryManager;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@RequiredArgsConstructor
final class MessageManagerImpl extends AbstractMessageManager {
    private final ModuleManagerImpl moduleManager;
    private final SharedLibraryManager sharedLibraryManager;
    private final List<String> startedModules;
    private final ScriptElementConverter converter;
    @Setter
    private ConfigurationEndpoint configurationEndpoint;

    @Override
    public void listen(Message message, String sender) {
        if(message instanceof ChangeModuleStateMessage) {
            ChangeModuleStateMessage msg = (ChangeModuleStateMessage) message;
            listenModuleStateChangeMessage(msg);
        } else if(message instanceof GetSharedLibrariesManagerMessage)
            this.sender.send(new GetSharedLibrariesManagerMessage.Reponse(sharedLibraryManager), sender);
        else if(message instanceof GetModuleManagerMessage)
            this.sender.send(new GetModuleManagerMessage.Response(moduleManager), sender);
    }

    private void listenModuleStateChangeMessage(ChangeModuleStateMessage msg) {
        ModuleManagerImpl.ModuleImpl mod = null;
        for(Module module : moduleManager.getModules()) {
            if(module.getName().equals(msg.getModule().getName())) {
                mod = (ModuleManagerImpl.ModuleImpl) module;
                break;
            }
        }
        if(mod == null)
            return;
        switch (msg.getState()) {
            case START: {
                if(mod.isRunning())
                    return;

                modulePreStarted(mod, mod.getModuleAdapter(), mod.getInstance());
                mod.getModuleAdapter().start();
                startedModules.add(mod.getName());
                moduleStarted(mod, mod.getModuleAdapter(), mod.getInstance());
                break;
            }
            case SHUTDOWN: {
                if(!mod.isRunning())
                    return;
                modulePreShutdown(mod, mod.getModuleAdapter(), mod.getInstance());
                mod.getModuleAdapter().shutdown();
                startedModules.remove(mod.getName());
                moduleShutdown(mod, mod.getModuleAdapter(), mod.getInstance());
                break;
            }
            case SHUTDOWN_NOW: {
                if(!mod.isRunning())
                    return;
                modulePreShutdownNow(mod, mod.getModuleAdapter(), mod.getInstance());
                mod.getModuleAdapter().shutdownNow();
                startedModules.remove(mod.getName());
                moduleShutdownNow(mod, mod.getModuleAdapter(), mod.getInstance());
                break;
            }
            case RELOAD: {
                if(!mod.isRunning())
                    return;

                ConfigurationTypedObject obj = null;
                for(CustomEndpointSection.UseDirective modules : configurationEndpoint.getCustomSection("modules").getUseDirectives()) {
                    if(modules.getName().equals(mod.getName()))
                        obj = modules.getConfiguration();
                }
                if(obj == null) {
                    System.err.println("Config object == null!");
                    return;
                }
                mod.getModuleAdapter().reload(obj, converter);
                moduleReload(mod, mod.getModuleAdapter(), mod.getInstance());
                break;
            }
            default:
                System.err.println("Can't parse message " + msg);
        }
    }

    public void moduleStarted(Module module, ModuleAdapter adapter, Object instance) {
        sender.broadcast(new ModuleStateChangedMessage(module, adapter, instance, ModuleStateChangedMessage.State.START));
    }

    public void moduleShutdown(Module module, ModuleAdapter adapter, Object instance) {
        sender.broadcast(new ModuleStateChangedMessage(module, adapter, instance, ModuleStateChangedMessage.State.SHUTDOWN));
    }

    public void moduleShutdownNow(Module module, ModuleAdapter adapter, Object instance) {
        sender.broadcast(new ModuleStateChangedMessage(module, adapter, instance, ModuleStateChangedMessage.State.SHUTDOWN_NOW));
    }

    public void moduleReload(Module module, ModuleAdapter adapter, Object instance) {
        sender.broadcast(new ModuleStateChangedMessage(module, adapter, instance, ModuleStateChangedMessage.State.RELOAD));
    }

    public void modulePreStarted(Module module, ModuleAdapter adapter, Object instance) {
        sender.broadcast(new ModulePreStateChangedMessage(module, adapter, instance, ModuleStateChangedMessage.State.START));
    }

    public void modulePreShutdown(Module module, ModuleAdapter adapter, Object instance) {
        sender.broadcast(new ModulePreStateChangedMessage(module, adapter, instance, ModuleStateChangedMessage.State.SHUTDOWN));
    }

    public void modulePreShutdownNow(Module module, ModuleAdapter adapter, Object instance) {
        sender.broadcast(new ModulePreStateChangedMessage(module, adapter, instance, ModuleStateChangedMessage.State.SHUTDOWN_NOW));
    }
}
