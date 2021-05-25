package fr.naruse.servermanager.bungee.main;

import fr.naruse.servermanager.bungee.api.ServerManagerBungeeEvent;
import fr.naruse.servermanager.bungee.event.BungeeListeners;
import fr.naruse.servermanager.bungee.event.BungeeServerManagerListeners;
import fr.naruse.servermanager.bungee.packet.BungeePacketProcessing;
import fr.naruse.servermanager.core.*;
import fr.naruse.servermanager.core.api.events.IEvent;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeManagerPlugin extends Plugin implements IServerManagerPlugin {

    private ServerManager serverManager;

    private ListenerInfo listenerInfo;

    @Override
    public void onEnable() {
        long millis  = System.currentTimeMillis();
        ServerManagerLogger.load(this.getLogger());
        ServerManagerLogger.info("Starting BungeeManager...");

        Updater.needToUpdate();

        this.setListenerInfo(BungeeCord.getInstance().getConfig().getListeners().stream().findFirst().get());
        this.serverManager = new ServerManager(new CoreData(CoreServerType.BUNGEE_MANAGER, this.getDataFolder(), 4848, null, this.listenerInfo.getQueryPort()), this);
        this.serverManager.getCurrentServer().getData().setCapacity(this.listenerInfo.getMaxPlayers());
        this.serverManager.registerPacketProcessing(new BungeePacketProcessing(this));

        this.getProxy().getPluginManager().registerListener(this, new BungeeListeners(this));
        this.getProxy().getPluginManager().registerListener(this, new BungeeServerManagerListeners(this));

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

    public ListenerInfo getListenerInfo() {
        return listenerInfo;
    }

    public void setListenerInfo(ListenerInfo listenerInfo) {
        this.listenerInfo = listenerInfo;
    }
}
