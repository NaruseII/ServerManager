package fr.naruse.servermanager.filemanager;

import fr.naruse.servermanager.core.Utils;
import fr.naruse.servermanager.core.config.Configuration;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AutoScaler {

    private static final ServerManagerLogger.Logger LOGGER = new ServerManagerLogger.Logger("AutoScaler");

    private final FileManager fileManager;
    private final Set<Configuration.ConfigurationSection> sectionSet;

    public AutoScaler(FileManager fileManager, Set<Configuration.ConfigurationSection> sectionSet) {
        this.fileManager = fileManager;
        this.sectionSet = sectionSet;
        LOGGER.info("Started");
    }

    public void scale(){
        Set<Server> set = ServerList.getAll();

        for (Configuration.ConfigurationSection section : sectionSet) {
            Matches matches = Matches.valueOf(section.get("match"));
            Object value = matches.transformValue(section.get("value"));
            if(value == null){
                continue;
            }

            if(matches.match(set, value)){
                for (int i = 0; i < Utils.getIntegerFromPacket(section.get("startServers")); i++) {
                    fileManager.createServer(section.getInitialPath());
                }
            }
        }
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

        private static final Matches WHEN_ALL_ON_STATUS = registerMatches("WHEN_ALL_ON_STATUS", new Matches<String, Server.Status>() {
            @Override
            public boolean match(Set<Server> set, Server.Status status) {
                for (Server server : set) {
                    if(!server.getData().hasStatus(status)){
                        return false;
                    }
                }
                return true;
            }

            @Override
            public Server.Status transformValue(String value) {
                return Server.Status.valueOf(value);
            }
        });

        private static final Matches WHEN_TOTAL_SIZE_IS_ABOVE = registerMatches("WHEN_TOTAL_SIZE_IS_ABOVE", new Matches<Integer, Integer>() {

            @Override
            public boolean match(Set<Server> set, Integer value) {
                int totalSize = 0;
                for (Server server : set) {
                    totalSize += server.getData().getPlayerSize();
                }
                return totalSize > value;
            }

            @Override
            public Integer transformValue(Integer value) {
                return value;
            }
        });

        private static final Matches WHEN_ANY_SIZE_IS_ABOVE = registerMatches("WHEN_ANY_SIZE_IS_ABOVE", new Matches<Integer, Integer>() {

            @Override
            public boolean match(Set<Server> set, Integer value) {
                for (Server server : set) {
                    if(server.getData().getPlayerSize() > value){
                        return true;
                    }
                }
                return false;
            }

            @Override
            public Integer transformValue(Integer value) {
                return value;
            }
        });

        private static final Matches WHEN_ALL_FULL = registerMatches("WHEN_ALL_FULL", new Matches<String, String>() {

            @Override
            public boolean match(Set<Server> set, String value) {
                for (Server server : set) {
                    if(server.getData().getPlayerSize() < server.getData().getCapacity()){
                        return false;
                    }
                }
                return true;
            }

            @Override
            public String transformValue(String value) {
                return value;
            }
        });

        public abstract boolean match(Set<Server> set, E value) ;

        public abstract E transformValue(T value) ;

        private Matches() {
        }
    }
}
