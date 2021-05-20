package fr.naruse.servermanager.filemanager;

import fr.naruse.servermanager.core.CoreData;
import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.Utils;
import fr.naruse.servermanager.core.config.Configuration;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;
import fr.naruse.servermanager.filemanager.event.FileManagerEventListener;
import fr.naruse.servermanager.filemanager.packet.FileManagerPacketProcessing;
import fr.naruse.servermanager.filemanager.task.CreateServerTask;
import fr.naruse.servermanager.filemanager.task.EditBungeeConfigFile;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;

public class FileManager {

    public static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    private static FileManager instance;
    public static FileManager get() {
        return instance;
    }

    public static void main(String[] args) {
        new FileManager();
    }

    private final ServerManager serverManager;
    private final Map<String, ServerProcess> serverProcesses = new HashMap<>();
    private AutoScaler autoScaler;
    private AutoKiller autoKiller;

    public FileManager() {
        instance = this;
        long millis  = System.currentTimeMillis();
        ServerManagerLogger.info("Starting FileManager...");

        this.serverManager = new ServerManager(new CoreData(CoreServerType.FILE_MANAGER, new File("configs"), 4848, "file-manager", 0)){
            @Override
            public void shutdown() {
                if(autoScaler != null){
                    autoScaler.shutdown();
                }

                if(autoKiller != null){
                    autoKiller.shutdown();
                }

                ServerManagerLogger.info("Stopping servers...");
                Set<ServerProcess> set = new HashSet<>(serverProcesses.values());
                for (ServerProcess serverProcess : set) {
                    EXECUTOR_SERVICE.submit(() -> serverProcess.shutdown());
                }
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

                ServerManagerLogger.info("Stopping task threads...");
                EditBungeeConfigFile.EXECUTOR_SERVICE.shutdown();
                super.shutdown();
            }
        };
        serverManager.registerPacketProcessing(new FileManagerPacketProcessing(this));
        serverManager.registerEventListener(new FileManagerEventListener(this));

        Configuration.ConfigurationSection autoScalerSection = serverManager.getConfigurationManager().getConfig().getSection("autoScaler");
        if(autoScalerSection.get("enabled")){
            Set<Configuration.ConfigurationSection> sectionSet = new HashSet<>();

            serverManager.getConfigurationManager().getAllTemplates().forEach(configuration -> {

                String baseName = configuration.get("baseName");

                if(autoScalerSection.contains(baseName)){
                    sectionSet.add(autoScalerSection.getSection(baseName));
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


        Scanner scanner = new Scanner(System.in);
        while (true){
            String line = scanner.nextLine();
            String[] args = line.split(" ");
            if(line.startsWith("help")){
                ServerManagerLogger.info("Available commands:");
                ServerManagerLogger.info("stop (Stop server)");
                ServerManagerLogger.info("shutdown <Server name>");
                ServerManagerLogger.info("status");
                ServerManagerLogger.info("generateSecretKey");
                ServerManagerLogger.info("createServer <Template Name>");
                ServerManagerLogger.info("scale");
            }else if(line.startsWith("stop")){
                System.exit(0);
            }else if(line.startsWith("shutdown")){
                if(args.length == 1){
                    ServerManagerLogger.error("shutdown <Server name>");
                }
                ServerProcess serverProcess = this.serverProcesses.get(args[1]);
                if(serverProcess == null){
                    ServerManagerLogger.error("Server '"+args[1]+"' not found");
                }else{
                    this.shutdownServer(serverProcess);
                }
            }else if(line.startsWith("generateSecretKey")){
                ServerManagerLogger.info("Generation...");
                ServerManagerLogger.info("Key generated: "+this.serverManager.generateNewSecretKey());
            }else if(line.startsWith("createServer")){
                if(args.length == 1){
                    ServerManagerLogger.error("createServer <Template Name>");
                }else{
                    this.createServer(args[1]);
                }
            }else if(line.startsWith("status")){
                serverManager.printStatus();
            }else if(line.startsWith("scale")){
                if(this.autoScaler != null){
                    this.autoScaler.scale();
                }
            }
        }
    }

    public void createServer(String templateName){
        Future future = EXECUTOR_SERVICE.submit(() -> {
            new CreateServerTask(this, templateName);
        });
        EXECUTOR_SERVICE.submit(() -> {
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
        this.shutdownServer(this.serverProcesses.get(name));
    }

    public void shutdownServer(ServerProcess process){
        if(process == null){
            ServerManagerLogger.error("Server not found");
            return;
        }
        this.serverProcesses.remove(process.getName());
        if(this.serverManager.isPrimaryThread()){
            EXECUTOR_SERVICE.submit(() -> process.shutdown());
        }else{
            process.shutdown();
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
}
