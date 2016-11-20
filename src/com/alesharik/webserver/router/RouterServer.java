package com.alesharik.webserver.router;

import com.alesharik.webserver.api.server.Server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

//TODO use Linux asyncronous socket

/**
 * Commands:<ol>
 * <li>get - args: microserviceName(String), return ip of microservice server, 404 if microservice not found, 500 - internal server error</li>
 * <li>add - args: microservices (String[])(sample: <code>[first, second, third]</code>), return 200 if server added, 500 - internal server error</li>
 * <li>remove - return 200 if server removed, 404 if server not found, 5oo - internal error</li>
 * </ol>
 */
public final class RouterServer extends Server {
    static final String OK = "200";
    static final String NOT_FOUND = "404";
    static final String INTERNAL_ERROR = "500";

    private final RouterServerRequestProcessor processor;
    private final Servers servers;

    public RouterServer(int port, String host, int threadCount) {
        super(host, port);
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

        //TODO Optimize
        public boolean containsServer(String server) {
            AtomicBoolean atomicBoolean = new AtomicBoolean(false);
            servers.forEachValue(1, strings -> strings.forEach(s -> {
                if(s.equals(server)) {
                    atomicBoolean.set(true);
                }
            }));
            return atomicBoolean.get();
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
