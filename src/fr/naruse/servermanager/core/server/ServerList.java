package fr.naruse.servermanager.core.server;

import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.api.events.server.ServerDeleteEvent;
import fr.naruse.servermanager.core.api.events.server.ServerRegisterEvent;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
            ServerManagerLogger.info("Registering server '"+name+"' -> ["+ server.getAddress().getHostAddress()+":"+port+"]");
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
                ServerManagerLogger.info("Deleting server '" + name + "' -> [" + server.getAddress().getHostAddress() + ":" + port + "]");
            }
        }
    }

    public static Set<Server> findServer(CoreServerType coreServerType){
        return getAll().stream().filter(server -> server.getCoreServerType() == coreServerType).collect(Collectors.toSet());
    }

    public static Optional<Server> findServer(CoreServerType coreServerType, SortType sortType){
        return getAll().stream().filter(server -> server.getCoreServerType() == coreServerType).sorted((o1, o2) -> {
            if(o1.getData().getPlayerSize() > o2.getData().getPlayerSize()){
                return sortType == SortType.FIND_MOST_POPULATED ? 1 : -1;
            }else if(o1.getData().getPlayerSize() < o2.getData().getPlayerSize()){
                return sortType == SortType.FIND_MOST_POPULATED ? -1 : 1;
            }
            return 0;
        }).findFirst();
    }

    public static Optional<Server> findServer(CoreServerType coreServerType, SortType sortType, String nameStartsWith){
        return getAll().stream().filter(server -> server.getCoreServerType() == coreServerType && server.getName().startsWith(nameStartsWith)).sorted((o1, o2) -> {
            if(o1.getData().getPlayerSize() > o2.getData().getPlayerSize()){
                return sortType == SortType.FIND_MOST_POPULATED ? 1 : -1;
            }else if(o1.getData().getPlayerSize() < o2.getData().getPlayerSize()){
                return sortType == SortType.FIND_MOST_POPULATED ? -1 : 1;
            }
            return 0;
        }).findFirst();
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

    public static Set<String> getAllNames(){
        return map.keySet();
    }

    public enum SortType {

        FIND_MOST_POPULATED,
        FIND_LEAST_POPULATED,
    }
}
