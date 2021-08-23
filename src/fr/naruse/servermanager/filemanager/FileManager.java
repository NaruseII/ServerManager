package fr.naruse.servermanager.filemanager;

import com.diogonunes.jcolor.Attribute;
import fr.naruse.servermanager.core.*;
import fr.naruse.servermanager.core.config.Configuration;
import fr.naruse.servermanager.core.connection.packet.PacketExecuteConsoleCommand;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import fr.naruse.servermanager.core.utils.Updater;
import fr.naruse.servermanager.core.utils.Utils;
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
            String line;
            try{
                line = scanner.nextLine();
            }catch (NoSuchElementException e){
                continue;
            }

            String[] args = line.split(" ");
            if(line.startsWith("stop")){

                System.exit(0);
            }else if(line.startsWith("shutdown")){
                if(args.length == 1){
                    ServerManagerLogger.error("shutdown <Server name>");
                }
                if(args[1].equalsIgnoreCase("all")){
                    for (String s : new HashSet<>(this.serverProcesses.keySet())) {
                        this.shutdownServer(s);
                    }
                }else{
                    for (String s : new HashSet<>(this.serverProcesses.keySet())) {
                        if(s.startsWith(args[1])){
                            this.shutdownServer(s);
                        }
                    }
                }
            }else if(line.startsWith("generateSecretKey")){
                ServerManagerLogger.info("Generation...");
                ServerManagerLogger.info("Key generated: "+this.serverManager.generateNewSecretKey());
            }else if(line.startsWith("create")){
                if(args.length == 1){
                    ServerManagerLogger.error("create <Template Name>");
                }else{
                    int count = 1;
                    if(args.length == 3){
                        try{
                            count = Integer.valueOf(args[2]);
                        }catch (Exception e){
                            ServerManagerLogger.error("create <Template Name> <[count]>");
                            continue;
                        }
                    }
                    for (int i = 0; i < count; i++) {
                        this.createServer(args[1]);
                    }
                }
            }else if(line.startsWith("status")){
                serverManager.printStatus();
            }else if(line.startsWith("scale")){
                if(this.autoScaler != null){
                    this.autoScaler.scale();
                    ServerManagerLogger.info("Scaled");
                }
            }else if(line.startsWith("insertCommand")){
                if(args.length <= 2){
                    ServerManagerLogger.error("insertCommand <Server name> <Cmd>");
                }else{
                    ServerProcess serverProcess = null;
                    for (String s : new HashSet<>(this.serverProcesses.keySet())) {
                        if(s.startsWith(args[1])) {
                            serverProcess = this.serverProcesses.get(s);
                            break;
                        }
                    }
                    if(serverProcess == null){
                        ServerManagerLogger.error("Server '"+args[1]+"' not found");
                    }else{
                        StringBuilder stringBuilder = new StringBuilder(" ");
                        for (int i = 2; i < args.length; i++) {
                            stringBuilder.append(" ").append(args[i]);
                        }
                        String command = stringBuilder.toString().replace("  ", "");
                        Server server = ServerList.getByName(serverProcess.getName());
                        if(server == null){
                            ServerManagerLogger.error("Server '"+args[1]+"' is starting...");
                        }else{
                            server.sendPacket(new PacketExecuteConsoleCommand(command));
                            ServerManagerLogger.info("Command sent");
                        }
                    }
                }
            }else if(line.startsWith("deleteUnUsedLogs")){
                File file = new File("serverLogs");
                if(file.exists() && file.listFiles() != null){
                    for (File f : file.listFiles()) {
                        if(!this.serverProcesses.keySet().contains(f.getName())){
                            Utils.delete(f);
                        }
                    }
                }
                ServerManagerLogger.info("Done");
            }else if(line.startsWith("screen -l")){
                ServerManagerLogger.info(Attribute.CYAN_TEXT(), "Screens:");
                for (String name : new HashSet<>(serverProcesses.keySet())) {
                    ServerManagerLogger.info(Attribute.MAGENTA_TEXT(), name);
                }
                ServerManagerLogger.info("Done");
            }else if(line.startsWith("viewScreen") && args.length > 0){
                ServerProcess serverProcess = null;
                for (String s : new HashSet<>(this.serverProcesses.keySet())) {
                    if(s.startsWith(args[1])) {
                        serverProcess = this.serverProcesses.get(s);
                        break;
                    }
                }

                if(serverProcess == null){
                    ServerManagerLogger.error("Could not find process '"+args[2]+"'");
                }

                if(serverProcess.getScreen().isAttached()){
                    serverProcess.getScreen().detachFromScreen();
                }else{
                    for (ServerProcess process : new HashSet<>(serverProcesses.values())) {
                        if(process.getScreen().isAttached()){
                            process.getScreen().detachFromScreen();
                        }
                    }
                    serverProcess.getScreen().attachToScreen();
                }
            }else if(line.startsWith("detach")){
                for (ServerProcess process : new HashSet<>(serverProcesses.values())) {
                    if(process.getScreen().isAttached()){
                        process.getScreen().detachFromScreen();
                    }
                }
            }else{
                ServerManagerLogger.info(Attribute.CYAN_TEXT(), "Available commands:");
                ServerManagerLogger.info("");
                ServerManagerLogger.info("-> stop (Stop server)");
                ServerManagerLogger.info("-> shutdown <Server name, All>");
                ServerManagerLogger.info("-> status");
                ServerManagerLogger.info("-> generateSecretKey");
                ServerManagerLogger.info("-> create <Template Name> <[count]>");
                ServerManagerLogger.info("-> scale");
                ServerManagerLogger.info("-> insertCommand <Server name> <Cmd>");
                ServerManagerLogger.info("-> deleteUnUsedLogs");
                ServerManagerLogger.info("-> screen -l (List all processes)");
                ServerManagerLogger.info("-> viewScreen <Process Name> (Attach on screen)");
                ServerManagerLogger.info("-> detach (Detach from current screen)");
                ServerManagerLogger.info("");
            }
        }
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
}
