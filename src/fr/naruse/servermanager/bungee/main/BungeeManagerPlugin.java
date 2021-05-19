package fr.naruse.servermanager.bungee.main;

import fr.naruse.servermanager.bungee.api.ServerManagerBungeeEvent;
import fr.naruse.servermanager.bungee.event.BungeeListeners;
import fr.naruse.servermanager.core.CoreData;
import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.IServerManagerPlugin;
import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.api.events.IEvent;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeManagerPlugin extends Plugin implements IServerManagerPlugin {

    private ServerManager serverManager;

    @Override
    public void onEnable() {
        long millis  = System.currentTimeMillis();
        ServerManagerLogger.load(this.getLogger());
        ServerManagerLogger.info("Starting BungeeManager...");

        this.serverManager = new ServerManager(new CoreData(CoreServerType.BUNGEE_MANAGER, this.getDataFolder(), 4848, "bungee-manager", 0), this);

        this.getProxy().getPluginManager().registerListener(this, new BungeeListeners(this));

        ServerManagerLogger.info("Start done! (It took "+(System.currentTimeMillis()-millis)+"ms)");
    }

    @Override
    public void shutdown() {
        BungeeCord.getInstance().stop();
    }

    @Override
    public void callEvent(IEvent event) {
        BungeeCord.getInstance().getPluginManager().callEvent(new ServerManagerBungeeEvent(event));
    }


    public ServerManager getServerManager() {
        return serverManager;
    }
}
