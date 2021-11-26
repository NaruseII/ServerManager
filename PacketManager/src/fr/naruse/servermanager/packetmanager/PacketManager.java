package fr.naruse.servermanager.packetmanager;

import fr.naruse.servermanager.core.*;
import fr.naruse.api.config.Configuration;
import fr.naruse.servermanager.core.connection.packet.PacketShutdown;
import fr.naruse.api.logging.GlobalLogger;
import fr.naruse.servermanager.core.utils.Utils;
import fr.naruse.servermanager.packetmanager.database.MongoAPI;
import fr.naruse.servermanager.core.plugin.Plugins;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import fr.naruse.servermanager.core.utils.Updater;
import fr.naruse.servermanager.packetmanager.command.PacketManagerCommand;
import fr.naruse.servermanager.packetmanager.database.Database;
import fr.naruse.servermanager.packetmanager.event.PacketManagerEventListener;
import fr.naruse.servermanager.packetmanager.packet.PacketManagerPacketListener;
import fr.naruse.servermanager.packetmanager.utils.Metrics;

import java.io.File;

public class PacketManager {

    private static PacketManager instance;
    public static PacketManager get() {
        return instance;
    }

    public static void main(String[] args) {
        long millis  = System.currentTimeMillis();
        GlobalLogger.info("Starting PacketManager...");
        if(Updater.needToUpdate(CoreServerType.PACKET_MANAGER)){
            return;
        }
        new PacketManager(millis);
    }

    private final ServerManager serverManager;
    private final Database database;
    private Configuration mongoConfiguration;

    public PacketManager(long millis) {
        instance = this;
        this.serverManager = new ServerManager(new CoreData(CoreServerType.PACKET_MANAGER, new File("configs"), "packet-manager", 4848)) {
            @Override
            public void shutdown() {
                boolean loop = false;
                int size = (int) ServerList.getAll().stream().filter(server -> server.getCoreServerType().is(CoreServerType.FILE_MANAGER)).count();

                if (size == 0) {
                    if(ServerList.getAll(false).size() != 0){
                        ServerList.getAll(false).forEach(server -> server.sendPacket(new PacketShutdown()));
                        GlobalLogger.warn("---------------------------------------------------------------------------------------------------------------------------");
                        GlobalLogger.warn("Why can't I find File-Manager ? " + size + " servers are still alive! How is that possible ?");
                        GlobalLogger.warn("You shouldn't start server without using File-Manager!");
                        GlobalLogger.warn("Waiting for Servers to stop...");
                        GlobalLogger.warn("---------------------------------------------------------------------------------------------------------------------------");
                        loop = true;
                    }
                } else {
                    for (Server server : ServerList.findServer(CoreServerType.FILE_MANAGER)) {
                        server.sendPacket(new PacketShutdown());
                        GlobalLogger.info("Waiting for File-Manager '" + server.getName() + "' to stop...");
                        loop = true;
                    }
                }

                if (loop) {
                    while (ServerList.getSize() > 1) {
                        Utils.sleep(1000);
                    }
                    Utils.sleep(10000);
                }

                database.save();

                if(MongoAPI.get() != null){
                    MongoAPI.get().shutdown();
                }

                super.shutdown();
            }
        };

        this.mongoConfiguration = new Configuration(new File(this.serverManager.getCoreData().getDataFolder(), "mongodb.json"), true);
        if((boolean) this.mongoConfiguration.get("enabled")){
            new MongoAPI(this.mongoConfiguration);
        }

        this.database = new Database();

        this.serverManager.registerEventListener(new PacketManagerEventListener());
        this.serverManager.registerPacketProcessing(new PacketManagerPacketListener());

        PacketManagerCommand packetManagerCommand = new PacketManagerCommand(this);
        Plugins.loadPlugins();

        new Metrics(serverManager, 11607);

        GlobalLogger.info("Start done! (It took " + (System.currentTimeMillis() - millis) + "ms)");

        GlobalLogger.info("");
        GlobalLogger.info("Type help to see commands");

        packetManagerCommand.run();
    }

    public ServerManager getServerManager() {
        return serverManager;
    }

    public Database getDatabase() {
        return database;
    }
}
