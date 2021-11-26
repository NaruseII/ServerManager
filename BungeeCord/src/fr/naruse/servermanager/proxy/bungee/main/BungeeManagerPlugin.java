package fr.naruse.servermanager.proxy.bungee.main;

import fr.naruse.api.config.Configuration;
import fr.naruse.servermanager.proxy.bungee.api.ServerManagerBungeeEvent;
import fr.naruse.servermanager.proxy.bungee.listener.BungeeListeners;
import fr.naruse.servermanager.proxy.bungee.packet.BungeeProcessPacketListener;
import fr.naruse.servermanager.core.*;
import fr.naruse.servermanager.core.api.events.IEvent;
import fr.naruse.api.logging.GlobalLogger;
import fr.naruse.servermanager.core.utils.Updater;
import fr.naruse.servermanager.proxy.bungee.server.BungeeServerHandler;
import fr.naruse.servermanager.proxy.common.ProxyUtils;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeManagerPlugin extends Plugin implements IServerManagerPlugin {

    private ServerManager serverManager;

    private ListenerInfo listenerInfo;
    private Configuration templateConfiguration;

    @Override
    public void onEnable() {
        long millis  = System.currentTimeMillis();
        GlobalLogger.setPluginLogger(this.getLogger());
        GlobalLogger.info("Starting BungeeManager...");

        if(Updater.needToUpdate(CoreServerType.BUNGEE_MANAGER)){
            BungeeCord.getInstance().stop();
            return;
        }

        this.setListenerInfo(BungeeCord.getInstance().getConfig().getListeners().stream().findFirst().get());
        this.serverManager = new ServerManager(new CoreData(CoreServerType.BUNGEE_MANAGER, this.getDataFolder(), null, this.listenerInfo.getQueryPort()), this);
        this.serverManager.getCurrentServer().getData().setCapacity(this.listenerInfo.getMaxPlayers());

        BungeeProcessPacketListener bungeeProcessPacketListener = new BungeeProcessPacketListener(this);
        this.serverManager.registerPacketProcessing(bungeeProcessPacketListener);

        ProxyUtils.load(this.getDataFolder());
        BungeeServerHandler.clear(this);
        BungeeServerHandler.reloadServers(this);

        this.getProxy().getPluginManager().registerListener(this, new BungeeListeners(this));

        GlobalLogger.info("Start done! (It took "+(System.currentTimeMillis()-millis)+"ms)");
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

    public Configuration getTemplateConfiguration() {
        return templateConfiguration;
    }

    public void setTemplateConfiguration(Configuration templateConfiguration) {
        this.templateConfiguration = templateConfiguration;
        BungeeServerHandler.reloadServers(this);
    }
}
