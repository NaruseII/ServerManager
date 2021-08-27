package fr.naruse.servermanager.filemanager;

import fr.naruse.servermanager.core.*;
import fr.naruse.servermanager.core.config.Configuration;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;
import fr.naruse.servermanager.core.utils.Updater;
import fr.naruse.servermanager.core.utils.Utils;
import fr.naruse.servermanager.filemanager.auto.AutoKiller;
import fr.naruse.servermanager.filemanager.auto.AutoScaler;
import fr.naruse.servermanager.filemanager.command.FileManagerCommand;
import fr.naruse.servermanager.filemanager.event.FileManagerEventListener;
import fr.naruse.servermanager.filemanager.packet.FileManagerProcessPacketListener;
import fr.naruse.servermanager.filemanager.task.CreateServerTask;
import fr.naruse.servermanager.filemanager.task.EditProxyConfigFile;

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
        ServerManagerLogger.info("Starting FileManager...");
        if(Updater.needToUpdate(CoreServerType.FILE_MANAGER)){
            return;
        }
        new FileManager(millis);
    }

    private final ServerManager serverManager;
    private final Map<String, ServerProcess> serverProcesses = new HashMap<>();
    private AutoScaler autoScaler;
    private AutoKiller autoKiller;

    public FileManager(long millis) {
        instance = this;

        this.serverManager = new ServerManager(new CoreData(CoreServerType.FILE_MANAGER, new File("configs"), 4848, "file-manager-"+Utils.randomLetters(4)+"-"+Utils.randomLetters(4), 0)){
            @Override
            public void shutdown() {
                if(autoScaler != null){
                    ServerManagerLogger.info("Stopping AutoScaler...");
                    autoScaler.shutdown();
                }

                if(autoKiller != null){
                    ServerManagerLogger.info("Stopping AutoKiller...");
                    autoKiller.shutdown();
                }

                ServerManagerLogger.info("Stopping servers...");

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

                ServerManagerLogger.info("Stopping server creator thread pool...");
                EXECUTOR_SERVICE.shutdown();
                while (!EXECUTOR_SERVICE.isTerminated()) ;

                ServerManagerLogger.info("Stopping task threads...");
                EditProxyConfigFile.EXECUTOR_SERVICE.shutdown();
                while (!EditProxyConfigFile.EXECUTOR_SERVICE.isTerminated()) ;

                ERROR_EXECUTOR_SERVICE.shutdown();
                super.shutdown();
            }
        };

        ServerManagerLogger.info("Looking for undeleted servers...");
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
        ServerManagerLogger.info(found+" undeleted servers found and deleted.");

        serverManager.registerPacketProcessing(new FileManagerProcessPacketListener(this));
        serverManager.registerEventListener(new FileManagerEventListener(this));

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

        ServerManagerLogger.info("Start done! (It took "+(System.currentTimeMillis()-millis)+"ms)");

        ServerManagerLogger.info("");
        ServerManagerLogger.info("Type help to see commands");

        Configuration.ConfigurationSection section = this.serverManager.getConfigurationManager().getConfig().getSection("startServerOnStart");
        section.getAll().forEach((templateName, o) -> {
            int count = Utils.getIntegerFromPacket(o);
            for (int i = 0; i < count; i++) {
                this.createServer(templateName);
            }
        });

        new FileManagerCommand(this).run();
    }

    public void createServer(String templateName){
        Future future = EXECUTOR_SERVICE.submit(() -> {
            new CreateServerTask(this, templateName);
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
        process.start();
    }

    public ServerProcess getServerProcess(String serverName){
        return this.serverProcesses.get(serverName);
    }

    public void shutdownServer(String name){
        this.shutdownServer(name, this.serverProcesses.get(name));
    }

    public void shutdownServer(String name, ServerProcess process){
        if(process == null){
            //ServerManagerLogger.warn("Server '"+name+"' not found on shutdown (This could be normal)");
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
