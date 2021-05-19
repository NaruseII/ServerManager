package fr.naruse.servermanager.bukkit.event;

import fr.naruse.servermanager.bukkit.main.BukkitManagerPlugin;
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
        pl.getServerManager().getCurrentServer().getData().setPlayerSize(Bukkit.getOnlinePlayers().size());
        pl.getServerManager().getCurrentServer().getData().getUUIDByNameMap().put(e.getPlayer().getName(), e.getPlayer().getUniqueId().toString());
    }

    @EventHandler
    public void quit(PlayerQuitEvent e){
        pl.getServerManager().getCurrentServer().getData().setPlayerSize(Bukkit.getOnlinePlayers().size()-1);
        pl.getServerManager().getCurrentServer().getData().getUUIDByNameMap().remove(e.getPlayer().getName());
    }
}
