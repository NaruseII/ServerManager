package fr.naruse.servermanager.nukkit.listener;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.nukkit.main.NukkitManagerPlugin;

public class NukkitListeners implements Listener {

    private final NukkitManagerPlugin pl;

    public NukkitListeners(NukkitManagerPlugin nukkitManagerPlugin) {
        this.pl = nukkitManagerPlugin;
    }

    @EventHandler
    public void join(PlayerJoinEvent e) {
        Server server = this.pl.getServerManager().getCurrentServer();
        server.getData().getUUIDByNameMap().put(e.getPlayer().getName(), e.getPlayer().getUniqueId());
        if(this.pl.getServer().getOnlinePlayers().size() == 1){
            server.getData().removeStatus(Server.Status.READY);
            server.getData().addStatus(Server.Status.ALLOCATED);
        }
    }

    @EventHandler
    public void quit(PlayerQuitEvent e){
        Server server = this.pl.getServerManager().getCurrentServer();
        server.getData().getUUIDByNameMap().remove(e.getPlayer().getName());
        if(this.pl.getServer().getOnlinePlayers().size() == 1){
            server.getData().removeStatus(Server.Status.ALLOCATED);
            server.getData().addStatus(Server.Status.READY);
        }
    }

}
