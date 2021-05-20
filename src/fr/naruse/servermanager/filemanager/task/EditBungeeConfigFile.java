package fr.naruse.servermanager.filemanager.task;

import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.config.Configuration;
import fr.naruse.servermanager.core.connection.packet.PacketReloadBungeeServers;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import fr.naruse.servermanager.filemanager.ServerProcess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditBungeeConfigFile {

    public static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
    private static final ServerManagerLogger.Logger LOGGER = new ServerManagerLogger.Logger("");

    private boolean isOnServers = false;
    private boolean foundClient = false;

    private boolean deleteCurrent = false;

    private boolean isOnPriorities = false;
    private boolean priorityAppended = false;

    public EditBungeeConfigFile(String serverName, String hostAddress, int port, ServerProcess process, boolean needToDelete) throws Exception {
        LOGGER.setTag("EditBungeeConfigTask - "+process.getName());
        LOGGER.info("Launching new task...");

        File configFile = new File(process.getServerFolder(), "config.yml");
        if(!configFile.exists()){
            configFile.createNewFile();
            LOGGER.info("'config.yml' created");
        }

        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(configFile));

        Configuration.ConfigurationSection prioritiesSection = process.getTemplate().getSection("config.yml").getSection("priorities");
        Optional<Server> optionalServer = ServerList.findServer(CoreServerType.BUKKIT_MANAGER, ServerList.SortType.valueOf(prioritiesSection.get("sortType")), prioritiesSection.get("forceOnTemplate"));

        reader.lines().forEach(line -> {

            int spaceCount = 0;
            for (char c : line.toCharArray()) {
                if(c == ' '){
                    spaceCount++;
                }
            }

            if(this.isOnServers){

                if(spaceCount == 2){
                    if((needToDelete && line.contains(serverName)) || !ServerList.getAllNames().contains(line.replace(" ", ""))){
                        this.deleteCurrent = true;
                        return;
                    }else if(this.deleteCurrent){
                        this.deleteCurrent = false;
                    }
                }else if(spaceCount == 0){
                    this.isOnServers = false;
                }else if(this.deleteCurrent){
                    return;
                }
            }

            if(line.contains("force_default_server")){
                stringBuilder.append("  force_default_server: ").append(true).append("\n");
            }
            if(this.isOnPriorities){
                if(!this.priorityAppended){
                    this.priorityAppended = true;
                    if(optionalServer.isPresent()){
                        Server server = optionalServer.get();
                        stringBuilder.append("    - ").append(server.getName()).append("\n");
                    }
                }
                if(spaceCount <= 2 && !line.replace(" ", "").startsWith("-")){
                    this.isOnPriorities = false;
                }else{
                    return;
                }
            }

            stringBuilder.append(line).append("\n");
            if(line.contains("servers")){
                this.foundClient = true;
                if(!needToDelete){
                    this.append(stringBuilder, serverName, hostAddress, port);
                }
                this.isOnServers = true;
            }

            if(line.contains("priorities") && (boolean) prioritiesSection.get("editPriorities")){
                this.isOnPriorities = true;
            }
        });
        reader.close();

        if(!this.foundClient && !needToDelete){
            this.append(stringBuilder, serverName, hostAddress, port);
        }

        FileWriter fileWriter = new FileWriter(configFile);
        fileWriter.write(stringBuilder.toString());
        fileWriter.close();

        ServerList.getByName(process.getName()).sendPacket(new PacketReloadBungeeServers());

        LOGGER.info("Task complete");
    }

    private void append(StringBuilder stringBuilder, String serverName, String hostAddress, int port) {
        stringBuilder.append("  ").append(serverName).append(":").append("\n");
        stringBuilder.append("    ").append("motd: ").append(serverName).append("\n");
        stringBuilder.append("    ").append("address: ").append(hostAddress).append(":").append(port).append("\n");
        stringBuilder.append("    ").append("restricted: ").append(false).append("\n");
    }
}
