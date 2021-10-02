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

    public static Server createNewServer(String name, int port, String host, int serverManagerPort, CoreServerType coreServerType){
        if(map.containsKey(name)){
            return null;
        }

        Server server = new Server(name, port, host, serverManagerPort, coreServerType);
        if(server.equals(ServerManager.get().getCurrentServer())){
            return null;
        }

        ServerRegisterEvent event = new ServerRegisterEvent(server);
        ServerManager.get().getPlugin().callEvent(event);
        if(event.isCancelled()){
            return null;
        }

        if(ServerManager.get().getCoreData().getCoreServerType().is(CoreServerType.PACKET_MANAGER, CoreServerType.BUNGEE_MANAGER, CoreServerType.VELOCITY_MANAGER)){
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
            if(ServerManager.get().getCoreData().getCoreServerType().is(CoreServerType.PACKET_MANAGER, CoreServerType.BUNGEE_MANAGER, CoreServerType.VELOCITY_MANAGER)) {
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

    public static Optional<Server> findServer(CoreServerType[] coreServerTypes, SortType sortType, String nameStartsWith){
        return findServer(coreServerTypes, sortType, nameStartsWith, server -> true);
    }

    public static Optional<Server> findServer(CoreServerType[] coreServerTypes, SortType sortType, String nameStartsWith, Predicate<Server> predicate){
        return getAll().stream().filter(server -> server.getCoreServerType().is(coreServerTypes) && server.getName().startsWith(nameStartsWith)).sorted((o1, o2) -> {
            if(o1.getData().getPlayerSize() > o2.getData().getPlayerSize()){
                return sortType == SortType.FIND_MOST_POPULATED ? -1 : 1;
            }else if(o1.getData().getPlayerSize() < o2.getData().getPlayerSize()){
                return sortType == SortType.FIND_MOST_POPULATED ? 1 : -1;
            }
            return 0;
        }).filter(predicate).findFirst();
    }

    public static Optional<Server> findPlayerBukkitServer(String playerName){
        return findPlayerServer(CoreServerType.BUKKIT_MANAGER, playerName);
    }

    public static Optional<Server> findPlayerSpongeServer(String playerName){
        return findPlayerServer(CoreServerType.SPONGE_MANAGER, playerName);
    }

    public static Optional<Server> findPlayerNukkitServer(String playerName){
        return findPlayerServer(CoreServerType.NUKKIT_MANAGER, playerName);
    }

    public static Optional<Server> findPlayerBukkitOrSpongeServer(String playerName){
        return findPlayerServer(new CoreServerType[]{CoreServerType.BUKKIT_MANAGER, CoreServerType.SPONGE_MANAGER}, playerName);
    }

    public static Optional<Server> findPlayerBukkitOrSpongeOrNukkitServer(String playerName){
        return findPlayerServer(new CoreServerType[]{CoreServerType.BUKKIT_MANAGER, CoreServerType.SPONGE_MANAGER, CoreServerType.NUKKIT_MANAGER}, playerName);
    }

    public static Optional<Server> findPlayerProxyServer(String playerName){
        return findPlayerServer(new CoreServerType[]{CoreServerType.BUNGEE_MANAGER, CoreServerType.VELOCITY_MANAGER}, playerName);
    }


    public static Optional<Server> findPlayerNukkitServer(UUID uuid){
        return findPlayerServer(CoreServerType.NUKKIT_MANAGER, uuid);
    }

    public static Optional<Server> findPlayerSpongeServer(UUID uuid){
        return findPlayerServer(CoreServerType.SPONGE_MANAGER, uuid);
    }

    public static Optional<Server> findPlayerBukkitServer(UUID uuid){
        return findPlayerServer(CoreServerType.BUKKIT_MANAGER, uuid);
    }

    public static Optional<Server> findPlayerBukkitOrSpongeServer(UUID uuid){
        return findPlayerServer(new CoreServerType[]{CoreServerType.BUKKIT_MANAGER, CoreServerType.SPONGE_MANAGER}, uuid);
    }

    public static Optional<Server> findPlayerBukkitOrSpongeOrNukkitServer(UUID uuid){
        return findPlayerServer(new CoreServerType[]{CoreServerType.BUKKIT_MANAGER, CoreServerType.SPONGE_MANAGER, CoreServerType.NUKKIT_MANAGER}, uuid);
    }


    public static Optional<Server> findPlayerProxyServer(UUID uuid){
        return findPlayerServer(new CoreServerType[]{CoreServerType.BUNGEE_MANAGER, CoreServerType.VELOCITY_MANAGER}, uuid);
    }

    public static Optional<String> getPlayerNameByUUID(UUID uuid){
        Optional<Server> optional = findPlayerServer(CoreServerType.BUKKIT_MANAGER, uuid);
        return optional.isPresent() ? Optional.of(optional.get().getData().getByUUID(uuid)) : Optional.empty();
    }

    public static Optional<Server> findPlayerServer(CoreServerType coreServerType, String playerName){
        return findServer(coreServerType).stream().filter(server -> server.getData().containsPlayer(playerName)).findFirst();
    }

    public static Optional<Server> findPlayerServer(CoreServerType[] coreServerTypes, String playerName){
        return findServer(coreServerTypes).stream().filter(server -> server.getData().containsPlayer(playerName)).findFirst();
    }

    public static Optional<Server> findPlayerServer(CoreServerType coreServerType, UUID uuid){
        return findServer(coreServerType).stream().filter(server -> server.getData().containsPlayer(uuid)).findFirst();
    }

    public static Optional<Server> findPlayerServer(CoreServerType[] coreServerTypes, UUID uuid){
        return findServer(coreServerTypes).stream().filter(server -> server.getData().containsPlayer(uuid)).findFirst();
    }

    public static Set<Server> findServers(String baseName){
        return findServers(baseName, false);
    }

    public static Set<Server> findServers(String baseName, boolean includeCurrent){
        return getAll(includeCurrent).stream().filter(server -> server.getName().startsWith(baseName)).collect(Collectors.toSet());
    }

    public static boolean isPlayerOnline(String playerName){
        return findPlayerBukkitServer(playerName).isPresent();
    }

    public static boolean isPlayerOnline(UUID uuid){
        return findPlayerBukkitServer(uuid).isPresent();
    }

    public static Server getByName(String name){
        return getByName(name, true);
    }

    public static Server getByName(String name, boolean addCurrentServer){
        Optional<Server> optionalServer = getByNameOptional(name, addCurrentServer);
        return optionalServer.isPresent() ? optionalServer.get() : null;
    }

    public static Optional<Server> getByNameOptional(String name){
        return getByNameOptional(name, true);
    }

    public static Optional<Server> getByNameOptional(String name, boolean addCurrentServer){
        Server server = map.get(name);
        if(server == null && addCurrentServer){
            if(ServerManager.get().getCurrentServer().getName().equals(name)){
                server = ServerManager.get().getCurrentServer();
            }
        }
        return server == null ? Optional.empty() : Optional.of(server);
    }

    public static int getSize() {
        return map.size();
    }

    public static Set<Server> getAll(){
        return getAll(true);
    }

    public static Set<Server> getAll(boolean addCurrentServer){
        Set<Server> set = new HashSet<>(map.values());
        if(addCurrentServer){
            set.add(ServerManager.get().getCurrentServer());
        }
        return set;
    }

    public static List<Server> sort(List<Server> list, SortType sortType){
        return list.stream().sorted((o1, o2) -> {
            if(o1.getData().getPlayerSize() > o2.getData().getPlayerSize()){
                return sortType == SortType.FIND_MOST_POPULATED ? 1 : -1;
            }else if(o1.getData().getPlayerSize() < o2.getData().getPlayerSize()){
                return sortType == SortType.FIND_MOST_POPULATED ? -1 : 1;
            }
            return 0;
        }).collect(Collectors.toList());
    }

    public static Set<String> getAllNames(){
        return new HashSet<>(map.keySet());
    }

    public enum SortType {

        FIND_MOST_POPULATED,
        FIND_LEAST_POPULATED,

    }
}
