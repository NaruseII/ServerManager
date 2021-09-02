package fr.naruse.servermanager.core.plugin;

import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.api.events.IEvent;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;

import java.io.File;

public abstract class SMPlugin {

    private final ServerManagerLogger.Logger LOGGER = new ServerManagerLogger.Logger("");

    private final String name;
    private final File dataFolder;
    public SMPlugin(String name, File dataFolder) {
        this.dataFolder = dataFolder;
        this.name = name;
        LOGGER.setTag(name);
    }

    public abstract void init();

    public abstract void shutdown();

    public abstract void handlePluginEvent(IEvent e);

    public ServerManager getServerManager() {
        return ServerManager.get();
    }

    public File getDataFolder() {
        return dataFolder;
    }

    public String getPluginName() {
        return name;
    }

    public ServerManagerLogger.Logger getLogger() {
        return LOGGER;
    }
}
