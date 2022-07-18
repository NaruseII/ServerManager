package fr.naruse.servermanager.filemanager;

import fr.naruse.servermanager.core.*;
import fr.naruse.servermanager.core.api.events.plugin.PluginFileManagerEvent;
import fr.naruse.api.config.Configuration;
import fr.naruse.api.logging.GlobalLogger;
import fr.naruse.servermanager.core.plugin.Plugins;
import fr.naruse.servermanager.core.utils.Updater;
import fr.naruse.servermanager.core.utils.Utils;
import fr.naruse.servermanager.filemanager.auto.AutoKiller;
import fr.naruse.servermanager.filemanager.auto.AutoScaler;
import fr.naruse.servermanager.filemanager.command.FileManagerCommand;
import fr.naruse.servermanager.filemanager.event.FileManagerEventListener;
import fr.naruse.servermanager.filemanager.packet.FileManagerPacketListener;
import fr.naruse.servermanager.filemanager.task.CreateServerTask;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;

public class FileManager {

    public static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();
    public static final ExecutorService ERROR_EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    private static FileManager instance;
    public static FileManager get() {
        return instance;
    }

    public static void main(String[] args) {
        long millis  = System.currentTimeMillis();
        GlobalLogger.info("Starting FileManager...");
        if(Updater.needToUpdate(CoreServerType.FILE_MANAGER)){
            return;
        }
        new FileManager(millis);
    }

    private final ServerManager serverManager;
    private final ConcurrentHashMap<String, ServerProcess> serverProcesses = new ConcurrentHashMap<>();
    private AutoScaler autoScaler;
    private AutoKiller autoKiller;

    public FileManager(long millis) {
        instance = this;

        this.serverManager = new ServerManager(new CoreData(CoreServerType.FILE_MANAGER, new File("configs"), "file-manager-"+Utils.randomLetters(4)+"-"+Utils.randomLetters(4), 0)){
            @Override
            public void shutdown() {
                Plugins.shutdownPlugins();

                if(autoScaler != null){
                    GlobalLogger.info("Stopping AutoScaler...");
                    autoScaler.shutdown();
                }

                if(autoKiller != null){
                    GlobalLogger.info("Stopping AutoKiller...");
                    autoKiller.shutdown();
                }

                GlobalLogger.info("Stopping servers...");

                Set<ServerProcess> set = new HashSet<>(serverProcesses.values());

                shutdownAllServers();

                while (true){
                    boolean breakLoop = true;
                    for (ServerProcess value : set) {
                        if(!value.isStopped()){
                            breakLoop = false;
                        }
                    }
                    if(breakLoop){
                        break;
                    }
                }

                GlobalLogger.info("Stopping server creator thread pool...");
                EXECUTOR_SERVICE.shutdown();
                long startEndMillis = System.currentTimeMillis();
                while (!EXECUTOR_SERVICE.isTerminated()){
                    if(System.currentTimeMillis()-startEndMillis > 5000){
                        GlobalLogger.info("Killing interrupted remaining threads...");
                        EXECUTOR_SERVICE.shutdownNow();
                        break;
                    }
                }

                GlobalLogger.info("Stopping task threads...");

                ERROR_EXECUTOR_SERVICE.shutdown();
                super.shutdown();
            }
        };

        GlobalLogger.info("Looking for undeleted servers...");
        int found = 0;
        for (Configuration template : serverManager.getConfigurationManager().getAllTemplates()) {
            File serverFolder = new File((String) template.get("pathTarget"));
            if(serverFolder.listFiles() != null){
                for (File file : serverFolder.listFiles()) {
                    if(file.getName().startsWith(template.get("baseName"))){
                        Utils.delete(file);
                        file.delete();
                        found++;
                    }
                }
            }
        }
        GlobalLogger.info(found+" undeleted servers found and deleted.");

        serverManager.registerEventListener(new FileManagerEventListener(this));
        serverManager.registerPacketProcessing(new FileManagerPacketListener());

        Configuration.ConfigurationSection autoScalerSection = serverManager.getConfigurationManager().getConfig().getSection("autoScaler");
        if(autoScalerSection.get("enabled")){
            Set<Configuration.ConfigurationSection> sectionSet = new HashSet<>();

            serverManager.getConfigurationManager().getAllTemplates().forEach(configuration -> {

                String baseName = configuration.get("baseName");

                if(autoScalerSection.contains(baseName)){
                    List<Configuration> list = autoScalerSection.getSectionList(baseName);
                    if(list == null){
                        sectionSet.add(autoScalerSection.getSection(baseName));
                    }else{
                        for (Configuration config : list) {
                            Configuration.ConfigurationSectionMain section = config.getMainSection();
                            section.setInitialPath(baseName);
                            sectionSet.add(section);
                        }
                    }
                }
            });

            this.autoScaler = new AutoScaler(this, sectionSet);
        }

        Configuration.ConfigurationSection autoKillerSection = serverManager.getConfigurationManager().getConfig().getSection("autoKiller");
        if(autoKillerSection.get("enabled")){
            Map<Configuration, Integer> timeOutMap = new HashMap<>();

            serverManager.getConfigurationManager().getAllTemplates().forEach(configuration -> {

                String baseName = configuration.get("baseName");

                if(autoKillerSection.contains(baseName)){
                    timeOutMap.put(configuration, Utils.getIntegerFromPacket(autoKillerSection.get(baseName)));
                }
            });

            this.autoKiller = new AutoKiller(this, timeOutMap);
        }

        FileManagerCommand fileManagerCommand = new FileManagerCommand(this);
        Plugins.loadPlugins();

        GlobalLogger.info("Start done! (It took "+(System.currentTimeMillis()-millis)+"ms)");

        GlobalLogger.info("");
        GlobalLogger.info("Type help to see commands");

        Configuration.ConfigurationSection section = this.serverManager.getConfigurationManager().getConfig().getSection("startServerOnStart");
        section.getAll().forEach((templateName, o) -> {
            int count = Utils.getIntegerFromPacket(o);
            for (int i = 0; i < count; i++) {
                this.createServer(templateName);
            }
        });

        fileManagerCommand.run();
    }

