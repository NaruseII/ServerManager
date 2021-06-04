package fr.naruse.servermanager.sponge.event;

import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.server.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public class SpongeListeners {

    @Listener
    public void join(ClientConnectionEvent.Join e){
        Player p = e.getTargetEntity();

        Server server = ServerManager.get().getCurrentServer();
        server.getData().getUUIDByNameMap().put(p.getName(), p.getUniqueId());
        if(Sponge.getServer().getOnlinePlayers().size() == 1){
            server.getData().removeStatus(Server.Status.READY);
            server.getData().addStatus(Server.Status.ALLOCATED);
        }
    }


    @Listener
    public void quit(ClientConnectionEvent.Disconnect e){
        Player p = e.getTargetEntity();

        Server server = ServerManager.get().getCurrentServer();
        server.getData().getUUIDByNameMap().remove(p.getName());
        if(Sponge.getServer().getOnlinePlayers().size() == 1){
            server.getData().removeStatus(Server.Status.ALLOCATED);
            server.getData().addStatus(Server.Status.READY);
        }
    }

}
