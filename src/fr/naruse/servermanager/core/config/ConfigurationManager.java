package fr.naruse.servermanager.core.config;

import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ConfigurationManager {

    public static final ServerManagerLogger.Logger LOGGER = new ServerManagerLogger.Logger("ConfigurationManager");

    private final ServerManager serverManager;
    private final File serverTemplateFolder;

    private Configuration config;
    private Map<String, Configuration> serverTemplateSet = new HashMap<>();

    public ConfigurationManager(ServerManager serverManager) {
        this.serverManager = serverManager;
        this.serverTemplateFolder = new File(serverManager.getCoreData().getDataFolder(), "serverTemplate");
        this.serverTemplateFolder.mkdirs();

        try {
            this.load();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void load() throws Exception {
        LOGGER.info("Loading configurations...");

        File configFile = new File(serverManager.getCoreData().getDataFolder(), "config.json");
        this.config = new Configuration(configFile);

        LOGGER.info("Configurations loaded");

        LOGGER.info("Loading server template configurations...");

        if(this.serverTemplateFolder.listFiles() != null){
            for (File file : this.serverTemplateFolder.listFiles()) {
                if(!file.getName().endsWith(".json")){
                    LOGGER.warn("File '"+file.getName()+"' is weird. I only want .json file in this folder!");
                }else{
                    this.serverTemplateSet.put(file.getName().replace(".json", ""), new Configuration(file, "serverTemplate.json"));
                }
            }
        }

        LOGGER.info(this.serverTemplateSet.size()+" server template configuration loaded");
    }

    public void shutdown() {
        LOGGER.info("Saving configurations...");
        this.config.save();
        for (Configuration configuration : this.serverTemplateSet.values()) {
            configuration.save();
        }
    }

    public Configuration getTemplate(String templateName){
        return this.serverTemplateSet.get(templateName);
    }

    public Configuration getConfig() {
        return config;
    }
}
