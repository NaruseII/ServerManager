package fr.naruse.servermanager.proxy.velocity.main;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.naruse.servermanager.core.CoreData;
import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.IServerManagerPlugin;
import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.api.events.IEvent;
import fr.naruse.servermanager.core.logging.SLF4JCustomLogger;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;
import fr.naruse.servermanager.core.utils.Updater;
import fr.naruse.servermanager.proxy.common.ProxyUtils;
import fr.naruse.servermanager.proxy.velocity.api.ServerManagerVelocityEvent;
import fr.naruse.servermanager.proxy.velocity.event.VelocityListeners;
import fr.naruse.servermanager.proxy.velocity.packet.VelocityProcessPacketListener;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(id = "servermanager", name = "ServerManager", version = "1.0.13", url = "https://www.mc-market.org/resources/20469/", description = "ServerManager Velocity Manager", authors = "Naruse")
public class VelocityManagerPlugin implements IServerManagerPlugin {

    private final ProxyServer proxyServer;
    private final Logger logger;
    private final Path dataFolderPath;
    private ServerManager serverManager;

    @Inject
    public VelocityManagerPlugin(ProxyServer proxyServer, Logger logger, @DataDirectory  Path dataFolderPath) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.dataFolderPath = dataFolderPath;
    }

    @Subscribe
    public void onInit(ProxyInitializeEvent e){
        long millis  = System.currentTimeMillis();
        ServerManagerLogger.setCustomLogger(new SLF4JCustomLogger(this.logger));
        ServerManagerLogger.info("Starting VelocityManager...");

        if(Updater.needToUpdate(CoreServerType.VELOCITY_MANAGER)){
            proxyServer.shutdown();
            return;
        }

        this.serverManager = new ServerManager(new CoreData(CoreServerType.VELOCITY_MANAGER, this.dataFolderPath.toFile(), 4848, null, this.proxyServer.getBoundAddress().getPort()), this);
        this.serverManager.getCurrentServer().getData().setCapacity(this.proxyServer.getConfiguration().getShowMaxPlayers());
        this.serverManager.registerPacketProcessing(new VelocityProcessPacketListener(this));

        ProxyUtils.load(this.dataFolderPath.toFile());

        this.proxyServer.getEventManager().register(this, new VelocityListeners(this));

        ServerManagerLogger.info("Start done! (It took "+(System.currentTimeMillis()-millis)+"ms)");
    }

    @Subscribe
    public void onStop(ProxyShutdownEvent e){
        this.serverManager.shutdown();
    }

    @Override
    public void shutdown() {
        this.proxyServer.shutdown();
    }

    @Override
    public void callEvent(IEvent event) {
        this.proxyServer.getEventManager().fire(new ServerManagerVelocityEvent(event));
    }

    public ServerManager getServerManager() {
        return serverManager;
    }

    public ProxyServer getProxyServer() {
        return proxyServer;
    }
}