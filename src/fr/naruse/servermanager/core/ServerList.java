package fr.naruse.servermanager.core;

import fr.naruse.servermanager.core.logging.ServerManagerLogger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ServerList {

    private static final Map<String, Server> map = new HashMap<>();

    public static Server createNewServer(String name, int port, CoreServerType coreServerType){
        if(map.containsKey(name)){
            return null;
        }
        try {
            ServerManagerLogger.info("Registering server '"+name+"' -> ["+ InetAddress.getLocalHost().getHostAddress()+":"+port+"]");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        Server server = new Server(name, port, coreServerType);
        map.put(name, server);
        return server;
    }

    public static void deleteServer(String name, int port) {
        if(map.containsKey(name)){
            map.remove(name);
            try {
                ServerManagerLogger.info("Deleting server '"+name+"' -> ["+ InetAddress.getLocalHost().getHostAddress()+":"+port+"]");
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }

    public static Server getByName(String name){
        return map.get(name);
    }

    public static int getSize() {
        return map.size();
    }

    public static Set<Server> getAll(){
        return new HashSet<>(map.values());
    }
}
