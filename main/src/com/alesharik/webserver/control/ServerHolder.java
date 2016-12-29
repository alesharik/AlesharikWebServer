package com.alesharik.webserver.control;

import com.alesharik.webserver.control.dataStorage.AdminDataStorageImpl;

import java.util.ArrayList;
import java.util.Arrays;

public class ServerHolder {
    private AdminDataStorageImpl holder;
    private ArrayList<String> servers = new ArrayList<>();
    private ArrayList<ServerController> controllers = new ArrayList<>();

    public ServerHolder(AdminDataStorageImpl holder) {
        this.holder = holder;
        try {
            read();
        } catch (NullPointerException e) {

        }
    }

    private void read() throws NullPointerException {
        parseString(((String) this.holder.get("servers")));
    }

    private void write() {
        this.holder.put("servers", asString());
    }

    public void addServer(String serverAddress, int index) {
        if(Integer.compare(index, servers.size()) == 0) {
            servers.add(serverAddress);
            controllers.add(new ServerController());
            return;
        }
        if(index < servers.size()) {
            servers.set(index, serverAddress);
            controllers.set(index, new ServerController());
        }
        write();
    }

    public void removeServer(String serverAddress) {
        this.controllers.remove(this.servers.indexOf(serverAddress));
        this.servers.remove(serverAddress);
        write();
    }

    public void removeServer(int index) {
        this.controllers.remove(index);
        this.servers.remove(index);
        write();
    }

    public boolean exists(String serverAddress) {
        return this.servers.contains(serverAddress);
    }

    public boolean exists(int index) {
        return this.servers.size() > index && this.servers.get(index) != null;
    }

    public int indexOf(String serverAddress) {
        return this.servers.indexOf(serverAddress);
    }

    public String getServer(int index) {
        return this.servers.get(index);
    }

    public WebSocketClientEndpoint connect(String serverAddress, String login, String password) throws Exception {
        return controllers.get(servers.indexOf(serverAddress)).connect(serverAddress, login, password);
    }

    private void parseString(String srt) {
        Arrays.asList(srt.split(",")).forEach(servers::add);
    }

    private String asString() {
        StringBuilder sb = new StringBuilder();
        servers.forEach(s -> {
            sb.append(s);
            sb.append(",");
        });
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
}
