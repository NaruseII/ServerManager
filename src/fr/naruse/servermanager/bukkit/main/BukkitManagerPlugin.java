package fr.naruse.servermanager.bukkit.main;

import fr.naruse.servermanager.bukkit.api.ServerManagerBukkitEvent;
import fr.naruse.servermanager.bukkit.cmd.BukkitServerManagerCommand;
import fr.naruse.servermanager.bukkit.event.BukkitListeners;
import fr.naruse.servermanager.bukkit.packet.BukkitProcessPacketListener;
import fr.naruse.servermanager.core.*;
import fr.naruse.servermanager.core.api.events.IEvent;
import fr.naruse.servermanager.core.config.Configuration;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;
import fr.naruse.servermanager.core.utils.Updater;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BukkitManagerPlugin extends JavaPlugin implements IServerManagerPlugin {

    private ServerManager serverManager;

    private boolean isUpperTwelve = false;

    @Override
    public void onEnable() {
        long millis  = System.currentTimeMillis();
        ServerManagerLogger.load(this.getLogger());
        ServerManagerLogger.info("Starting BukkitManager...");

        if(Updater.needToUpdate(CoreServerType.BUKKIT_MANAGER)){
            Bukkit.shutdown();
            return;
        }

        String serverName = "unknown";
        try {
            serverName = (String) Bukkit.class.getDeclaredMethod("getServerName").invoke(null);
        } catch (Exception illegalAccessException) {
            try {
                serverName = new Configuration(new File(this.getDataFolder(), "config.json")).get("currentServerName");
                isUpperTwelve = true;
            } catch (Exception e) { }
        }

        this.serverManager = new ServerManager(new CoreData(CoreServerType.BUKKIT_MANAGER, this.getDataFolder(), 4848, serverName, Bukkit.getPort()), this);
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
        Runnable runnable = () -> Bukkit.getPluginManager().callEvent(new ServerManagerBukkitEvent(event));
        if(this.isUpperTwelve){
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
