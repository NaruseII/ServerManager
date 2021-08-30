package fr.naruse.servermanager.proxy.bungee.event;

import fr.naruse.servermanager.core.api.events.server.ServerDeleteEvent;
import fr.naruse.servermanager.core.api.events.server.ServerPostDeleteEvent;
import fr.naruse.servermanager.core.api.events.server.ServerPostRegisterEvent;
import fr.naruse.servermanager.proxy.bungee.api.ServerManagerBungeeEvent;
import fr.naruse.servermanager.proxy.bungee.main.BungeeManagerPlugin;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.proxy.bungee.server.BungeeServerHandler;
import fr.naruse.servermanager.proxy.common.ProxyListeners;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
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

        BungeeServerHandler.reloadServers(this.pl, false);
    }

    @EventHandler
    public void switchEvent(ServerSwitchEvent e){
        BungeeServerHandler.reloadServers(this.pl, false);
    }

    @EventHandler
    public void onQuit(PlayerDisconnectEvent e){
        Server server = this.pl.getServerManager().getCurrentServer();
        server.getData().getUUIDByNameMap().remove(e.getPlayer().getName());
        if(BungeeCord.getInstance().getPlayers().size() == 1){
            server.getData().removeStatus(Server.Status.ALLOCATED);
            server.getData().addStatus(Server.Status.READY);
        }

        BungeeServerHandler.reloadServers(this.pl, false);
    }

    @EventHandler
    public void onServerRegisterEvent(ServerManagerBungeeEvent e) {
        if(e.getEvent() instanceof ServerPostRegisterEvent){
            BungeeServerHandler.reloadServers(this.pl);
        }else if(e.getEvent() instanceof ServerPostDeleteEvent){
            BungeeServerHandler.reloadServers(this.pl);
        }
    }
}
