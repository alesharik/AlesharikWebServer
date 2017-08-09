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

package com.alesharik.webserver.main.console.impl;

import com.alesharik.webserver.api.misc.Triple;
import com.alesharik.webserver.configuration.ConfigurationImpl;
import com.alesharik.webserver.configuration.Module;
import com.alesharik.webserver.main.console.ConsoleCommand;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.PrintStream;

public final class PluginConsoleCommand implements ConsoleCommand {
    private static volatile ConfigurationImpl config;

    public static void init(@Nonnull ConfigurationImpl config) {
        if(PluginConsoleCommand.config == null)
            PluginConsoleCommand.config = config;
    }

    @Nonnull
    @Override
    public String getName() {
        return "plugin";
    }

    @Nonnull
    @Override
    public String getDescription() {
        return "allows to control plugins";
    }

    @Override
    public void printHelp(PrintStream printStream) {
        printStream.println("plugin list - print all available plugins");
        printStream.println("plugin info ${pluginName} - print information about ${pluginName} plugin");
        printStream.println("plugin reload ${pluginName} - reloads plugin");
    }

    @Override
    public void handle(@Nonnull String command, @Nonnull PrintStream out, @Nonnull Reader reader) {
        String[] parts = command.split(" ", 3);
        if(parts.length == 1)
            printHelp(out);
        else if(parts[1].equals("list")) {
            for(Triple<Module, String, Boolean> moduleStringBooleanTriple : config.getModules()) {
                out.println("Module with type " + moduleStringBooleanTriple.getA().getName() + " named " + moduleStringBooleanTriple.getB() + (moduleStringBooleanTriple.getC() ? " is running" : " inn't running"));
            }
        } else if(parts[1].equals("info")) {
            if(parts.length < 3) {
                printHelp(out);
                return;
            }
            Triple<Module, String, Boolean> module = getModuleByName(parts[2]);
            out.println("Module - " + module.getB() + ", type - " + module.getA().getName());
            out.println("Started: " + module.getC());
            out.println("Module object: " + module.getA().toString());
        } else if(parts[1].equals("reload")) {
            if(parts.length < 3) {
                printHelp(out);
                return;
            }
            out.println("Trying to reload module " + parts[2]);
            config.reloadModule(parts[2]);
        } else
            printHelp(out);
    }

    @Nullable
    private static Triple<Module, String, Boolean> getModuleByName(String name) {
        for(Triple<Module, String, Boolean> moduleStringBooleanTriple : config.getModules()) {
            if(moduleStringBooleanTriple.getB().equals(name))
                return moduleStringBooleanTriple;
        }
        return null;
    }
}
