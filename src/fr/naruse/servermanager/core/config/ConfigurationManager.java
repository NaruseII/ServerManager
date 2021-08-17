package fr.naruse.servermanager.core.config;

import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.api.events.config.ConfigurationLoadedEvent;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConfigurationManager {

    public static final ServerManagerLogger.Logger LOGGER = new ServerManagerLogger.Logger("ConfigurationManager");

    private final ServerManager serverManager;
    private final File serverTemplateFolder;

    private Configuration config;
    private Map<String, Configuration> serverTemplateMap = new HashMap<>();

    public ConfigurationManager(ServerManager serverManager) {
        this.serverManager = serverManager;
        this.serverTemplateFolder = new File(serverManager.getCoreData().getDataFolder(), "serverTemplates");
        this.serverTemplateFolder.mkdirs();

        this.load();
    }

    private void load() {
        LOGGER.info("Loading configurations...");

        this.config = new Configuration(new File(serverManager.getCoreData().getDataFolder(), "config.json"));

        LOGGER.info("Configurations loaded");

        LOGGER.info("Loading server template configurations...");

        if(this.serverTemplateFolder.listFiles() != null){
            for (File file : this.serverTemplateFolder.listFiles()) {
                if(!file.getName().endsWith(".json")){
                    LOGGER.warn("File '"+file.getName()+"' is weird. I only want .json file in this folder!");
                }else{
                    this.serverTemplateMap.put(file.getName().replace(".json", ""), new Configuration(file, "serverTemplate.json"));
                }
            }
        }

        this.serverManager.getPlugin().callEvent(new ConfigurationLoadedEvent(this.serverTemplateFolder, this.config, this.serverTemplateMap));

        LOGGER.info(this.serverTemplateMap.size()+" server template configuration loaded");
    }

    public void shutdown() {
        LOGGER.info("Saving configurations...");
        this.config.save();
        for (Configuration configuration : this.serverTemplateMap.values()) {
            configuration.save();
        }
    }

    public void addTemplate(String templateName, Configuration template){
        this.serverTemplateMap.put(templateName, template);
    }

    public Configuration getTemplate(String templateName){
        return this.serverTemplateMap.get(templateName);
    }

    public Set<Configuration> getAllTemplates(){
        return new HashSet<>(this.serverTemplateMap.values());
    }

    public Configuration getConfig() {
        return config;
    }
}
