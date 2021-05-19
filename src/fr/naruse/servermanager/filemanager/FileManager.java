package fr.naruse.servermanager.filemanager;

import fr.naruse.servermanager.core.CoreData;
import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FileManager {

    private static FileManager instance;
    public static FileManager get() {
        return instance;
    }

    public static void main(String[] args) {
        new FileManager();
    }

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();
    private final ServerManager serverManager;
    private final Map<String, ServerProcess> serverProcesses = new HashMap<>();

    public FileManager() {
        instance = this;
        long millis  = System.currentTimeMillis();
        ServerManagerLogger.info("Starting FileManager...");

        this.serverManager = new ServerManager(new CoreData(CoreServerType.FILE_MANAGER, new File("configs"), 4848, "file-manager", 0)){
            @Override
            public void shutdown() {
                ServerManagerLogger.info("Stopping servers...");
                for (ServerProcess serverProcess : serverProcesses.values()) {
                    serverProcess.shutdown();
                }
                ServerManagerLogger.info("Stopping server creator thread pool...");
                EXECUTOR_SERVICE.shutdown();
                super.shutdown();
            }
        };

        ServerManagerLogger.info("Start done! (It took "+(System.currentTimeMillis()-millis)+"ms)");

        ServerManagerLogger.info("");
        ServerManagerLogger.info("Type help to see commands");

        Scanner scanner = new Scanner(System.in);
        while (true){
            String line = scanner.nextLine();
            String[] args = line.split(" ");
            if(line.startsWith("help")){
                ServerManagerLogger.info("Available commands:");
                ServerManagerLogger.info("stop (Stop server)");
                ServerManagerLogger.info("shutdown <Server name>");
                ServerManagerLogger.info("listServer");
                ServerManagerLogger.info("generateSecretKey");
            }else if(line.startsWith("stop")){
                System.exit(0);
            }else if(line.startsWith("shutdown")){
                ServerProcess serverProcess = this.serverProcesses.get(args[1]);
                if(serverProcess == null){
                    ServerManagerLogger.error("Server '"+args[1]+"' not found");
                }else{
                    this.shutdownServer(serverProcess);
                }
            }else if(line.startsWith("listServer")){
                ServerManagerLogger.info("Server list:");
                for (String s : this.serverProcesses.keySet()) {
                    ServerManagerLogger.info(" -> "+s);
                }
            }else if(line.startsWith("generateSecretKey")){
                ServerManagerLogger.info("Generation...");
                ServerManagerLogger.info("Key generated: "+this.serverManager.generateNewSecretKey());
            }
        }
    }

    public void createServer(String templateName){
        Future future = EXECUTOR_SERVICE.submit(() -> {
            new CreateServerTask(this, templateName);
        });
        EXECUTOR_SERVICE.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void followProcess(ServerProcess process) {
        this.serverProcesses.put(process.getName(), process);
        process.start();
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
}
