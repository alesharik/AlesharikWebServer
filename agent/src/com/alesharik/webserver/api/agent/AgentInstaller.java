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

package com.alesharik.webserver.api.agent;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;

/**
 * Install AlesharikWebServer Agent on the VM
 */
final class AgentInstaller {
    public static void install() {
        try {
            File currentJar = new File(Agent.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
            int p = nameOfRunningVM.indexOf('@');
            String pid = nameOfRunningVM.substring(0, p);

            VirtualMachine virtualMachine = VirtualMachine.attach(pid);
            virtualMachine.loadAgent(currentJar.toString(), "");
            virtualMachine.detach();
        } catch (URISyntaxException | IOException | AgentInitializationException | AgentLoadException e) {
            e.printStackTrace();
        } catch (AttachNotSupportedException e) {
            throw new RuntimeException("Dynamic agent load not supported!");
        }
    }
}
