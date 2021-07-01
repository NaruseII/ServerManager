package fr.naruse.servermanager.packetmanager;

import fr.naruse.servermanager.core.*;
import fr.naruse.servermanager.core.connection.packet.PacketShutdown;
import fr.naruse.servermanager.core.database.Database;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import fr.naruse.servermanager.core.utils.Updater;
import fr.naruse.servermanager.packetmanager.packet.PacketManagerProcessPacketListener;

import java.io.File;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.BiConsumer;

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

        this.serverManager = new ServerManager(new CoreData(CoreServerType.PACKET_MANAGER, new File("configs"), 4848, "packet-manager", 4848)){
            @Override
            public void shutdown() {
                Optional<Server> optional = ServerList.findServer(CoreServerType.FILE_MANAGER).stream().findFirst();

                boolean loop = false;
                int size = ServerList.getSize();
                if(optional.isPresent()){
                    optional.get().sendPacket(new PacketShutdown());
                    ServerManagerLogger.info("Waiting for File-Manager to stop...");
                    loop = true;
                }else if(size > 1){
                    ServerList.getAll().forEach(server -> server.sendPacket(new PacketShutdown()));
                    ServerManagerLogger.warn("---------------------------------------------------------------------------------------------------------------------------");
                    ServerManagerLogger.warn("Why can't I find File-Manager ? "+(size-1)+" servers are still alive! How is that possible ?");
                    ServerManagerLogger.warn("You shouldn't start server without using File-Manager!");
                    ServerManagerLogger.warn("Waiting for Servers to stop...");
                    ServerManagerLogger.warn("---------------------------------------------------------------------------------------------------------------------------");
                    loop = true;
                }
                if(loop){
                    try {
                        while (ServerList.getSize() > 1){
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

        ServerManagerLogger.info("Start done! (It took "+(System.currentTimeMillis()-millis)+"ms)");

        ServerManagerLogger.info("");
        ServerManagerLogger.info("Type help to see commands");

        Scanner scanner = new Scanner(System.in);
        while (true){
            String line;
            try{
                line = scanner.nextLine();
            }catch (NoSuchElementException e){
                continue;
            }

            String[] args = line.split(" ");
            if(line.startsWith("stop")){
                System.exit(0);
            }else if(line.startsWith("generateSecretKey")){
                ServerManagerLogger.info("Generation...");
                ServerManagerLogger.info("Key generated: "+this.serverManager.generateNewSecretKey());
            }else if(line.startsWith("status")){
                this.serverManager.printStatus();
            }else if(line.startsWith("database")){
                ServerManagerLogger.info("Database's datas:");
                this.database.getMap().forEach((s, dataObject) -> ServerManagerLogger.info("["+s+"] -> "+dataObject.getValue().toString()));
            }else{
                ServerManagerLogger.info("Available commands:");
                ServerManagerLogger.info("");
                ServerManagerLogger.info("-> stop (Stop server)");
                ServerManagerLogger.info("-> generateSecretKey");
                ServerManagerLogger.info("-> status");
                ServerManagerLogger.info("-> database (Show all datas)");
            }
        }
    }

    public ServerManager getServerManager() {
        return serverManager;
    }

    public Database getDatabase() {
        return database;
    }
}
