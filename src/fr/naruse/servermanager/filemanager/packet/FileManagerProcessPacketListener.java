package fr.naruse.servermanager.filemanager.packet;

import fr.naruse.servermanager.core.connection.packet.PacketProxyRequestConfigWrite;
import fr.naruse.servermanager.core.connection.packet.ProcessPacketListener;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import fr.naruse.servermanager.filemanager.FileManager;
import fr.naruse.servermanager.filemanager.task.EditProxyConfigFile;

public class FileManagerProcessPacketListener extends ProcessPacketListener {

    private final FileManager fileManager;

    public FileManagerProcessPacketListener(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    @Override
    public void processProxyRequestConfigWrite(PacketProxyRequestConfigWrite packet) {
        if(ServerList.getByName(packet.getBungeeName()) == null){
            ServerManagerLogger.error("Bungee '"+packet.getBungeeName()+"' not found! All servers must be launched by me and only me!");
            return;
        }

        Server targetServer = ServerList.getByName(packet.getServerTarget());

        EditProxyConfigFile.EXECUTOR_SERVICE.submit(() -> {
            try {
                if(targetServer == null){
                    new EditProxyConfigFile(packet.getServerTarget(), "null", 0, fileManager.getServerProcess(packet.getBungeeName()), packet.needToDelete());
                }else{
                    new EditProxyConfigFile(targetServer.getName(), targetServer.getAddress().getHostAddress(), targetServer.getPort(), fileManager.getServerProcess(packet.getBungeeName()), packet.needToDelete());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
