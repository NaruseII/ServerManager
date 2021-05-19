package fr.naruse.servermanager.bukkit.main;

import fr.naruse.servermanager.bukkit.event.BukkitListeners;
import fr.naruse.servermanager.core.CoreData;
import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitManagerPlugin extends JavaPlugin {

    private ServerManager serverManager;

    @Override
    public void onEnable() {
        long millis  = System.currentTimeMillis();
        ServerManagerLogger.load(this.getLogger());
        ServerManagerLogger.info("Starting BukkitManager...");

        this.serverManager = new ServerManager(new CoreData(CoreServerType.BUKKIT_MANAGER, this.getDataFolder(), 4848, Bukkit.getServerName(), 0));

        this.serverManager.getCurrentServer().getData().setPlayerSize(Bukkit.getOnlinePlayers().size());
        for (Player player : Bukkit.getOnlinePlayers()) {
            this.serverManager.getCurrentServer().getData().getUUIDByNameMap().put(player.getName(), player.getUniqueId().toString());
        }

        this.getServer().getPluginManager().registerEvents(new BukkitListeners(this), this);

        ServerManagerLogger.info("Start done! (It took "+(System.currentTimeMillis()-millis)+"ms)");
    }

    @Override
    public void onDisable() {
        if(this.serverManager != null){
            this.serverManager.shutdown();
        }
    }

    public ServerManager getServerManager() {
        return serverManager;
    }
}
