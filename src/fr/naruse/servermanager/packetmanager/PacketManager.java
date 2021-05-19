package fr.naruse.servermanager.packetmanager;

import fr.naruse.servermanager.core.*;
import fr.naruse.servermanager.core.connection.packet.PacketCreateServer;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;

import java.io.File;
import java.util.Scanner;

public class PacketManager {

    public static void main(String[] args) {
        new PacketManager();
    }

    private final ServerManager serverManager;

    public PacketManager() {
        long millis  = System.currentTimeMillis();
        ServerManagerLogger.info("Starting PacketManager...");

        this.serverManager = new ServerManager(new CoreData(CoreServerType.PACKET_MANAGER, new File("configs"), 4848, "packet-manager", 4848));

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
                    ServerManagerLogger.info(" -> "+server.getName());
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
