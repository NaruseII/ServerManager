package fr.naruse.servermanager.filemanager.task;

import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.connection.packet.AbstractPacketResponsive;
import fr.naruse.servermanager.core.connection.packet.PacketFileManagerRequest;
import fr.naruse.servermanager.core.utils.Utils;
import fr.naruse.api.config.Configuration;
import fr.naruse.api.logging.GlobalLogger;
import fr.naruse.servermanager.filemanager.FileManager;
import fr.naruse.servermanager.filemanager.NameProperty;
import fr.naruse.servermanager.filemanager.SaverUtils;
import fr.naruse.servermanager.filemanager.ServerProcess;

import java.io.*;
import java.net.ServerSocket;
import java.util.*;

public class CreateServerTask {

    private final GlobalLogger.Logger LOGGER = new GlobalLogger.Logger("CreateServerTask");

    private File targetFolder;
    private File templateFolder;
    private File serverFolder;
    private String[] name;

    public CreateServerTask(FileManager fileManager, String templateName, Map<String, Object> initialServerData, boolean isSavedServer) {
        LOGGER.info("Launching new task...");
        Configuration template = fileManager.getServerManager().getConfigurationManager().getTemplate(templateName);
        if(template == null){

            Configuration.ConfigurationSection section = SaverUtils.CONFIGURATION.getSection(templateName);
            boolean flag = true;
            if(section != null){
                template = fileManager.getServerManager().getConfigurationManager().getTemplate(section.get("templateName"));
                flag = template == null;
            }

            if(flag){
                LOGGER.error("Template '"+templateName+".json' not found!");
                return;
            }
        }

        name = new String[]{template.get("baseName")};

        boolean random = template.get("randomName");
        NameProperty nameProperty = NameProperty.valueOf(template.get("nameProperty"));

        if(random){
            if((nameProperty == null || nameProperty == NameProperty.RANDOM_4_CHAR)){
                name[0] += "-" +Utils.randomLetters(4)+"-"+Utils.randomLetters(4);
            }else if(nameProperty == NameProperty.RANDOM_8_CHAR){
                name[0] += "-" +Utils.randomLetters(8)+"-"+Utils.randomLetters(8);
            }else if(nameProperty == NameProperty.FROM_0){

                Thread thread = Thread.currentThread();
                AbstractPacketResponsive abstractPacket = new PacketFileManagerRequest.NewName(fileManager.getServerManager().getCurrentServer().getName(), thread.getId(), name[0]);
                fileManager.getServerManager().getConnectionManager().sendResponsivePacket(thread, abstractPacket, object -> {
                    PacketFileManagerRequest.NewNameResponse packet = (PacketFileManagerRequest.NewNameResponse) object;
                    name[0] = packet.getNewName();
                });

            }
        }else if(fileManager.getServerProcess(name[0]) != null){
            name[0] += "-" +Utils.randomLetters(12)+"-"+Utils.randomLetters(12);
        }

        if(fileManager.getServerProcess(name[0]) != null){
            LOGGER.error("Could not create '"+templateName+"' all names are used! This isn't supposed to happen unless you create "+((12*26*26*2)*2+(4*26*26*2)*2)+" servers!");
            return;
        }

        LOGGER.setTag("CreateServerTask - "+ name[0]);
        LOGGER.debug("Starting creation of '"+ name[0] +"'...");

        String templateFolderUrl = template.get("pathTemplate");
        LOGGER.debug("Template folder URL is '"+templateFolderUrl+"'");
        templateFolder = new File(templateFolderUrl);
        if(!templateFolder.exists()){
            LOGGER.error("Template folder '"+templateFolder.getAbsolutePath()+"' not found!");
            return;
        }

        String targetFolderUrl = template.get("pathTarget");
        LOGGER.debug("Target folder URL is '"+targetFolderUrl+"'");
        targetFolder = new File(targetFolderUrl);
        if(!templateFolder.exists()){
            templateFolder.mkdirs();
        }

        serverFolder = new File(targetFolder, name[0]);
        LOGGER.debug("Server folder URL is '"+serverFolder.getAbsolutePath()+"'");
        serverFolder.mkdirs();

        if(templateFolder.listFiles() == null){
            LOGGER.error("Template folder '"+templateFolder.getAbsolutePath()+"' is empty!");
            return;
        }

        if(isSavedServer){
            LOGGER.debug("Starting unzip...");

            String fileName = SaverUtils.getFileName(templateName);
            if(fileName == null){
                LOGGER.error("Could not find saved server with key '"+templateName+"'");
                return;
            }

            File savedServerFolder = new File(SaverUtils.SAVE_FOLDER, fileName);
            try {
                SaverUtils.ZipHelper.unzipFolder(savedServerFolder, serverFolder.toPath());
            } catch (IOException e) {
                LOGGER.error("Could not unzip saved server '"+fileName+"!");
            }

            LOGGER.debug("Unzip done");

            Utils.delete(savedServerFolder);
            SaverUtils.delete(templateName);
        }else{
            LOGGER.debug("Starting copy...");
            Utils.copyDirectory(templateFolder, serverFolder);
            LOGGER.debug("Copy done !");

            LOGGER.debug("Editing 'ServerManager/config.json'...");
            this.editServerManagerPluginConfig(serverFolder, name[0], initialServerData);
            LOGGER.debug("'ServerManager/config.json' edited");

        }

        LOGGER.debug("Getting ready to start the server...");

        String startFileName = template.get("startFileName");
        LOGGER.debug("Server start file name is '"+startFileName+"'");

        boolean isBatchFile = template.get("isBatchFile");
        boolean isShellFile = template.get("isShellFile");
        boolean isJarFile = template.get("isJarFile");
        boolean noGui = template.getSection("server.properties").get("noGui");

        try {
            this.editVanillaConfig(template, new File(serverFolder, "server.properties"), name[0]);

            this.editProxyConfig(template, serverFolder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        LOGGER.info("Task complete");
        ProcessBuilder processBuilder;
        if(isBatchFile){
            processBuilder = new ProcessBuilder(startFileName);
            processBuilder.directory(serverFolder);
        }else if(isJarFile){
            List<String> args = new ArrayList<>();
            args.add("java");
            args.add("-jar");
            if(template.contains("additionalStartArgs") && !template.get("additionalStartArgs").toString().isEmpty()){
                args.addAll(Arrays.asList(template.get("additionalStartArgs").toString().split(" ")));
            }
            args.add(startFileName);

            if(noGui){
                args.add("nogui");
            }

            processBuilder = new ProcessBuilder(args);
            processBuilder.directory(serverFolder);
        }else{
            processBuilder = new ProcessBuilder(startFileName);
            processBuilder.directory(serverFolder);
        }

        System.setProperty("IReallyKnowWhatIAmDoingISwear", "true");

        fileManager.followProcess(new ServerProcess(fileManager, processBuilder, name[0], template, serverFolder, template.get("keepLogs"), templateName));
    }

    private void editProxyConfig(Configuration template, File serverFolder) throws IOException {
        File configFile = new File(serverFolder, "config.yml");
        if(!configFile.exists()){
            configFile = new File(serverFolder, "velocity.toml");
        }

        StringBuilder stringBuilder = new StringBuilder();

        Configuration.ConfigurationSection section = template.getSection("config.yml");
        boolean editMaxPlayers = section.get("editMaxPlayers");

        if(configFile.exists()){
            BufferedReader reader = new BufferedReader(new FileReader(configFile));
            reader.lines().forEach(line -> {
                try{
                    if(editMaxPlayers && line.contains("max_players:")){
                        stringBuilder.append("  max_players: ").append(Utils.getIntegerFromPacket(section.get("maxPlayers"))).append("\n");
                    }else if(editMaxPlayers && line.contains("show-max-players")){
                        stringBuilder.append("show-max-players = ").append(Utils.getIntegerFromPacket(section.get("maxPlayers"))).append("\n");
                    }
                    else{
                        stringBuilder.append(line).append("\n");
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            });
            reader.close();

            FileWriter fileWriter = new FileWriter(configFile);
            fileWriter.write(stringBuilder.toString());
            fileWriter.close();
        }
    }

    private void editServerManagerPluginConfig(File serverFolder, String serverName, Map<String, Object> initialServerData) {
        boolean isSponge = new File(serverFolder, "mods").exists();
        boolean isVelocity = new File(serverFolder, "velocity.toml").exists();

        File configJson = new File(serverFolder, isSponge ? "config/servermanager/config.json" : isVelocity ? "plugins/servermanager/config.json" : "plugins/ServerManager/config.json");
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
        Map<String, Object> packetManagerMap = new HashMap<>();
        packetManagerMap.put("serverPort", ServerManager.get().getCoreData().getPacketManagerPort());
        packetManagerMap.put("serverAddress", ServerManager.get().getCoreData().getPacketManagerHost());
        map.put("packet-manager", packetManagerMap);
        if(initialServerData != null){
            map.put("initialServerData", initialServerData);
        }

        try {
            FileWriter fileWriter = new FileWriter(configJson);
            fileWriter.write(Utils.GSON.toJson(map));
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void editVanillaConfig(Configuration template, File propertiesFile, String serverName) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();

        Configuration.ConfigurationSection section = template.getSection("server.properties");
        boolean editServerIP = section.get("editServerIP");
        boolean editServerPort = section.get("editServerPort");
        boolean editServerName = section.get("editServerName");
        boolean editMaxPlayers = section.get("editMaxPlayers");

        if(propertiesFile.exists()){
            BufferedReader reader = new BufferedReader(new FileReader(propertiesFile));
            reader.lines().forEach(line -> {
                try{
                    if(editServerIP && line.contains("server-ip=")){
                        stringBuilder.append("server-ip="+ Utils.getLocalHost().getHostAddress()+"\n");
                    }
                    else if(editServerPort && line.contains("server-port=")){
                        ServerSocket socket = new ServerSocket(0);
                        stringBuilder.append("server-port="+ socket.getLocalPort()+"\n");
                        socket.close();
                    }
                    else if(editServerName && line.contains("server-name=")){
                        stringBuilder.append("server-name="+ serverName+"\n");
                    }
                    else if(editMaxPlayers && line.contains("max-players=")){
                        stringBuilder.append("max-players="+Utils.getIntegerFromPacket(section.get("maxPlayers"))+"\n");
                    }
                    else{
                        stringBuilder.append(line).append("\n");
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            });
            reader.close();
            FileWriter fileWriter = new FileWriter(propertiesFile);
            fileWriter.write(stringBuilder.toString());
            fileWriter.close();

            File eulaFile = new File(propertiesFile.getParentFile(), "eula.txt");
            if(!eulaFile.exists()){
                eulaFile.createNewFile();
                FileWriter fileWriter1 = new FileWriter(eulaFile);
                fileWriter1.write("eula=true");
                fileWriter1.close();
            }
        }
    }

    public File getServerFolder() {
        return serverFolder;
    }

    public File getTargetFolder() {
        return targetFolder;
    }

    public File getTemplateFolder() {
        return templateFolder;
    }

    public void setTargetFolder(File targetFolder) {
        this.targetFolder = targetFolder;
    }

    public String getName() {
        return name[0];
    }
}
