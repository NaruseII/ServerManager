package fr.naruse.servermanager.proxy.velocity.event;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import fr.naruse.servermanager.core.api.events.server.ServerPostDeleteEvent;
import fr.naruse.servermanager.core.api.events.server.ServerPostRegisterEvent;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.proxy.velocity.api.ServerManagerVelocityEvent;
import fr.naruse.servermanager.proxy.velocity.main.VelocityManagerPlugin;
import fr.naruse.servermanager.proxy.velocity.server.VelocityServerHandler;

public class VelocityListeners {

    private final VelocityManagerPlugin pl;

    public VelocityListeners(VelocityManagerPlugin pl) {
        this.pl = pl;
    }

    @Subscribe
    public void onJoin(LoginEvent e){
        Player p = e.getPlayer();

        Server server = this.pl.getServerManager().getCurrentServer();
        server.getData().getUUIDByNameMap().put(p.getUsername(), p.getUniqueId());
        if(pl.getProxyServer().getPlayerCount() == 0){
            server.getData().removeStatus(Server.Status.READY);
            server.getData().addStatus(Server.Status.ALLOCATED);
        }

        VelocityServerHandler.reloadServers(this.pl, false);
    }

    @Subscribe
    public void onQuit(DisconnectEvent e){
        Player p = e.getPlayer();

        Server server = this.pl.getServerManager().getCurrentServer();
        server.getData().getUUIDByNameMap().remove(p.getUsername());
        if(pl.getProxyServer().getPlayerCount() == 1){
            server.getData().removeStatus(Server.Status.ALLOCATED);
            server.getData().addStatus(Server.Status.READY);
        }

        VelocityServerHandler.reloadServers(this.pl, false);
    }

    @Subscribe
    public void onServerRegisterEvent(ServerManagerVelocityEvent e) {
        if(e.getEvent() instanceof ServerPostRegisterEvent){
            VelocityServerHandler.reloadServers(this.pl);
        }else if(e.getEvent() instanceof ServerPostDeleteEvent){
            VelocityServerHandler.reloadServers(this.pl);
        }
    }
}
