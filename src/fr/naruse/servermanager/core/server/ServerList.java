package fr.naruse.servermanager.core.server;

import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.api.events.server.ServerDeleteEvent;
import fr.naruse.servermanager.core.api.events.server.ServerPostDeleteEvent;
import fr.naruse.servermanager.core.api.events.server.ServerPostRegisterEvent;
import fr.naruse.servermanager.core.api.events.server.ServerRegisterEvent;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ServerList {

    private static final Map<String, Server> map = new HashMap<>();

    public static Server createNewServer(String name, int port, int serverManagerPort, CoreServerType coreServerType){
        if(map.containsKey(name)){
            return null;
        }

        Server server = new Server(name, port, serverManagerPort, coreServerType);
        if(server.equals(ServerManager.get().getCurrentServer())){
            return null;
        }

        ServerRegisterEvent event = new ServerRegisterEvent(server);
        ServerManager.get().getPlugin().callEvent(event);
        if(event.isCancelled()){
            return null;
        }

        if(ServerManager.get().getCoreData().getCoreServerType().is(CoreServerType.PACKET_MANAGER, CoreServerType.BUNGEE_MANAGER)){
            ServerManagerLogger.info("Registering server '"+name+"' -> ["+server.getAddress().getHostAddress()+"] Port: "+port+" SMPort: "+serverManagerPort);
        }

        map.put(name, server);

        ServerManager.get().getPlugin().callEvent(new ServerPostRegisterEvent(server));

        return server;
    }

    public static void deleteServer(String name) {
        Server server = map.get(name);
        if(server != null) {

            ServerManager.get().getPlugin().callEvent(new ServerDeleteEvent(server));

            map.remove(name);
            if(ServerManager.get().getCoreData().getCoreServerType().is(CoreServerType.PACKET_MANAGER, CoreServerType.BUNGEE_MANAGER)) {
                ServerManagerLogger.info("Deleting server '" + name + "' -> ["+server.getAddress().getHostAddress()+"] Port: "+server.getPort()+" SMPort: "+server.getServerManagerPort());
            }

            ServerManager.get().getPlugin().callEvent(new ServerPostDeleteEvent(server));
        }
    }

    public static Set<Server> findServer(CoreServerType... coreServerTypes){
        return getAll().stream().filter(server -> server.getCoreServerType().is(coreServerTypes)).collect(Collectors.toSet());
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
        return findServer(coreServerType, sortType, nameStartsWith, server -> true);
    }

    public static Optional<Server> findServer(CoreServerType coreServerType, SortType sortType, String nameStartsWith, Predicate<Server> predicate){
        return findServer(new CoreServerType[]{coreServerType}, sortType, nameStartsWith, predicate);
    }

    public static Optional<Server> findServer(CoreServerType[] coreServerTypes, SortType sortType, String nameStartsWith, Predicate<Server> predicate){
        return getAll().stream().filter(server -> server.getCoreServerType().is(coreServerTypes) && server.getName().startsWith(nameStartsWith)).sorted((o1, o2) -> {
            if(o1.getData().getPlayerSize() > o2.getData().getPlayerSize()){
                return sortType == SortType.FIND_MOST_POPULATED ? 1 : -1;
            }else if(o1.getData().getPlayerSize() < o2.getData().getPlayerSize()){
                return sortType == SortType.FIND_MOST_POPULATED ? -1 : 1;
            }
            return 0;
        }).filter(predicate).findFirst();
    }

    public static Optional<Server> findPlayerBukkitServer(String playerName){
        return findPlayerServer(CoreServerType.BUKKIT_MANAGER, playerName);
    }

    public static Optional<Server> findPlayerBungeeServer(String playerName){
        return findPlayerServer(CoreServerType.BUNGEE_MANAGER, playerName);
    }

    public static Optional<Server> findPlayerBukkitServer(UUID uuid){
        return findPlayerServer(CoreServerType.BUKKIT_MANAGER, uuid);
    }

    public static Optional<Server> findPlayerBungeeServer(UUID uuid){
        return findPlayerServer(CoreServerType.BUNGEE_MANAGER, uuid);
    }

    private static Optional<String> getPlayerNameByUUID(UUID uuid){
        Optional<Server> optional = findPlayerServer(CoreServerType.BUKKIT_MANAGER, uuid);
        return optional.isPresent() ? Optional.of(optional.get().getData().getByUUID(uuid)) : Optional.empty();
    }

    private static Optional<Server> findPlayerServer(CoreServerType coreServerType, String playerName){
        return findServer(coreServerType).stream().filter(server -> server.getData().containsPlayer(playerName)).findFirst();
    }

    private static Optional<Server> findPlayerServer(CoreServerType coreServerType, UUID uuid){
        return findServer(coreServerType).stream().filter(server -> server.getData().containsPlayer(uuid)).findFirst();
    }

    public static boolean isPlayerOnline(String playerName){
        return findPlayerBukkitServer(playerName).isPresent();
    }

    public static boolean isPlayerOnline(UUID uuid){
        return findPlayerBukkitServer(uuid).isPresent();
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
