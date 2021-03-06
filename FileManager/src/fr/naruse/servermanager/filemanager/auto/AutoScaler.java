package fr.naruse.servermanager.filemanager.auto;

import fr.naruse.servermanager.core.utils.Utils;
import fr.naruse.api.config.Configuration;
import fr.naruse.api.logging.GlobalLogger;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import fr.naruse.servermanager.filemanager.FileManager;
import fr.naruse.servermanager.filemanager.ServerProcess;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AutoScaler {

    private static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();
    private static final GlobalLogger.Logger LOGGER = new GlobalLogger.Logger("AutoScaler");

    private final FileManager fileManager;
    private final Set<Configuration.ConfigurationSection> sectionSet;

    private boolean cancel = false;

    public AutoScaler(FileManager fileManager, Set<Configuration.ConfigurationSection> sectionSet) {
        this.fileManager = fileManager;
        this.sectionSet = sectionSet;
        this.EXECUTOR_SERVICE.scheduleAtFixedRate(() -> this.scale(), 5, 5, TimeUnit.SECONDS);
        LOGGER.info("Started");
    }

    public void scale(){
        if(this.cancel){
            return;
        }

        Set<ServerProcess> serverProcesses = FileManager.get().getAllServerProcess();
        Set<String> inCreationTemplates = new HashSet<>();

        for (Configuration.ConfigurationSection section : this.sectionSet) {
            Matches matches = Matches.valueOf(section.get("match"));
            Object value = matches.transformValue(section.get("value"));
            if(value == null){
                continue;
            }

            String baseName = section.getInitialPath();
            if(inCreationTemplates.contains(baseName)){
                continue;
            }

            if(matches.match(serverProcesses.stream().filter(server -> server.getName().startsWith(baseName)).collect(Collectors.toSet()), value)){

                /*long count = set.stream().filter(process -> ServerList.getByName(process.getName()) == null).count();
                if(count != 0){
                    continue;
                }*/

                for (int i = 0; i < section.getInt("startServers"); i++) {
                    fileManager.createServer(baseName);
                }

                inCreationTemplates.add(baseName);
            }
        }
    }

    public Set<Configuration.ConfigurationSection> getSectionSet() {
        return sectionSet;
    }

    public void shutdown() {
        this.cancel = true;
        this.EXECUTOR_SERVICE.shutdown();
    }

    public abstract static class Matches<T, E> {

        private static final Map<String, Matches> map = new HashMap<>();
        private static Matches registerMatches(String name, Matches matches){
            map.put(name, matches);
            return matches;
        }

        public static Matches valueOf(String name){
            return map.get(name.toUpperCase());
        }

        public static final Matches WHEN_ALL_ON_STATUS = registerMatches("WHEN_ALL_ON_STATUS", new Matches<String, Server.Status>() {
            @Override
            public boolean match(Set<Server> set, Server.Status status) {
                for (Server server : set) {
                    if(server.getData().hasStatus(status)){
                        return false;
                    }
                }
                return set.stream().filter(server -> server.getData().hasStatus(status)).count() != 0;
            }

            @Override
            public Server.Status transformValue(String value) {
                return Server.Status.valueOf(value);
            }
        });

        public static final Matches WHEN_ALL_FULL = registerMatches("WHEN_ALL_FULL", new Matches<String, String>() {

            @Override
            public boolean match(Set<Server> set, String value) {

                for (Server server : set) {
                    if(server.getData().getPlayerSize() < server.getData().getCapacity()){
                        return false;
                    }
                }
                return !set.isEmpty();
            }

            @Override
            public String transformValue(String value) {
                return value;
            }
        });

        public static final Matches WHEN_ALL_STOPPED = registerMatches("WHEN_ALL_STOPPED", new Matches<String, String>() {

            @Override
            public boolean match(Set<Server> set, String value) {
                return set.isEmpty();
            }

            @Override
            public String transformValue(String value) {
                return value;
            }
        });

        public static final Matches WHEN_SERVER_COUNT_IS_UNDER = registerMatches("WHEN_SERVER_COUNT_IS_UNDER", new Matches<Number, Integer>() {

            @Override
            public boolean match(Set<Server> set, Integer value) {
                return set.size() < value;
            }

            @Override
            public Integer transformValue(Number value) {
                return Utils.getIntegerFromPacket(value);
            }
        });

        public abstract boolean match(Set<Server> set, E value) ;

        public abstract E transformValue(T value) ;

        private Matches() {
        }
    }
}
