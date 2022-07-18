package fr.naruse.servermanager.bukkit.main;

import fr.naruse.servermanager.bukkit.api.ServerManagerBukkitEvent;
import fr.naruse.servermanager.bukkit.cmd.BukkitServerManagerCommand;
import fr.naruse.servermanager.bukkit.listener.BukkitListeners;
import fr.naruse.servermanager.bukkit.packet.BukkitProcessPacketListener;
import fr.naruse.servermanager.core.*;
import fr.naruse.servermanager.core.api.events.IEvent;
import fr.naruse.api.config.Configuration;
import fr.naruse.api.logging.GlobalLogger;
import fr.naruse.servermanager.core.utils.Updater;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class BukkitManagerPlugin extends JavaPlugin implements IServerManagerPlugin {

    private String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
    private double doubleVersion;

    private ServerManager serverManager;
    private BasicServerManagerPlugin basicServerManagerPlugin;

    @Override
    public void onEnable() {
        long millis  = System.currentTimeMillis();
        GlobalLogger.setPluginLogger(this.getLogger());
        GlobalLogger.info("Starting BukkitManager...");

        if(Updater.needToUpdate(CoreServerType.BUKKIT_MANAGER)){
            Bukkit.shutdown();
            return;
        }

        String sVersion = version.replace("v", "").replace("R1", "").replace("R2", "").replace("R3", "").replace("R4", "").replace("_", ".");
        doubleVersion = Double.valueOf(sVersion.trim().substring(0, sVersion.length()-2));

        String serverName = "unknown";
        try {
            serverName = (String) Bukkit.class.getDeclaredMethod("getServerName").invoke(null);
        } catch (Exception illegalAccessException) {
            try {
                serverName = new Configuration(new File(this.getDataFolder(), "config.json")).get("currentServerName");
            } catch (Exception e) { }
        }

        this.serverManager = new ServerManager(new CoreData(CoreServerType.BUKKIT_MANAGER, this.getDataFolder(), serverName, Bukkit.getPort()), this);
        this.serverManager.getCurrentServer().getData().setCapacity(Bukkit.getMaxPlayers());
        this.serverManager.registerPacketProcessing(new BukkitProcessPacketListener(this));
        this.basicServerManagerPlugin = new BasicServerManagerPlugin(this.serverManager.getEventListenerSet());

        for (Player player : Bukkit.getOnlinePlayers()) {
            this.serverManager.getCurrentServer().getData().getUUIDByNameMap().put(player.getName(), player.getUniqueId());
        }

        this.getServer().getPluginManager().registerEvents(new BukkitListeners(this), this);
        this.getCommand("servermanager").setExecutor(new BukkitServerManagerCommand(this));

        GlobalLogger.info("Start done! (It took "+(System.currentTimeMillis()-millis)+"ms)");
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
        if(this.basicServerManagerPlugin != null){
            this.basicServerManagerPlugin.callEvent(event);
        }
        Runnable runnable = () -> Bukkit.getPluginManager().callEvent(new ServerManagerBukkitEvent(event));
        if(doubleVersion >= 1.13){
            if(this.serverManager != null && this.serverManager.isShuttingDowned()){
                return;
            }
            Bukkit.getScheduler().runTask(this, runnable);
        }else{
            runnable.run();
        }
    }

    public ServerManager getServerManager() {
        return serverManager;
    }
}
