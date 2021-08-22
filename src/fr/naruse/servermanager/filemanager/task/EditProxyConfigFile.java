package fr.naruse.servermanager.filemanager.task;

import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.config.Configuration;
import fr.naruse.servermanager.core.connection.packet.PacketReloadProxyServers;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import fr.naruse.servermanager.core.utils.Utils;
import fr.naruse.servermanager.filemanager.ServerProcess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

public class EditProxyConfigFile {

    public static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
    private final ServerManagerLogger.Logger LOGGER = new ServerManagerLogger.Logger("");

    private boolean isOnServers = false;
    private boolean foundClient = false;

    private boolean deleteCurrent = false;

    private boolean isOnPriorities = false;
    private boolean priorityAppended = false;

    public EditProxyConfigFile(String serverName, String hostAddress, int port, ServerProcess process, boolean needToDelete) throws Exception {
        if(process == null){
            return;
        }

        LOGGER.setTag("EditBungeeConfigTask - "+process.getName());
        LOGGER.info("Launching new task...");
        LOGGER.info((port == 0 ? "Removing '" : "Adding '")+serverName+" -> "+(hostAddress.equals("null") ? "" : hostAddress+":")+port+"'...");

        Configuration.ConfigurationSection configSection = process.getTemplate().getSection("config.yml");
        Configuration.ConfigurationSection prioritiesSection = configSection.getSection("priorities");

        CoreServerType[] defaultServerTypes = new CoreServerType[]{CoreServerType.BUKKIT_MANAGER, CoreServerType.SPONGE_MANAGER};

        Optional<Server> optionalServer = ServerList.findServer(defaultServerTypes, ServerList.SortType.valueOf(prioritiesSection.get("sortType")), prioritiesSection.get("forceOnTemplate"), new Predicate<Server>() {
            @Override
            public boolean test(Server server) {
                return !(needToDelete && server.getName().equals(serverName));
            }
        });

        int count = 0;
        Server bungeeServer;
        do {
            bungeeServer = ServerList.getByName(process.getName());
            if(bungeeServer != null){
                bungeeServer.sendPacket(new PacketReloadProxyServers(optionalServer.isPresent() ? optionalServer.get().getName() : "null", configSection.get("transformToLocalhostIfPossible")));
                break;
            }else{
                if(count > 16){
                    break;
                }
                Thread.sleep(250);
                count++;
            }
        }while (bungeeServer == null);

        //if(true){ // Don't need anymore to edit config.yml or velocity.toml files (Code stays in case I need to edit)
            LOGGER.info("Task complete");
       /*     return;
        }

        File configFile = new File(process.getServerFolder(), "config.yml");
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(configFile));

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
                    this.append(stringBuilder, serverName, hostAddress, port, configSection.get("transformToLocalhostIfPossible"));
                }
                this.isOnServers = true;
            }

            if(line.contains("priorities") && (boolean) prioritiesSection.get("editPriorities")){
                this.isOnPriorities = true;
            }
        });
        reader.close();

        if(!this.foundClient && !needToDelete){
            this.append(stringBuilder, serverName, hostAddress, port, configSection.get("transformToLocalhostIfPossible"));
        }

        FileWriter fileWriter = new FileWriter(configFile);
        fileWriter.write(stringBuilder.toString());
        fileWriter.close();

        LOGGER.info("Task complete");*/
    }

    /*private void append(StringBuilder stringBuilder, String serverName, String hostAddress, int port, boolean transformToLocalhostIfPossible) {
        stringBuilder.append("  ").append(serverName).append(":").append("\n");
        stringBuilder.append("    ").append("motd: ").append(serverName).append("\n");
        stringBuilder.append("    ").append("address: ")
                .append(transformToLocalhostIfPossible ? hostAddress.equals(Utils.getLocalHost().getHostAddress()) ? "localhost" : hostAddress : hostAddress)
                .append(":").append(port).append("\n");
        stringBuilder.append("    ").append("restricted: ").append(false).append("\n");
    }*/
}
