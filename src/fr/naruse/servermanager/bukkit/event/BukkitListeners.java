package fr.naruse.servermanager.bukkit.event;

import fr.naruse.servermanager.bukkit.main.BukkitManagerPlugin;
import fr.naruse.servermanager.core.server.Server;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class BukkitListeners implements Listener {

    private final BukkitManagerPlugin pl;

    public BukkitListeners(BukkitManagerPlugin pl) {
        this.pl = pl;
    }

    @EventHandler
    public void join(PlayerJoinEvent e){
        Server server = this.pl.getServerManager().getCurrentServer();
        server.getData().getUUIDByNameMap().put(e.getPlayer().getName(), e.getPlayer().getUniqueId());
        if(Bukkit.getServer().getOnlinePlayers().size() == 1){
            server.getData().removeStatus(Server.Status.READY);
            server.getData().addStatus(Server.Status.ALLOCATED);
        }
    }

    @EventHandler
    public void quit(PlayerQuitEvent e){
        Server server = this.pl.getServerManager().getCurrentServer();
        server.getData().getUUIDByNameMap().remove(e.getPlayer().getName());
        if(Bukkit.getServer().getOnlinePlayers().size() == 1){
            server.getData().removeStatus(Server.Status.ALLOCATED);
            server.getData().addStatus(Server.Status.READY);
        }
    }
}
