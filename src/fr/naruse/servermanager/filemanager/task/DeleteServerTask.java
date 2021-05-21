package fr.naruse.servermanager.filemanager.task;

import fr.naruse.servermanager.core.config.Configuration;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;
import fr.naruse.servermanager.filemanager.FileManager;

import java.io.File;
import java.util.*;

public class DeleteServerTask {

    private final ServerManagerLogger.Logger LOGGER = new ServerManagerLogger.Logger("DeleteServerTask");

    public DeleteServerTask(Configuration template, String serverName) {
        LOGGER.setTag("DeleteServerTask - "+serverName);
        LOGGER.info("Launching new task...");
        LOGGER.info("Starting deletion of '"+serverName+"'...");

        String templateFolderUrl = template.get("pathTemplate");
        LOGGER.debug("Template folder URL is '"+templateFolderUrl+"'");
        File templateFolder = new File(templateFolderUrl);
        if(!templateFolder.exists()){
            LOGGER.error("Template folder '"+templateFolder.getAbsolutePath()+"' not found!");
            return;
        }

        String targetFolderUrl = template.get("pathTarget");
        LOGGER.debug("Target folder URL is '"+targetFolderUrl+"'");
        File targetFolder = new File(targetFolderUrl);
        if(!templateFolder.exists()){
            templateFolder.mkdirs();
        }

        File serverFolder = new File(targetFolder, serverName);
        LOGGER.debug("Server folder URL is '"+serverFolder.getAbsolutePath()+"'");
        serverFolder.mkdirs();

        LOGGER.info("Deleting...");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.delete(serverFolder);
        LOGGER.info("Server deleted");
    }

    private void delete(File file) {
        List<File> list = new ArrayList<>();

        if(file.isDirectory()){
            if(file.listFiles() == null){
                file.delete();
            }else{
                for (File listFile : file.listFiles()) {
                    this.delete(listFile);
                }
                list.add(file);
            }
        }else{
            file.delete();
        }

        Collections.reverse(list);
        for (File file1 : list) {
            file1.delete();
        }
    }
}
