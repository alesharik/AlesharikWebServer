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

package com.alesharik.webserver.router;
import com.alesharik.webserver.logger.Prefixes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

//TODO use Linux asyncronous socket

/**
 * Commands:<ol>
 * <li>get - args: microserviceName(String), return ip of microservice server, 404 if microservice not found, 500 - internal server error</li>
 * <li>add - args: microservices (String[])(sample: <code>[first, second, third]</code>), return 200 if server added, 500 - internal server error</li>
 * <li>remove - return 200 if server removed, 404 if server not found, 5oo - internal error</li>
 * </ol>
 */
@Prefixes("[RouterServer]")
public final class RouterServer {
    static final String OK = "200";
    static final String NOT_FOUND = "404";
    static final String INTERNAL_ERROR = "500";

    private final RouterServerRequestProcessor processor;
    private final Servers servers;

    public RouterServer(int port, String host, int threadCount) {
        servers = new Servers();
        processor = new RouterServerRequestProcessor(port, host, threadCount, servers);
    }

    public void start() {
        processor.start();
    }

    public void shutdown() throws IOException {
        processor.shutdown();
    }

    /**
     * This class used for hold servers
     */
    static final class Servers {
        //Microservice - server ips
        private final ConcurrentHashMap<String, ArrayList<String>> servers;

        public Servers() {
            servers = new ConcurrentHashMap<>();
        }

        public void addServer(String server, String[] microservices) {
            for(String microservice : microservices) {
                if(servers.containsKey(microservice)) {
                    ArrayList<String> arr = servers.get(microservice);
                    if(!arr.contains(server)) {
                        arr.add(server);
                    }
                } else {
                    ArrayList<String> arr = new ArrayList<>();
                    arr.add(server);
                    servers.put(microservice, arr);
                }
            }
        }

        public void removeServer(String server) {
            servers.entrySet().stream()
                    .filter(stringArrayListEntry -> stringArrayListEntry.getValue() != null)
                    .filter(stringArrayListEntry -> {
                        stringArrayListEntry.getValue().stream()
                                .filter(server::equals)
                                .forEach(stringArrayListEntry.getValue()::remove);
                        return stringArrayListEntry.getValue().isEmpty();
                    }).forEach(stringArrayListEntry -> servers.remove(stringArrayListEntry.getKey()));
        }

        public boolean containsServer(String server) {
            Enumeration<ArrayList<String>> enumeration = servers.elements();
            while(enumeration.hasMoreElements()) {
                ArrayList<String> arrayList = enumeration.nextElement();
                for(String server1 : arrayList) {
                    if(server1.equals(server)) {
                        return true;
                    }
                }
            }
            return false;
        }

        public String[] getServers(String microservice) {
            if(servers.containsKey(microservice)) {
                ArrayList<String> arrayList = servers.get(microservice);
                return arrayList.toArray(new String[arrayList.size()]);
            } else {
                return new String[0];
            }
        }
    }
}
