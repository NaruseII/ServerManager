package fr.naruse.servermanager.core.server;

import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.api.events.server.ServerDeleteEvent;
import fr.naruse.servermanager.core.api.events.server.ServerRegisterEvent;
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

        Server server = new Server(name, port, coreServerType);
        ServerRegisterEvent event = new ServerRegisterEvent(server);
        ServerManager.get().getPlugin().callEvent(event);
        if(event.isCancelled()){
            return null;
        }

        if(ServerManager.get().getCoreData().getCoreServerType().is(CoreServerType.PACKET_MANAGER, CoreServerType.BUNGEE_MANAGER)){
            try {
                ServerManagerLogger.info("Registering server '"+name+"' -> ["+ InetAddress.getLocalHost().getHostAddress()+":"+port+"]");
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

        map.put(name, server);
        return server;
    }

    public static void deleteServer(String name, int port) {
        Server server = map.get(name);
        if(server != null) {

            ServerDeleteEvent event = new ServerDeleteEvent(server);
            ServerManager.get().getPlugin().callEvent(event);

            map.remove(name);
            if(ServerManager.get().getCoreData().getCoreServerType().is(CoreServerType.PACKET_MANAGER, CoreServerType.BUNGEE_MANAGER)) {
                try {
                    ServerManagerLogger.info("Deleting server '" + name + "' -> [" + InetAddress.getLocalHost().getHostAddress() + ":" + port + "]");
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
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
