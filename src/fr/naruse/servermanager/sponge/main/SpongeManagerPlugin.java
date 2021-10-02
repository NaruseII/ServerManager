package fr.naruse.servermanager.sponge.main;

import fr.naruse.servermanager.core.CoreData;
import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.IServerManagerPlugin;
import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.api.events.IEvent;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;
import fr.naruse.servermanager.core.utils.Updater;
import fr.naruse.servermanager.sponge.api.ServerManagerSpongeEvent;
import fr.naruse.servermanager.sponge.cmd.SpongeServerManagerCommand;
import fr.naruse.servermanager.sponge.listener.SpongeListeners;
import fr.naruse.servermanager.core.logging.SLF4JCustomLogger;
import fr.naruse.servermanager.sponge.packet.SpongeProcessPacketListener;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.Plugin;

import javax.inject.Inject;
import java.io.File;

@Plugin(id = "servermanager", name = "ServerManager", version = "1.0.0", description = "ServerManager Sponge Manager")
public class SpongeManagerPlugin implements IServerManagerPlugin {

    @Inject
    private Logger logger;
    @Inject
    @ConfigDir(sharedRoot = false)
    private File dataFolder;

    private ServerManager serverManager;

    @Listener
    public void onServerStart(GameInitializationEvent e){
        long millis  = System.currentTimeMillis();

        ServerManagerLogger.setCustomLogger(new SLF4JCustomLogger(this.logger));
        ServerManagerLogger.info("Starting SpongeManager...");

        if(Updater.needToUpdate(CoreServerType.SPONGE_MANAGER)){
            Sponge.getServer().shutdown();
            return;
        }

        this.serverManager = new ServerManager(new CoreData(CoreServerType.SPONGE_MANAGER, this.getDataFolder(), null, Sponge.getServer().getBoundAddress().get().getPort()), this);
        this.serverManager.getCurrentServer().getData().setCapacity(Sponge.getServer().getMaxPlayers());
        this.serverManager.registerPacketProcessing(new SpongeProcessPacketListener(this));

        for (Player player : Sponge.getServer().getOnlinePlayers()) {
            this.serverManager.getCurrentServer().getData().getUUIDByNameMap().put(player.getName(), player.getUniqueId());
        }

        Sponge.getEventManager().registerListeners(this, new SpongeListeners());
        Sponge.getCommandManager().register(this, CommandSpec.builder()
                .permission("servermanager")
                .executor(new SpongeServerManagerCommand())
                .child(new SpongeServerManagerCommand.CreateServer(), "createServer")
                .child(new SpongeServerManagerCommand.ShutdownServer(), "shutdown")
                .child(new SpongeServerManagerCommand.InsertCommand(), "insertCommand")
                .child(new SpongeServerManagerCommand.Status(), "status")
                .build(), "servermanager", "sm");

        ServerManagerLogger.info("Start done! (It took "+(System.currentTimeMillis()-millis)+"ms)");
    }

    @Listener
    public void onServerStop(GameStoppingEvent e){
        if(this.serverManager != null){
            this.serverManager.shutdown();
        }
    }

    @Override
    public void shutdown() {
        Sponge.getServer().shutdown();
    }

    @Override
    public void callEvent(IEvent event) {
        Sponge.getEventManager().post(new ServerManagerSpongeEvent(event));
    }

    public File getDataFolder() {
        return dataFolder;
    }

    public ServerManager getServerManager() {
        return serverManager;
    }
}
