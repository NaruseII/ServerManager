package fr.naruse.servermanager.bungee.event;

import fr.naruse.servermanager.bungee.main.BungeeManagerPlugin;
import fr.naruse.servermanager.core.server.Server;
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
    public void join(LoginEvent e){
        Server server = this.pl.getServerManager().getCurrentServer();
        server.getData().getUUIDByNameMap().put(e.getConnection().getName(), e.getConnection().getUniqueId().toString());
        if(BungeeCord.getInstance().getPlayers().size() == 0){
            server.getData().removeStatus(Server.Status.READY);
            server.getData().addStatus(Server.Status.ALLOCATED);
        }
    }

    @EventHandler
    public void leave(PlayerDisconnectEvent e){
        Server server = this.pl.getServerManager().getCurrentServer();
        server.getData().getUUIDByNameMap().remove(e.getPlayer().getName());
        if(BungeeCord.getInstance().getPlayers().size() == 1){
            server.getData().removeStatus(Server.Status.ALLOCATED);
            server.getData().addStatus(Server.Status.READY);
        }
    }
}
