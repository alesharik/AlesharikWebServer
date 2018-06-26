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
import com.alesharik.webserver.configuration.config.lang.ScriptEndpointSection;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationPrimitive;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationTypedObject;
import com.alesharik.webserver.configuration.extension.CommandExecutor;
import com.alesharik.webserver.configuration.extension.CommandPredicate;
import com.alesharik.webserver.extension.module.meta.ScriptElementConverter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@RequiredArgsConstructor
final class CommandExecutorImpl implements CommandExecutor {
    private final ModuleManagerImpl moduleManager;
    private final MessageManagerImpl messageManager;
    private final List<String> startedModules;
    private final ScriptElementConverter converter;
    @Setter
    private ConfigurationEndpoint configurationEndpoint;

    @Override
    public CommandPredicate getPredicate() {
        return commandName -> commandName.equals("start") || commandName.equals("shutdown") || commandName.equals("reload") || commandName.equals("restart");
    }

    @Override
    public void execute(ScriptEndpointSection.Command command) {
        switch (command.getName()) {
            case "start": {
                ModuleManagerImpl.ModuleImpl module = (ModuleManagerImpl.ModuleImpl) moduleManager.getModule(((ConfigurationPrimitive.String) command.getArg()).value());
                startedModules.add(module.getName());

                messageManager.modulePreStarted(module, module.getModuleAdapter(), module.getInstance());
                module.getModuleAdapter().start();
                messageManager.moduleStarted(module, module.getModuleAdapter(), module.getInstance());
                break;
            }
            case "shutdown": {
                ModuleManagerImpl.ModuleImpl module = (ModuleManagerImpl.ModuleImpl) moduleManager.getModule(((ConfigurationPrimitive.String) command.getArg()).value());
                startedModules.remove(module.getName());

                messageManager.modulePreShutdown(module, module.getModuleAdapter(), module.getInstance());
                module.getModuleAdapter().shutdown();
                messageManager.moduleShutdown(module, module.getModuleAdapter(), module.getInstance());
                break;
            }
            case "reload": {
                ModuleManagerImpl.ModuleImpl module = (ModuleManagerImpl.ModuleImpl) moduleManager.getModule(((ConfigurationPrimitive.String) command.getArg()).value());

                ConfigurationTypedObject obj = null;
                for(CustomEndpointSection.UseDirective modules : configurationEndpoint.getCustomSection("modules").getUseDirectives()) {
                    if(modules.getName().equals(module.getName()))
                        obj = modules.getConfiguration();
                }
                if(obj == null) {
                    System.err.println("Config object == null!");
                    return;
                }

                module.getModuleAdapter().reload(obj, converter);
                messageManager.moduleReload(module, module.getModuleAdapter(), module.getInstance());
                break;
            }
            case "restart": {
                ModuleManagerImpl.ModuleImpl module = (ModuleManagerImpl.ModuleImpl) moduleManager.getModule(((ConfigurationPrimitive.String) command.getArg()).value());

                messageManager.modulePreShutdown(module, module.getModuleAdapter(), module.getInstance());
                module.getModuleAdapter().shutdown();
                messageManager.moduleShutdown(module, module.getModuleAdapter(), module.getInstance());

                messageManager.modulePreStarted(module, module.getModuleAdapter(), module.getInstance());
                module.getModuleAdapter().start();
                messageManager.moduleStarted(module, module.getModuleAdapter(), module.getInstance());
                break;
            }
            default:
                System.err.println("Error! Command " + command + " not supported!");
        }
    }
}
