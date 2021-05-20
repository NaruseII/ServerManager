package fr.naruse.servermanager.packetmanager;

import fr.naruse.servermanager.core.*;
import fr.naruse.servermanager.core.connection.packet.PacketCreateServer;
import fr.naruse.servermanager.core.connection.packet.PacketShutdown;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;

import java.io.File;
import java.util.Optional;
import java.util.Scanner;

public class PacketManager {

    public static void main(String[] args) {
        new PacketManager();
    }

    private final ServerManager serverManager;

    public PacketManager() {
        long millis  = System.currentTimeMillis();
        ServerManagerLogger.info("Starting PacketManager...");

        this.serverManager = new ServerManager(new CoreData(CoreServerType.PACKET_MANAGER, new File("configs"), 4848, "packet-manager", 4848)){
            @Override
            public void shutdown() {
                Optional<Server> optional = ServerList.findServer(CoreServerType.FILE_MANAGER).stream().findFirst();

                boolean loop = false;
                int size = ServerList.getSize();
                if(optional.isPresent()){
                    optional.get().sendPacket(new PacketShutdown());
                    ServerManagerLogger.warn("---------------------------------------------------------------------------------------------------------------------------");
                    ServerManagerLogger.warn("You shouldn't stop Packet-Manager before the others!");
                    ServerManagerLogger.warn("It may cause bugs, servers might not be deleted and server might also no stop and run in background!");
                    ServerManagerLogger.warn("Waiting for File-Manager to stop...");
                    ServerManagerLogger.warn("---------------------------------------------------------------------------------------------------------------------------");
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
                    while (ServerList.getSize() > 1){
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                super.shutdown();
            }
        };

        ServerManagerLogger.info("Start done! (It took "+(System.currentTimeMillis()-millis)+"ms)");

        ServerManagerLogger.info("");
        ServerManagerLogger.info("Type help to see commands");

        Scanner scanner = new Scanner(System.in);
        while (true){
            String line = scanner.nextLine();
            String[] args = line.split(" ");
            if(line.startsWith("help")){
                ServerManagerLogger.info("Available commands:");
                ServerManagerLogger.info("stop (Stop server)");
                ServerManagerLogger.info("createServer <Template Name>");
                ServerManagerLogger.info("generateSecretKey");
                ServerManagerLogger.info("status");
            }else if(line.startsWith("stop")){
                System.exit(0);
            }else if(line.startsWith("createServer")){
                if(args.length == 1){
                    ServerManagerLogger.error("createServer <Template Name>");
                }else{
                    new PacketCreateServer(args[1]).process(this.serverManager);
                    ServerManagerLogger.info("Packet sent");
                }
            }else if(line.startsWith("generateSecretKey")){
                ServerManagerLogger.info("Generation...");
                ServerManagerLogger.info("Key generated: "+this.serverManager.generateNewSecretKey());
            }else if(line.startsWith("status")){
                ServerManagerLogger.info("Server list:");
                for (Server server : ServerList.getAll()) {
                    ServerManagerLogger.info("\n -> "+server.getName()+" ["+server.getCoreServerType()+"]");
                    ServerManagerLogger.info("    Port: "+server.getPort());
                    ServerManagerLogger.info("    ServerManagerPort: "+server.getServerManagerPort());
                    ServerManagerLogger.info("    Capacity: "+server.getData().getCapacity());
                    ServerManagerLogger.info("    PlayerSize: "+server.getData().getPlayerSize());
                    ServerManagerLogger.info("    Players: "+server.getData().getUUIDByNameMap().toString());
                }
            }
        }
    }

    public ServerManager getServerManager() {
        return serverManager;
    }
}
