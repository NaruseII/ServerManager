package fr.naruse.servermanager.proxy.common;

import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.api.events.IEvent;
import fr.naruse.servermanager.core.api.events.server.ServerPostRegisterEvent;
import fr.naruse.servermanager.core.api.events.server.ServerRegisterEvent;
import fr.naruse.servermanager.core.config.Configuration;
import fr.naruse.servermanager.core.connection.packet.*;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import fr.naruse.servermanager.proxy.bungee.api.ServerManagerBungeeEvent;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ProxyListeners {

    public static boolean onServerRegisterEvent(ServerPostRegisterEvent e){
        Server server = e.getServer();
        if(server.getCoreServerType().is(CoreServerType.BUKKIT_MANAGER, CoreServerType.SPONGE_MANAGER)){
            return true;
        }
        return false;
    }

    public static void processTeleportToLocation(PacketTeleportToLocation packet) {
        Optional<Server> optional = ServerList.findPlayerServer(new CoreServerType[]{CoreServerType.BUKKIT_MANAGER, CoreServerType.SPONGE_MANAGER}, packet.getPlayerName());
        if(optional.isPresent()){
            optional.get().sendPacket(packet);
        }
    }

    public static void processTeleportToPlayer(ProcessPacketListener listener, PacketTeleportToPlayer packet) {
        Optional<Server> optional = ServerList.findPlayerBukkitOrSpongeServer(packet.getTargetName());
        if(optional.isPresent()){
            listener.processSwitchServer(new PacketSwitchServer(optional.get(), packet.getPlayerName()));
            optional.get().sendPacket(packet);
        }
    }

    public static List<Server> sortServers(Configuration configuration){
        Configuration.ConfigurationSection configSection = configuration.getSection("config.yml");
        Configuration.ConfigurationSection prioritiesSection = configSection.getSection("priorities");

        ServerList.SortType sortType = ServerList.SortType.valueOf(prioritiesSection.get("sortType"));

        List<Server> list = ServerList.getAll(false).stream().filter(server -> server.getCoreServerType().is(CoreServerType.BUKKIT_MANAGER, CoreServerType.SPONGE_MANAGER)).collect(Collectors.toList());
        ServerList.sort(list, sortType);

        return list;
    }

    public static Optional<Server> findDefaultServer(Configuration configuration){
        Configuration.ConfigurationSection configSection = configuration.getSection("config.yml");
        Configuration.ConfigurationSection prioritiesSection = configSection.getSection("priorities");

        CoreServerType[] defaultServerTypes = new CoreServerType[]{CoreServerType.BUKKIT_MANAGER, CoreServerType.SPONGE_MANAGER};
        ServerList.SortType sortType = ServerList.SortType.valueOf(prioritiesSection.get("sortType"));
        String forceTemplate = prioritiesSection.get("forceOnTemplate");

        return ServerList.findServer(defaultServerTypes, sortType, forceTemplate);
    }
}
