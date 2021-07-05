package fr.naruse.servermanager.proxy.common;

import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.api.events.IEvent;
import fr.naruse.servermanager.core.api.events.server.ServerDeleteEvent;
import fr.naruse.servermanager.core.api.events.server.ServerRegisterEvent;
import fr.naruse.servermanager.core.connection.packet.*;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;

import java.util.Optional;

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

    public static void processTeleportToLocation(PacketTeleportToLocation packet) {
        Optional<Server> optional = ServerList.findPlayerServer(new CoreServerType[]{CoreServerType.BUKKIT_MANAGER, CoreServerType.SPONGE_MANAGER}, packet.getPlayerName());
        if(optional.isPresent()){
            optional.get().sendPacket(packet);
        }
    }

    public static void processTeleportToPlayer(ProcessPacketListener listener, PacketTeleportToPlayer packet) {
        Optional<Server> optional = ServerList.findPlayerBukkitOrSpongeServer(packet.getTargetName());
        if(optional.isPresent()){
            listener.processSwitchServer(new PacketSwitchServer(optional.get(), packet.getPlayerName()));
            optional.get().sendPacket(packet);
        }
    }
}
