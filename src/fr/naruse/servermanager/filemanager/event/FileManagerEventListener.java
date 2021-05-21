package fr.naruse.servermanager.filemanager.event;

import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.api.events.EventListener;
import fr.naruse.servermanager.core.api.events.server.ServerDeleteEvent;
import fr.naruse.servermanager.core.api.events.server.ServerRegisterEvent;
import fr.naruse.servermanager.filemanager.FileManager;

public class FileManagerEventListener extends EventListener {

    private final FileManager fileManager;

    public FileManagerEventListener(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    @Override
    public void onServerRegisterEvent(ServerRegisterEvent e) {
        if(this.fileManager.getAutoScaler() != null){
            this.fileManager.getAutoScaler().scale();
        }
    }

    @Override
    public void onServerDeleteEvent(ServerDeleteEvent e) {
        if(e.getServer().getCoreServerType().is(CoreServerType.BUNGEE_MANAGER, CoreServerType.BUKKIT_MANAGER)){
            this.fileManager.shutdownServer(e.getServer().getName());
        }
    }
}
