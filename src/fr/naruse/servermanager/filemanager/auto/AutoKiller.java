package fr.naruse.servermanager.filemanager.auto;

import fr.naruse.servermanager.core.config.Configuration;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import fr.naruse.servermanager.filemanager.FileManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

public class AutoKiller {

    private static final ServerManagerLogger.Logger LOGGER = new ServerManagerLogger.Logger("AutoKiller");
    private static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();

    private final FileManager fileManager;
    private final Map<Configuration, Integer> timeOutMap;
    private final Map<Server, Integer> serverIntegerMap = new HashMap<>();

    public AutoKiller(FileManager fileManager, Map<Configuration, Integer> timeOutMap) {
        this.fileManager = fileManager;
        this.timeOutMap = timeOutMap;
        LOGGER.info("Started");

        Future future = EXECUTOR_SERVICE.scheduleAtFixedRate(() -> this.killAllTimedOut(), 1, 1, TimeUnit.SECONDS);
        FileManager.EXECUTOR_SERVICE.submit(() -> {
            try {
                future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

    public void killAllTimedOut(){
        Set<Server> set = ServerList.getAll();

        this.timeOutMap.forEach((configuration, timeOut) -> {
            set.stream().filter(server -> server.getName().startsWith(configuration.get("baseName"))).forEach(server -> {
                Integer integer = this.serverIntegerMap.get(server);
                if(integer == null){
                    this.serverIntegerMap.put(server, timeOut);
                }else if(integer == -1){
                    return;
                }else if (server.getData().hasStatus(Server.Status.READY)) {
                    if(integer <= 0){

                        if(this.fileManager.getAutoScaler() != null){
                            for (Configuration.ConfigurationSection section : this.fileManager.getAutoScaler().getSectionSet()) {
                                if(server.getName().startsWith(section.getInitialPath()) && AutoScaler.Matches.valueOf(section.get("match")) == AutoScaler.Matches.WHEN_SERVER_COUNT_IS_UNDER){

                                    int count = (int) AutoScaler.Matches.WHEN_SERVER_COUNT_IS_UNDER.transformValue(section.get("value"));
                                    long serverCount = set.stream().filter(s -> s.getName().startsWith(section.getInitialPath())).count();

                                    if(serverCount <= count){
                                        this.serverIntegerMap.put(server, timeOut);
                                        return;
                                    }

                                }
                            }
                        }

                        this.serverIntegerMap.put(server, -1);
                        this.fileManager.shutdownServer(server.getName());
                    }else{
                        this.serverIntegerMap.put(server, integer-1);
                    }
                }else{
                    this.serverIntegerMap.put(server, timeOut);
                }
            });
        });

        new HashSet<>(this.serverIntegerMap.keySet()).forEach(server -> {
            if(ServerList.getByName(server.getName()) == null){
                this.serverIntegerMap.remove(server);
            }
        });
    }

    public void shutdown(){
        EXECUTOR_SERVICE.shutdown();
    }
}
