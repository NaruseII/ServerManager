package fr.naruse.servermanager.nukkit.main;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.plugin.PluginBase;
import fr.naruse.servermanager.core.CoreData;
import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.IServerManagerPlugin;
import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.api.events.IEvent;
import fr.naruse.api.config.Configuration;
import fr.naruse.servermanager.nukkit.logging.NukkitCustomLogger;
import fr.naruse.api.logging.GlobalLogger;
import fr.naruse.servermanager.core.utils.Updater;
import fr.naruse.servermanager.nukkit.api.ServerManagerNukkitEvent;
import fr.naruse.servermanager.nukkit.cmd.NukkitServerManagerCommand;
import fr.naruse.servermanager.nukkit.listener.NukkitListeners;
import fr.naruse.servermanager.nukkit.packet.NukkitProcessPacketListener;

import java.io.File;

public class NukkitManagerPlugin extends PluginBase implements IServerManagerPlugin {

    private ServerManager serverManager;

    @Override
    public void onEnable() {
        long millis  = System.currentTimeMillis();
        GlobalLogger.setCustomLogger(new NukkitCustomLogger(this.getLogger()));
        GlobalLogger.info("Starting NukkitManager...");

        if(Updater.needToUpdate(CoreServerType.NUKKIT_MANAGER)){
            this.getServer().shutdown();
            return;
        }

        String serverName = new Configuration(new File(this.getDataFolder(), "config.json")).get("currentServerName");

        this.serverManager = new ServerManager(new CoreData(CoreServerType.NUKKIT_MANAGER, this.getDataFolder(), serverName, this.getServer().getPort()), this);
        this.serverManager.getCurrentServer().getData().setCapacity(this.getServer().getMaxPlayers());
        this.serverManager.registerPacketProcessing(new NukkitProcessPacketListener(this));

        for (Player player : this.getServer().getOnlinePlayers().values()) {
            this.serverManager.getCurrentServer().getData().getUUIDByNameMap().put(player.getName(), player.getUniqueId());
        }

        this.getServer().getPluginManager().registerEvents(new NukkitListeners(this), this);

        GlobalLogger.info("Start done! (It took "+(System.currentTimeMillis()-millis)+"ms)");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("servermanager")){
            NukkitServerManagerCommand.onCommand(this, sender, args);
        }
        return super.onCommand(sender, command, label, args);
    }

    @Override
    public void shutdown() {
        this.getServer().shutdown();
    }

    @Override
    public void callEvent(IEvent event) {
        this.getServer().getPluginManager().callEvent(new ServerManagerNukkitEvent(event));
    }

    public ServerManager getServerManager() {
        return serverManager;
    }
}
