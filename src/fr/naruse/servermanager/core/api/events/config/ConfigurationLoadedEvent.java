package fr.naruse.servermanager.core.api.events.config;

import fr.naruse.servermanager.core.api.events.IEvent;
import fr.naruse.servermanager.core.config.Configuration;

import java.io.File;
import java.util.Map;

public class ConfigurationLoadedEvent implements IEvent {

    private final File serverTemplateFolder;
    private final Configuration config;
    private final Map<String, Configuration> serverTemplateSet;

    public ConfigurationLoadedEvent(File serverTemplateFolder, Configuration config, Map<String, Configuration> serverTemplateSet) {
        this.serverTemplateFolder = serverTemplateFolder;
        this.config = config;
        this.serverTemplateSet = serverTemplateSet;
    }

    public Configuration getConfig() {
        return config;
    }

    public File getServerTemplateFolder() {
        return serverTemplateFolder;
    }

    public Map<String, Configuration> getServerTemplateSet() {
        return serverTemplateSet;
    }
}
