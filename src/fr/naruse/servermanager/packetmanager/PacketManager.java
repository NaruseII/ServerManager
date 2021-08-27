package fr.naruse.servermanager.packetmanager;

import fr.naruse.servermanager.core.*;
import fr.naruse.servermanager.core.connection.packet.PacketShutdown;
import fr.naruse.servermanager.core.database.Database;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import fr.naruse.servermanager.core.utils.Updater;
import fr.naruse.servermanager.packetmanager.command.PacketManagerCommand;
import fr.naruse.servermanager.packetmanager.packet.PacketManagerProcessPacketListener;

import java.io.File;

public class PacketManager {

    public static void main(String[] args) {
        long millis  = System.currentTimeMillis();
        ServerManagerLogger.info("Starting PacketManager...");
        if(Updater.needToUpdate(CoreServerType.PACKET_MANAGER)){
            return;
        }
        new PacketManager(millis);
    }

    private final ServerManager serverManager;
    private final Database database = new Database();

    public PacketManager(long millis) {

        this.serverManager = new ServerManager(new CoreData(CoreServerType.PACKET_MANAGER, new File("configs"), 4848, "packet-manager", 4848)) {
            @Override
            public void shutdown() {
                boolean loop = false;
                int size = (int) ServerList.getAll().stream().filter(server -> server.getCoreServerType().is(CoreServerType.FILE_MANAGER)).count();

                if (size == 0) {
                    if(ServerList.getAll(false).size() != 0){
                        ServerList.getAll(false).forEach(server -> server.sendPacket(new PacketShutdown()));
                        ServerManagerLogger.warn("---------------------------------------------------------------------------------------------------------------------------");
                        ServerManagerLogger.warn("Why can't I find File-Manager ? " + size + " servers are still alive! How is that possible ?");
                        ServerManagerLogger.warn("You shouldn't start server without using File-Manager!");
                        ServerManagerLogger.warn("Waiting for Servers to stop...");
                        ServerManagerLogger.warn("---------------------------------------------------------------------------------------------------------------------------");
                        loop = true;
                    }
                } else {
                    for (Server server : ServerList.findServer(CoreServerType.FILE_MANAGER)) {
                        server.sendPacket(new PacketShutdown());
                        ServerManagerLogger.info("Waiting for File-Manager '" + server.getName() + "' to stop...");
                        loop = true;
                    }
                }

                if (loop) {
                    try {
                        while (ServerList.getSize() > 1) {
                            Thread.sleep(1000);
                        }
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                super.shutdown();
            }
        };
        this.serverManager.registerPacketProcessing(new PacketManagerProcessPacketListener(this));

        new Metrics(serverManager, 11607);

        ServerManagerLogger.info("Start done! (It took " + (System.currentTimeMillis() - millis) + "ms)");

        ServerManagerLogger.info("");
        ServerManagerLogger.info("Type help to see commands");

        new PacketManagerCommand(this).run();
    }

    public ServerManager getServerManager() {
        return serverManager;
    }

    public Database getDatabase() {
        return database;
    }
}
