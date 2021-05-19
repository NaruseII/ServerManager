package fr.naruse.servermanager.filemanager.task;

import fr.naruse.servermanager.core.logging.ServerManagerLogger;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.filemanager.ServerProcess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditBungeeConfigFile {

    public static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
    private static final ServerManagerLogger.Logger LOGGER = new ServerManagerLogger.Logger("EditBungeeConfigTask");

    private boolean isOnServers = false;
    private boolean deleteCurrent = false;

    public EditBungeeConfigFile(String serverName, String hostAddress, int port, ServerProcess process, boolean needToDelete) throws Exception {
        LOGGER.info("Launching new task...");
        File configFile = new File(process.getServerFolder(), "config.yml");
        if(!configFile.exists()){
            configFile.createNewFile();
            LOGGER.info("'config.yml' created");
        }

        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(configFile));

        reader.lines().forEach(line -> {

            if(this.isOnServers && needToDelete){
                int spaceCount = 0;
                for (char c : line.toCharArray()) {
                    if(c == ' '){
                        spaceCount++;
                    }
                }

                if(spaceCount == 2){
                    if(line.contains(serverName)){
                        this.deleteCurrent = true;
                        return;
                    }else if(this.deleteCurrent){
                        this.deleteCurrent = false;
                    }
                }else if(this.deleteCurrent){
                    return;
                }
            }

            stringBuilder.append(line).append("\n");
            if(line.contains("servers")){
                if(!needToDelete){
                    stringBuilder.append("  ").append(serverName).append(":").append("\n");
                    stringBuilder.append("    ").append("motd: ").append(serverName).append("\n");
                    stringBuilder.append("    ").append("address: ").append(hostAddress).append(":").append(port).append("\n");
                    stringBuilder.append("    ").append("restricted: ").append(false).append("\n");
                }else{
                    this.isOnServers = true;
                }
            }
        });
        reader.close();

        FileWriter fileWriter = new FileWriter(configFile);
        fileWriter.write(stringBuilder.toString());
        fileWriter.close();

        LOGGER.info("Task complete");
    }
}
