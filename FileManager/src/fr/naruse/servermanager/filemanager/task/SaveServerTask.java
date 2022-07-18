package fr.naruse.servermanager.filemanager.task;

import fr.naruse.api.config.Configuration;
import fr.naruse.api.logging.GlobalLogger;
import fr.naruse.servermanager.filemanager.SaverUtils;
import fr.naruse.servermanager.filemanager.ServerProcess;

import java.io.File;
import java.util.UUID;

public class SaveServerTask {

    private final GlobalLogger.Logger LOGGER = new GlobalLogger.Logger("SaveServerTask");
    private ServerProcess serverProcess;

    public SaveServerTask(ServerProcess serverProcess, String saveKey) {
        this.serverProcess = serverProcess;

        LOGGER.setTag("SaveServerTask - "+serverProcess.getName());
        LOGGER.info("Launching new task...");
        LOGGER.debug("Starting save of '"+serverProcess.getName()+"'...");

        String fileName = UUID.randomUUID() + UUID.randomUUID().toString();

        this.save(fileName);

        Configuration.ConfigurationSection section = SaverUtils.CONFIGURATION.newSection(saveKey);
        section.set("fileName", fileName);
        section.set("templateName", serverProcess.getTemplateName());
        SaverUtils.CONFIGURATION.save();

        LOGGER.info("Task complete");
    }

    private void save(String fileName){
        if(this.serverProcess.getServerFolder().listFiles() == null){
            return;
        }

        LOGGER.info("Saving...");

        SaverUtils.ZipHelper.zipFolder(this.serverProcess.getServerFolder(), new File(SaverUtils.SAVE_FOLDER, fileName));

        LOGGER.info("Server saved");
    }

}
