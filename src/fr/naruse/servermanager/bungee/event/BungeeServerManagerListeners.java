package fr.naruse.servermanager.bungee.event;

import fr.naruse.servermanager.bungee.api.ServerManagerBungeeEvent;
import fr.naruse.servermanager.bungee.main.BungeeManagerPlugin;
import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.api.events.IEvent;
import fr.naruse.servermanager.core.api.events.server.ServerDeleteEvent;
import fr.naruse.servermanager.core.api.events.server.ServerRegisterEvent;
import fr.naruse.servermanager.core.connection.packet.PacketBungeeRequestConfigWrite;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.function.Consumer;

public class BungeeServerManagerListeners implements Listener {

    private final BungeeManagerPlugin pl;

    public BungeeServerManagerListeners(BungeeManagerPlugin pl) {
        this.pl = pl;
    }

    @EventHandler
    public void serverManagerEvent(ServerManagerBungeeEvent event){
        IEvent iEvent = event.getEvent();
        if(iEvent instanceof ServerRegisterEvent){
            ServerRegisterEvent e = (ServerRegisterEvent) iEvent;

            Server server = e.getServer();
            if(server.getCoreServerType().is(CoreServerType.BUKKIT_MANAGER)){
                ServerList.findServer(CoreServerType.FILE_MANAGER).forEach(fileManager -> {
                    fileManager.sendPacket(new PacketBungeeRequestConfigWrite(pl.getServerManager().getCoreData().getServerName(), server.getName(), false));
                });
            }
        }
        else if(iEvent instanceof ServerDeleteEvent){
            ServerDeleteEvent e = (ServerDeleteEvent) iEvent;

            Server server = e.getServer();
            if(server.getCoreServerType().is(CoreServerType.BUKKIT_MANAGER)){
                ServerList.findServer(CoreServerType.FILE_MANAGER).forEach(fileManager -> {
                    fileManager.sendPacket(new PacketBungeeRequestConfigWrite(pl.getServerManager().getCoreData().getServerName(), server.getName(), true));
                });
            }
        }
    }
}
