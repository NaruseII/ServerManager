package fr.naruse.servermanager.core.connection;

import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.connection.packet.PacketServerList;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.connection.packet.PacketKeepAlive;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class KeepAliveServerThread {

    private static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();

    public static void launch(ServerManager serverManager) {
        EXECUTOR_SERVICE.scheduleAtFixedRate(() -> {
            if(serverManager.getCoreData().getCoreServerType() == CoreServerType.PACKET_MANAGER){
                Set<Server> set = ServerList.getAll();
                for (Server server : ServerList.getAll()) {
                    Integer integer = server.getData().get("countBeforeDelete");
                    if(integer == null){
                        server.getData().set("countBeforeDelete", 3);
                    }else{
                        if(integer == 0){
                            ServerList.deleteServer(server.getName());
                            ServerManagerLogger.warn("Server '"+server.getName()+"' didn't respond for 9 seconds! Did it crash ?");
                            continue;
                        }
                        server.getData().set("countBeforeDelete", integer-1);
                    }
                    server.sendPacket(new PacketServerList(set));
                }
            }else{
                serverManager.getConnectionManager().sendPacket(new PacketKeepAlive(serverManager.getCurrentServer()));
            }
        }, 3, 3, TimeUnit.SECONDS);
    }

    public static void shutdown(){
        ServerManagerLogger.info("Stopping KeepAlive thread...");
        EXECUTOR_SERVICE.shutdown();
    }
}
