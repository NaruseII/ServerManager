package fr.naruse.servermanager.filemanager.event;

import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.api.events.EventListener;
import fr.naruse.servermanager.core.api.events.server.ServerDeleteEvent;
import fr.naruse.servermanager.core.api.events.server.ServerRegisterEvent;
import fr.naruse.servermanager.core.connection.packet.PacketAddStatus;
import fr.naruse.servermanager.core.connection.packet.PacketSendTemplate;
import fr.naruse.servermanager.filemanager.FileManager;
import fr.naruse.servermanager.filemanager.ServerProcess;

import java.util.List;

public class FileManagerEventListener extends EventListener {

    private final FileManager fileManager;

    public FileManagerEventListener(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    @Override
    public void onServerDeleteEvent(ServerDeleteEvent e) {
        if(e.getServer().getCoreServerType().is(CoreServerType.BUNGEE_MANAGER, CoreServerType.BUKKIT_MANAGER, CoreServerType.SPONGE_MANAGER, CoreServerType.NUKKIT_MANAGER)){
            this.fileManager.shutdownServer(e.getServer().getName());
        }
    }

    @Override
    public void onServerRegisterEvent(ServerRegisterEvent e) {
        if(e.getServer().getCoreServerType().is(CoreServerType.BUNGEE_MANAGER, CoreServerType.VELOCITY_MANAGER)){
            ServerProcess serverProcess = this.fileManager.getServerProcess(e.getServer().getName());
            if(serverProcess == null){
                return;
            }
            e.getServer().sendPacket(new PacketSendTemplate(serverProcess.getTemplate().toJson()));
        }
        if(!e.getServer().getCoreServerType().is(CoreServerType.FILE_MANAGER)){
            ServerProcess serverProcess = this.fileManager.getServerProcess(e.getServer().getName());
            if(serverProcess != null){
                List<String> defaultStatusList = serverProcess.getTemplate().get("defaultStatus");
                if(!defaultStatusList.isEmpty()){
                    e.getServer().sendPacket(new PacketAddStatus(defaultStatusList));
                }
            }
        }
    }
}
