package fr.naruse.servermanager.proxy.common;

import com.google.common.collect.Maps;
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

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ProxyListeners {

    public static void processTeleportToLocation(PacketTeleportToLocation packet) {
        Optional<Server> optional = ServerList.findPlayerServer(new CoreServerType[]{CoreServerType.BUKKIT_MANAGER, CoreServerType.SPONGE_MANAGER, CoreServerType.NUKKIT_MANAGER}, packet.getPlayerName());
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

        List<Server> list = ServerList.getAll(false).stream().filter(server -> server.getCoreServerType().is(CoreServerType.BUKKIT_MANAGER, CoreServerType.SPONGE_MANAGER, CoreServerType.NUKKIT_MANAGER)).collect(Collectors.toList());
        ServerList.sort(list, sortType);

        return list;
    }

    public static Optional<Server> findDefaultServer(Configuration configuration){
        Configuration.ConfigurationSection configSection = configuration.getSection("config.yml");
        Configuration.ConfigurationSection prioritiesSection = configSection.getSection("priorities");
        Configuration.ConfigurationSection templatesSection = prioritiesSection.getSection("forceOnTemplates");

        ServerList.SortType sortType = ServerList.SortType.valueOf(prioritiesSection.get("sortType"));

        Map<Integer, List<String>> map = new HashMap<>();
        templatesSection.getAll().forEach((template, o) -> {
            try{
                int priority = templatesSection.getInt(template);

                if(!map.containsKey(priority)){
                    map.put(priority, new ArrayList<>());
                }
                List<String> list = map.get(priority);
                list.add(template);
                map.put(priority, list);
            }catch (Exception e){}
        });

        List<Integer> list = new ArrayList<>(map.keySet());

        Collections.sort(list);
        Collections.reverse(list);

        for (Integer priority : list) {
            List<String> templateList = map.get(priority);

            for (String template : templateList) {
                List<Server> servers = ServerList.sort(new ArrayList<>(ServerList.findServers(template, false)), sortType);

                for (Server server : servers) {
                    if(server.getData().getCapacity() > server.getData().getPlayerSize()){
                        return Optional.of(server);
                    }
                }
            }
        }

        return Optional.empty();
    }
}
