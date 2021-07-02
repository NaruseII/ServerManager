package fr.naruse.servermanager.bukkit.main;

import fr.naruse.servermanager.bukkit.api.ServerManagerBukkitEvent;
import fr.naruse.servermanager.bukkit.cmd.BukkitServerManagerCommand;
import fr.naruse.servermanager.bukkit.event.BukkitListeners;
import fr.naruse.servermanager.bukkit.packet.BukkitProcessPacketListener;
import fr.naruse.servermanager.core.*;
import fr.naruse.servermanager.core.api.events.IEvent;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;
import fr.naruse.servermanager.core.utils.Updater;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitManagerPlugin extends JavaPlugin implements IServerManagerPlugin {

    private ServerManager serverManager;

    @Override
    public void onEnable() {
        long millis  = System.currentTimeMillis();
        ServerManagerLogger.load(this.getLogger());
        ServerManagerLogger.info("Starting BukkitManager...");

        if(Updater.needToUpdate(CoreServerType.BUKKIT_MANAGER)){
            Bukkit.shutdown();
            return;
        }

        this.serverManager = new ServerManager(new CoreData(CoreServerType.BUKKIT_MANAGER, this.getDataFolder(), 4848, Bukkit.getServerName(), Bukkit.getPort()), this);
        this.serverManager.getCurrentServer().getData().setCapacity(Bukkit.getMaxPlayers());
        this.serverManager.registerPacketProcessing(new BukkitProcessPacketListener(this));

        for (Player player : Bukkit.getOnlinePlayers()) {
            this.serverManager.getCurrentServer().getData().getUUIDByNameMap().put(player.getName(), player.getUniqueId());
        }

        this.getServer().getPluginManager().registerEvents(new BukkitListeners(this), this);
        this.getCommand("servermanager").setExecutor(new BukkitServerManagerCommand(this));

        ServerManagerLogger.info("Start done! (It took "+(System.currentTimeMillis()-millis)+"ms)");
    }

    @Override
    public void onDisable() {
        if(this.serverManager != null){
            this.serverManager.shutdown();
        }
    }

    @Override
    public void shutdown() {
        Bukkit.shutdown();
    }

    @Override
    public void callEvent(IEvent event) {
        Bukkit.getPluginManager().callEvent(new ServerManagerBukkitEvent(event));
    }

    public ServerManager getServerManager() {
        return serverManager;
    }
}
