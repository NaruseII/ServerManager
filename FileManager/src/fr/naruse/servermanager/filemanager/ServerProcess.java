package fr.naruse.servermanager.filemanager;

import fr.naruse.api.config.Configuration;
import fr.naruse.servermanager.core.connection.packet.PacketShutdown;
import fr.naruse.api.logging.GlobalLogger;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import fr.naruse.servermanager.core.utils.Utils;
import fr.naruse.servermanager.filemanager.task.DeleteServerTask;
import fr.naruse.servermanager.filemanager.task.SaveServerTask;

import java.io.*;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class ServerProcess {

    private static final File LOG_FOLDER = new File("serverLogs");
    public static boolean BE_PATIENT = false;
    private final GlobalLogger.Logger LOGGER = new GlobalLogger.Logger("");

    private final FileManager fileManager;
    private final Screen screen;
    private Process process;
    private final ProcessBuilder processBuilder;
    private final String name;
    private final Configuration template;
    private final File logFile;
    private final File serverFolder;
    private final boolean keepLogs;
    private final String templateName;
    private long startTime;
    private boolean saveOnShutdown = false;
    private String saveKey;

    private boolean isStopped = false;
    private boolean isShuttingDown = false;

    public ServerProcess(FileManager fileManager, ProcessBuilder processBuilder, String name, Configuration template, File serverFolder, boolean keepLogs, String templateName) {
        LOGGER.setTag("ServerProcess - "+name);
        LOGGER.info("Starting following process...");

        this.fileManager = fileManager;
        this.name = name;
        this.template = template;
        this.processBuilder = processBuilder;
        this.serverFolder = serverFolder;
        this.keepLogs = keepLogs;
        this.templateName = templateName;

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
        this.screen = new Screen(this);
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
            this.startTime = System.currentTimeMillis();
            LOGGER.info("Server started");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        if(this.isShuttingDown){
            return;
        }
        this.isShuttingDown = true;
        if(this.process.isAlive()){
            LOGGER.info("Stopping server...");

            Server server = ServerList.getByName(this.name);
            if(server != null){
                this.fileManager.getServerManager().getConnectionManager().sendPacket(server, new PacketShutdown());
                Utils.sleep(10000);

                if(process.isAlive()){
                    LOGGER.info("Server is still alive! Killing it... (It may take several seconds)");
                    destroy();
                    waitFor();
                }
            }else{
                LOGGER.info("Server didn't fully started! Killing it... (It may take several seconds)");
                destroy();
                waitFor();
            }
            LOGGER.info("Server stopped");
        }
        this.isStopped = true;

        if(this.shouldSaveOnShutdown()){
            new SaveServerTask(this, this.getSaveKey());
        }

        new DeleteServerTask(this.template, this.name);
        this.screen.detachFromScreen();
    }

    private void destroy() {
        AtomicBoolean cant = new AtomicBoolean(false);
        try {
            Method method = this.process.getClass().getDeclaredMethod("descendants");
            if(method != null){
                Stream stream = (Stream) method.invoke(process);
                stream.forEach(o -> {
                    try {
                        Method m = o.getClass().getMethod("destroy");
                        m.invoke(o);
                    } catch (Exception e) {
                        cant.set(true);
                    }
                });
            }
        } catch (Exception e) {
            cant.set(true);
        }
        this.process.destroy();
        if(cant.get() && System.currentTimeMillis()-startTime < 60000){
            LOGGER.debug("I detected a recent start, but I can't stop all subprocess on Java below 1.9! You'll probably need to stop it manually.");
        }
    }

    private void waitFor(){
        try {
            this.process.waitFor(17, TimeUnit.SECONDS);
            Utils.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return this.name;
    }

    public File getLogFile() {
        return this.logFile;
    }

    public static File getLogFolder() {
        return LOG_FOLDER;
    }

    public File getServerFolder() {
        return this.serverFolder;
    }

    public Configuration getTemplate() {
        return this.template;
    }

    public boolean isStopped() {
        return this.isStopped;
    }

    public Screen getScreen() {
        return this.screen;
    }

    public void setSaveOnShutdown(boolean saveOnShutdown, String saveKey) {
        this.saveOnShutdown = saveOnShutdown;
        this.saveKey = saveKey;
    }

    public boolean shouldSaveOnShutdown() {
        return this.saveOnShutdown;
    }

    public String getSaveKey() {
        return this.saveKey;
    }

    public String getTemplateName() {
        return this.templateName;
    }
}