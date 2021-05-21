package fr.naruse.servermanager.packetmanager;

import fr.naruse.servermanager.core.*;
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
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
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
            if(line.startsWith("stop")){
                System.exit(0);
            }else if(line.startsWith("generateSecretKey")){
                ServerManagerLogger.info("Generation...");
                ServerManagerLogger.info("Key generated: "+this.serverManager.generateNewSecretKey());
            }else if(line.startsWith("status")){
                serverManager.printStatus();
            }else{
                ServerManagerLogger.info("Available commands:");
                ServerManagerLogger.info("");
                ServerManagerLogger.info("-> stop (Stop server)");
                ServerManagerLogger.info("-> generateSecretKey");
                ServerManagerLogger.info("-> status");
            }
        }
    }

    public ServerManager getServerManager() {
        return serverManager;
    }
}
