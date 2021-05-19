package fr.naruse.servermanager.bungee.event;

import fr.naruse.servermanager.bungee.main.BungeeManagerPlugin;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.bukkit.event.player.PlayerLoginEvent;

public class BungeeListeners implements Listener {

    private final BungeeManagerPlugin pl;

    public BungeeListeners(BungeeManagerPlugin pl) {
        this.pl = pl;
    }

    @EventHandler
    public void join(PlayerLoginEvent e){
        this.pl.getServerManager().getCurrentServer().getData().getUUIDByNameMap().put(e.getPlayer().getName(), e.getPlayer().getUniqueId().toString());
    }

    @EventHandler
    public void leave(PlayerDisconnectEvent e){
        this.pl.getServerManager().getCurrentServer().getData().getUUIDByNameMap().remove(e.getPlayer().getName());
    }
}
