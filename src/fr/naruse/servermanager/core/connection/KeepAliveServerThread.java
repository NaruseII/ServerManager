package fr.naruse.servermanager.core.connection;

import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.connection.packet.PacketServerList;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.connection.packet.PacketKeepAlive;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;

import java.util.Set;
import java.util.concurrent.*;

public class KeepAliveServerThread {

    public static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    public static void launch(ServerManager serverManager) {
        Future future = EXECUTOR_SERVICE.scheduleAtFixedRate(() -> {
            if(serverManager.getCoreData().getCoreServerType() == CoreServerType.PACKET_MANAGER){
                Set<Server> set = ServerList.getAll(false);
                for (Server server : ServerList.getAll(false)) {

                    int integer = server.getData().getCountBeforeDelete();
                    if(integer == 0){
                        ServerList.deleteServer(server.getName());
                        ServerManagerLogger.warn("Server '"+server.getName()+"' didn't respond for 9 seconds! Did it crash ?");
                        continue;
                    }
                    server.getData().setCountBeforeDelete(integer-1);
                    server.sendPacket(new PacketServerList(set));
                }
            }else{
                serverManager.getConnectionManager().sendPacket(new PacketKeepAlive(serverManager.getCurrentServer()));
            }
        }, 3, 3, TimeUnit.SECONDS);

        EXECUTOR.submit(() -> {
            try {
                future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

    public static void shutdown(){
        ServerManagerLogger.info("Stopping KeepAlive thread...");
        EXECUTOR_SERVICE.shutdown();
    }
}
