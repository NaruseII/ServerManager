package fr.naruse.servermanager.packetmanager;

import fr.naruse.servermanager.core.connection.KeepAliveServerThread;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import fr.naruse.servermanager.core.connection.packet.PacketKeepAlive;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class KeepAliveBuffer {

    private static final ConcurrentMap<Server, PacketKeepAlive> map = new ConcurrentHashMap<>();

    public static void put(PacketKeepAlive packet){
        Server server = ServerList.getByName(packet.getName());
        if(server == null){
            server = ServerList.createNewServer(packet.getName(), packet.getPort(), packet.getAddress(), packet.getServerManagerPort(), packet.getCoreServerType());
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
        new HashMap<>(map).forEach((server, packet) -> {
            KeepAliveServerThread.EXECUTOR_SERVICE.submit(() -> server.getData().setCountBeforeDelete(3));
            server.getData().setCapacity(packet.getCapacity());
            server.getData().setUUIDByNameMap(packet.getUUIDByNameMap());
            server.getData().setDataMap(packet.getDataMap());
            server.setServerManagerPort(packet.getServerManagerPort());
            server.getData().setStatusSet(packet.getStatusSet());
        });

        map.clear();
    }

}
