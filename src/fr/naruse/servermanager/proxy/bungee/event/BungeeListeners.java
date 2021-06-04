package fr.naruse.servermanager.proxy.bungee.event;

import fr.naruse.servermanager.proxy.bungee.api.ServerManagerBungeeEvent;
import fr.naruse.servermanager.proxy.bungee.main.BungeeManagerPlugin;
import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.api.events.IEvent;
import fr.naruse.servermanager.core.api.events.server.ServerDeleteEvent;
import fr.naruse.servermanager.core.api.events.server.ServerRegisterEvent;
import fr.naruse.servermanager.core.connection.packet.PacketProxyRequestConfigWrite;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import fr.naruse.servermanager.proxy.common.ProxyListeners;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BungeeListeners implements Listener {

    private final BungeeManagerPlugin pl;

    public BungeeListeners(BungeeManagerPlugin pl) {
        this.pl = pl;
    }

    @EventHandler
    public void onJoin(LoginEvent e){
        Server server = this.pl.getServerManager().getCurrentServer();
        server.getData().getUUIDByNameMap().put(e.getConnection().getName(), e.getConnection().getUniqueId());
        if(BungeeCord.getInstance().getPlayers().size() == 0){
            server.getData().removeStatus(Server.Status.READY);
            server.getData().addStatus(Server.Status.ALLOCATED);
        }
    }

    @EventHandler
    public void onQuit(PlayerDisconnectEvent e){
        Server server = this.pl.getServerManager().getCurrentServer();
        server.getData().getUUIDByNameMap().remove(e.getPlayer().getName());
        if(BungeeCord.getInstance().getPlayers().size() == 1){
            server.getData().removeStatus(Server.Status.ALLOCATED);
            server.getData().addStatus(Server.Status.READY);
        }
    }

    @EventHandler
    public void onServerManagerEvent(ServerManagerBungeeEvent event){
        ProxyListeners.onServerManagerEvent(event.getEvent());
    }
}
