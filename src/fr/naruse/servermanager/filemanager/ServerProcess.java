package fr.naruse.servermanager.filemanager;

import fr.naruse.servermanager.core.connection.packet.PacketShutdown;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import fr.naruse.servermanager.filemanager.task.DeleteServerTask;

import java.io.*;

public class ServerProcess {

    private static final ServerManagerLogger.Logger LOGGER = new ServerManagerLogger.Logger("ServerProcess");
    private static final File LOG_FOLDER = new File("serverLogs");

    private final FileManager fileManager;
    private Process process;
    private final ProcessBuilder processBuilder;
    private final String name;
    private final String templateName;
    private final File logFile;
    private final File serverFolder;

    public ServerProcess(FileManager fileManager, ProcessBuilder processBuilder, String name, String templateName, File serverFolder) {
        LOGGER.info("Starting following process for '"+name+"'...");
        this.fileManager = fileManager;
        this.name = name;
        this.templateName = templateName;
        this.processBuilder = processBuilder;
        this.serverFolder = serverFolder;

        if(!LOG_FOLDER.exists()){
            LOGGER.info("Creating log folder...");
            LOG_FOLDER.mkdirs();
        }

        File serverLogFolder = new File(LOG_FOLDER, name);
        LOGGER.info("Creating 'serverLogs/"+serverLogFolder.getName()+"'...");
        if(serverLogFolder.exists()){
            serverLogFolder.delete();
        }
        serverLogFolder.mkdirs();

        this.logFile = new File(serverLogFolder, "logs.log");
        LOGGER.info("Creating '"+this.logFile.getName()+"'...");
    }

    public void start() {
        try {
            this.logFile.createNewFile();

            processBuilder.redirectError(logFile);
            processBuilder.redirectOutput(logFile);
            this.process = processBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        if(process.isAlive()){
            LOGGER.info("Stopping server '"+this.name+"'...");

            Server server = ServerList.getByName(this.name);
            if(server != null){
                this.fileManager.getServerManager().getConnectionManager().sendPacket(server, new PacketShutdown());
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if(process.isAlive()){
                    LOGGER.info("Server '"+this.name+"' is still alive! Killing it...");
                    process.destroy();
                }
            }else{
                process.destroy();
            }
            LOGGER.info("Server '"+this.name+"' stopped");
        }
        new DeleteServerTask(this.fileManager, this.templateName, this.name);
    }

    public String getName() {
        return name;
    }

    public File getLogFile() {
        return logFile;
    }

    public static File getLogFolder() {
        return LOG_FOLDER;
    }

    public String getTemplateName() {
        return templateName;
    }

    public File getServerFolder() {
        return serverFolder;
    }
}