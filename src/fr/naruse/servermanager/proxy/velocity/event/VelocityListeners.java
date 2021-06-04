package fr.naruse.servermanager.proxy.velocity.event;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.api.events.IEvent;
import fr.naruse.servermanager.core.api.events.server.ServerDeleteEvent;
import fr.naruse.servermanager.core.api.events.server.ServerRegisterEvent;
import fr.naruse.servermanager.core.connection.packet.PacketProxyRequestConfigWrite;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import fr.naruse.servermanager.proxy.common.ProxyListeners;
import fr.naruse.servermanager.proxy.velocity.api.ServerManagerVelocityEvent;
import fr.naruse.servermanager.proxy.velocity.main.VelocityManagerPlugin;

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
    }

    @Subscribe
    public void onServerManagerEvent(ServerManagerVelocityEvent event){
        ProxyListeners.onServerManagerEvent(event.getEvent());
    }
}
