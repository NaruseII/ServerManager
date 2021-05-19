package fr.naruse.servermanager.packetmanager;

import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import fr.naruse.servermanager.core.connection.packet.PacketKeepAlive;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;

import java.util.HashMap;
import java.util.Map;

public class KeepAliveBuffer {

    private static final ServerManagerLogger.Logger LOGGER = new ServerManagerLogger.Logger("KeepAliveBuffer");

    private static final Map<Server, PacketKeepAlive> map = new HashMap<>();

    public static void put(PacketKeepAlive packet){
        Server server = ServerList.getByName(packet.getName());
        if(server == null){
            server = ServerList.createNewServer(packet.getName(), packet.getPort(), packet.getCoreServerType());
            if(server == null){
                return;
            }
        }
        if(map.containsKey(server)) {
            process();
        }
        map.put(server, packet);
        if(map.size() >= ServerList.getSize()){
            process();
        }
    }

    private static void process(){
        map.forEach((server, packet) -> {
            server.getData().set("countBeforeDelete", 3);
            server.getData().setCapacity(packet.getCapacity());
            server.getData().setUUIDByNameMap(packet.getUUIDByNameMap());
            server.getData().setDataMap(packet.getDataMap());
        });

        map.clear();
    }

}