    public void createServer(String templateName){
        this.createServer(templateName, false, null);
    }

    public void createServer(String templateName, boolean isSavedServer){
        this.createServer(templateName, isSavedServer, null);
    }

    public void createServer(String templateName, Map<String, Object> initialServerData){
        this.createServer(templateName, false, initialServerData);
    }

    public void createServer(String templateName, boolean isSavedServer, Map<String, Object> initialServerData){
        Future future = EXECUTOR_SERVICE.submit(() -> {

            //Plugin event
            PluginFileManagerEvent.AsyncPreCreateServerEvent event = new PluginFileManagerEvent.AsyncPreCreateServerEvent(templateName);
            Plugins.fireEvent(event);
            if(event.isCancelled()){
                return;
            }

            CreateServerTask createServerTask = new CreateServerTask(this, templateName, initialServerData, isSavedServer);

            Plugins.fireEvent(new PluginFileManagerEvent.AsyncPostCreateServerEvent(createServerTask, initialServerData));
        });
        ERROR_EXECUTOR_SERVICE.submit(() -> {
            try {
                future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

    public void followProcess(ServerProcess process) {
        this.serverProcesses.put(process.getName(), process);
        try {
            process.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ServerProcess getServerProcess(String serverName){
        return this.serverProcesses.get(serverName);
    }

    public void shutdownServer(String name){
        this.shutdownServer(name, this.serverProcesses.get(name));
    }

    public void shutdownServer(String name, ServerProcess process){
        if(process == null){
            //GlobalLogger.warn("Server '"+name+"' not found on shutdown (This could be normal)");
            return;
        }
        this.serverProcesses.remove(process.getName());
        if(this.serverManager.isPrimaryThread()){
            Future future = EXECUTOR_SERVICE.submit(() -> process.shutdown());
            ERROR_EXECUTOR_SERVICE.submit(() -> {
                try {
                    future.get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }else{
            process.shutdown();
        }
    }

    private void shutdownAllServers() {
        Set<ServerProcess> set = new HashSet<>(serverProcesses.values());
        for (ServerProcess serverProcess : set) {
            Future future = EXECUTOR_SERVICE.submit(() -> serverProcess.shutdown());
            ERROR_EXECUTOR_SERVICE.submit(() -> {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public ServerManager getServerManager() {
        return serverManager;
    }

    public AutoScaler getAutoScaler() {
        return autoScaler;
    }

    public Set<ServerProcess> getAllServerProcess(){
        return new HashSet<>(this.serverProcesses.values());
    }

    public Map<String, ServerProcess> getServerProcessesMap() {
        return serverProcesses;
    }
}
