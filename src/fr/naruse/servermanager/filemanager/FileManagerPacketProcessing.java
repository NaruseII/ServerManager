package fr.naruse.servermanager.filemanager;

import fr.naruse.servermanager.core.connection.packet.PacketBungeeRequestConfigWrite;
import fr.naruse.servermanager.core.connection.packet.PacketProcessing;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import fr.naruse.servermanager.filemanager.task.EditBungeeConfigFile;

public class FileManagerPacketProcessing extends PacketProcessing {

    private final FileManager fileManager;

    public FileManagerPacketProcessing(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    @Override
    public void processBungeeRequestConfigWrite(PacketBungeeRequestConfigWrite packet) {
        if(ServerList.getByName(packet.getBungeeName()) == null){
            ServerManagerLogger.error("Bungee '"+packet.getBungeeName()+"' not found! All servers must be launched by me and only me!");
            return;
        }

        Server targetServer = ServerList.getByName(packet.getServerTarget());


        EditBungeeConfigFile.EXECUTOR_SERVICE.submit(() -> {
            try {
                if(targetServer == null){
                    new EditBungeeConfigFile(packet.getServerTarget(), "null", 0, fileManager.getServerProcess(packet.getBungeeName()), packet.needToDelete());
                }else{
                    new EditBungeeConfigFile(targetServer.getName(), targetServer.getAddress().getHostAddress(), targetServer.getPort(), fileManager.getServerProcess(packet.getBungeeName()), packet.needToDelete());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
