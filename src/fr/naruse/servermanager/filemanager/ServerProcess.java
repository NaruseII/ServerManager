package fr.naruse.servermanager.filemanager;

import fr.naruse.servermanager.core.config.Configuration;
import fr.naruse.servermanager.core.connection.packet.PacketShutdown;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import fr.naruse.servermanager.filemanager.task.DeleteServerTask;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

public class ServerProcess {

    private static final File LOG_FOLDER = new File("serverLogs");
    public static boolean BE_PATIENT = false;
    private final ServerManagerLogger.Logger LOGGER = new ServerManagerLogger.Logger("");

    private final FileManager fileManager;
    private Process process;
    private final ProcessBuilder processBuilder;
    private final String name;
    private final Configuration template;
    private final File logFile;
    private final File serverFolder;
    private final boolean keepLogs;

    private boolean isStopped = false;

    public ServerProcess(FileManager fileManager, ProcessBuilder processBuilder, String name, Configuration template, File serverFolder, boolean keepLogs) {
        LOGGER.setTag("ServerProcess - "+name);
        LOGGER.info("Starting following process...");

        this.fileManager = fileManager;
        this.name = name;
        this.template = template;
        this.processBuilder = processBuilder;
        this.serverFolder = serverFolder;
        this.keepLogs = keepLogs;

        if(!LOG_FOLDER.exists()){
            LOGGER.debug("Creating log folder...");
            LOG_FOLDER.mkdirs();
        }

        File serverLogFolder = new File(LOG_FOLDER, name);
        LOGGER.debug("Creating 'serverLogs/"+serverLogFolder.getName()+"'...");
        if(serverLogFolder.exists()){
            serverLogFolder.delete();
        }
        serverLogFolder.mkdirs();

        this.logFile = new File(serverLogFolder, "logs.log");
        LOGGER.debug("Creating '"+this.logFile.getName()+"'...");
    }

    public void start() {
        try {
            LOGGER.info("Starting server...");
            this.logFile.createNewFile();

            if(this.keepLogs){
                this.processBuilder.redirectError(this.logFile);
                this.processBuilder.redirectOutput(this.logFile);
            }
            this.process = processBuilder.start();
            LOGGER.info("Server started");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        if(process.isAlive()){
            LOGGER.info("Stopping server...");

            Server server = ServerList.getByName(this.name);
            if(server != null){
                this.fileManager.getServerManager().getConnectionManager().sendPacket(server, new PacketShutdown());
                sleep(10000);

                if(process.isAlive()){
                    LOGGER.info("Server is still alive! Killing it... (It may take several seconds)");
                    process.destroy();
                    waitFor();
                }
            }else{
                LOGGER.info("Server didn't fully started! Killing it... (It may take several seconds)");
                process.destroyForcibly();
                waitFor();
            }
            LOGGER.info("Server stopped");
        }
        this.isStopped = true;
        new DeleteServerTask(this.template, this.name);
    }

    private void sleep(long time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void waitFor(){
        try {
            process.waitFor();
            if(BE_PATIENT){
                sleep(30000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public File getServerFolder() {
        return serverFolder;
    }

    public Configuration getTemplate() {
        return template;
    }

    public boolean isStopped() {
        return isStopped;
    }
}