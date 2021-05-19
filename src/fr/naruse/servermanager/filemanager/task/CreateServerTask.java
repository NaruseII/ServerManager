package fr.naruse.servermanager.filemanager.task;

import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.Utils;
import fr.naruse.servermanager.core.config.Configuration;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;
import fr.naruse.servermanager.filemanager.FileManager;
import fr.naruse.servermanager.filemanager.ServerProcess;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

public class CreateServerTask {

    private static final ServerManagerLogger.Logger LOGGER = new ServerManagerLogger.Logger("CreateServerTask");

    public CreateServerTask(FileManager fileManager, String templateName) {
        LOGGER.info("Launching new task...");
        Configuration template = fileManager.getServerManager().getConfigurationManager().getTemplate(templateName);
        if(template == null){
            LOGGER.error("Template '"+templateName+".json' not found!");
            return;
        }
        String name = template.get("baseName")+ Utils.randomLetters()+"-"+Utils.randomLetters();
        LOGGER.info("Starting creation of '"+name+"'...");

        String templateFolderUrl = template.get("pathTemplate");
        LOGGER.info("Template folder URL is '"+templateFolderUrl+"'");
        File templateFolder = new File(templateFolderUrl);
        if(!templateFolder.exists()){
            LOGGER.error("Template folder '"+templateFolder.getAbsolutePath()+"' not found!");
            return;
        }

        String targetFolderUrl = template.get("pathTarget");
        LOGGER.info("Target folder URL is '"+targetFolderUrl+"'");
        File targetFolder = new File(targetFolderUrl);
        if(!templateFolder.exists()){
            templateFolder.mkdirs();
        }

        File serverFolder = new File(targetFolder, name);
        LOGGER.info("Server folder URL is '"+serverFolder.getAbsolutePath()+"'");
        serverFolder.mkdirs();

        if(templateFolder.listFiles() == null){
            LOGGER.error("Template folder '"+templateFolder.getAbsolutePath()+"' is empty!");
            return;
        }

        LOGGER.info("Starting copy...");
        this.copyDirectory(templateFolder, serverFolder);
        LOGGER.info("Copy done !");

        LOGGER.info("Editing 'ServerManager/config.json'...");
        this.editConfigJson(serverFolder, name);
        LOGGER.info("'ServerManager/config.json' edited");

        LOGGER.info("Getting ready to start the server...");

        String startFileName = template.get("startFileName");
        LOGGER.info("Server start file name is '"+startFileName+"'");

        boolean isBatchFile = template.get("isBatchFile");
        boolean isShellFile = template.get("isShellFile");
        boolean isJarFile = template.get("isJarFile");

        try {
            File serverProperties = new File(serverFolder, "server.properties");
            if(serverProperties.exists()){
                this.editServerProperties(template, serverProperties, name);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        LOGGER.info("Starting server...");
        ProcessBuilder processBuilder;
        if(isBatchFile){
            processBuilder = new ProcessBuilder(new File(serverFolder, startFileName).getAbsolutePath());
        }else if(isJarFile){
            processBuilder = new ProcessBuilder("java", "-jar", startFileName);
            processBuilder.directory(serverFolder);
        }else{
            processBuilder = new ProcessBuilder(startFileName);
        }
        fileManager.followProcess(new ServerProcess(fileManager, processBuilder, name, templateName, serverFolder));
        LOGGER.info("Server started!");
    }

    private void editConfigJson(File serverFolder, String serverName) {
        File configJson = new File(serverFolder, "plugins/ServerManager/config.json");
        configJson.getParentFile().mkdirs();
        if(configJson.exists()){
            configJson.delete();
        }
        try {
            configJson.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<String, Object> map = new HashMap<>();
        map.put("key", ServerManager.get().getConfigurationManager().getConfig().get("key"));
        map.put("currentServerName", serverName);

        try {
            FileWriter fileWriter = new FileWriter(configJson);
            fileWriter.write(Utils.GSON.toJson(map));
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void editServerProperties(Configuration template, File propertiesFile, String serverName) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();

        Configuration.ConfigurationSection section = template.getSection("server.properties");
        boolean editServerIP = section.get("editServerIP");
        boolean editServerPort = section.get("editServerPort");
        boolean editServerName = section.get("editServerName");
        boolean editMaxPlayers = section.get("editMaxPlayers");

        propertiesFile.createNewFile();

        if(editServerIP){
            stringBuilder.append("server-ip="+ InetAddress.getLocalHost().getHostAddress()+"\n");
        }
        if(editServerPort){
            ServerSocket socket = new ServerSocket(0);
            stringBuilder.append("server-port="+ socket.getLocalPort()+"\n");
            socket.close();
        }
        if(editServerName){
            stringBuilder.append("server-name="+ serverName+"\n");
        }
        if(editMaxPlayers){
            stringBuilder.append("max-players="+Utils.getIntegerFromPacket(section.get("maxPlayers"))+"\n");
        }

        FileWriter fileWriter = new FileWriter(propertiesFile);
        fileWriter.write(stringBuilder.toString());
        fileWriter.close();
    }

    private void copyDirectory(File source, File dest) {
        for (File file : source.listFiles()) {
            if(file.isDirectory()){
                this.copyDirectory(file, new File(dest, file.getName()));
            }else{
                this.copyFile(file, new File(dest, file.getName()));
            }
        }
    }

    private void copyFile(File sourceFile, File destFile) {
        try{
            if (!sourceFile.exists()) {
                return;
            }
            if(!destFile.getParentFile().exists()){
                destFile.getParentFile().mkdirs();
            }
            if (!destFile.exists()) {
                destFile.createNewFile();
            }
            FileChannel source = null;
            FileChannel destination = null;
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            if (destination != null && source != null) {
                destination.transferFrom(source, 0, source.size());
            }
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
