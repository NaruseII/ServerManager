package fr.naruse.servermanager.proxy.common;

import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.api.events.IEvent;
import fr.naruse.servermanager.core.api.events.server.ServerDeleteEvent;
import fr.naruse.servermanager.core.api.events.server.ServerRegisterEvent;
import fr.naruse.servermanager.core.connection.packet.PacketProxyRequestConfigWrite;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;

public class ProxyListeners {

    public static void onServerManagerEvent(IEvent iEvent){
        if(iEvent instanceof ServerRegisterEvent){
            ServerRegisterEvent e = (ServerRegisterEvent) iEvent;

            Server server = e.getServer();
            if(server.getCoreServerType().is(CoreServerType.BUKKIT_MANAGER, CoreServerType.SPONGE_MANAGER)){
                ServerList.findServer(CoreServerType.FILE_MANAGER).forEach(fileManager -> {
                    fileManager.sendPacket(new PacketProxyRequestConfigWrite(ServerManager.get().getCoreData().getServerName(), server.getName(), false));
                });
            }
        }
        else if(iEvent instanceof ServerDeleteEvent){
            ServerDeleteEvent e = (ServerDeleteEvent) iEvent;

            Server server = e.getServer();
            if(server.getCoreServerType().is(CoreServerType.BUKKIT_MANAGER, CoreServerType.SPONGE_MANAGER)){
                ServerList.findServer(CoreServerType.FILE_MANAGER).forEach(fileManager -> {
                    fileManager.sendPacket(new PacketProxyRequestConfigWrite(ServerManager.get().getCoreData().getServerName(), server.getName(), true));
                });
            }
        }
    }
}
