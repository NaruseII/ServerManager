package fr.naruse.servermanager.core.server;

import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.connection.packet.IPacket;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;
import fr.naruse.servermanager.core.utils.MultiMap;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class Server {

    private final Data data = new Data();
    private final ServerManager serverManager;
    private final String name;
    private final CoreServerType coreServerType;
    private int serverManagerPort;
    private int port;

    public Server(String name, int port, int serverManagerPort, CoreServerType coreServerType) {
        this.serverManager = ServerManager.get();
        this.name = name;
        this.port = port;
        this.serverManagerPort = serverManagerPort;
        this.coreServerType = coreServerType;
    }

    public String getName() {
        return name;
    }

    public CoreServerType getCoreServerType() {
        return coreServerType;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setServerManagerPort(int serverManagerPort) {
        this.serverManagerPort = serverManagerPort;
    }

    public int getServerManagerPort() {
        return serverManagerPort;
    }

    public Data getData() {
        return data;
    }

    public void sendPacket(IPacket packet){
        if(port == serverManagerPort){
            this.serverManager.getConnectionManager().sendPacket(packet);
        }else{
            this.serverManager.getConnectionManager().sendPacket(this, packet);
        }
    }

    public InetAddress getAddress() {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Server)){
            return false;
        }
        return this.name.equals(((Server) o).getName());
    }

    @Override
    public String toString() {
        return "Server{" +
                "data=" + data +
                ", serverManager=" + serverManager +
                ", name='" + name + '\'' +
                ", coreServerType=" + coreServerType +
                ", serverManagerPort=" + serverManagerPort +
                ", port=" + port +
                '}';
    }

    public static class Data {

        private Map<String, Object> dataMap = new HashMap<>();
        private MultiMap<String, UUID> uuidByNameMap = new MultiMap<>(); // Name -> UUID
        private int capacity;
        private Set<Status> statusSet = new HashSet<>();
        private int countBeforeDelete = 3;

        public Data() {
            this.statusSet.add(Status.READY);
        }

        public <T> T get(String dataName){
            return (T) this.dataMap.get(dataName);
        }

        public void set(String dataName, Object o){
            this.dataMap.put(dataName, o);
        }

        public int getCapacity() {
            return capacity;
        }

        public int getPlayerSize() {
            return this.uuidByNameMap.size();
        }

        public void setCapacity(int capacity) {
            this.capacity = capacity;
        }

        public UUID getByName(String name){
            return this.uuidByNameMap.get(name);
        }

        public String getByUUID(UUID uuid){
            return this.uuidByNameMap.reverse().get(uuid);
        }

        public boolean containsPlayer(String name){
            return this.getByName(name) != null;
        }

        public boolean containsPlayer(UUID uuid){
            return this.getByUUID(uuid) != null;
        }

        public MultiMap<String, UUID> getUUIDByNameMap() {
            return this.uuidByNameMap;
        }

        public void setUUIDByNameMap(MultiMap<String, UUID> uuidByNameMap) {
            this.uuidByNameMap = uuidByNameMap;
        }

        public Map<String, Object> getDataMap() {
            return dataMap;
        }

        public void setDataMap(Map<String, Object> dataMap) {
            this.dataMap = dataMap;
        }

        public Set<Status> getStatusSet() {
            return statusSet;
        }

        public void setStatusSet(Set<Status> statusSet) {
            this.statusSet = statusSet;
        }

        public boolean hasStatus(Status... status){
            for (Status s : this.statusSet) {
                if(s.is(status)){
                    return true;
                }
            }
            return false;
        }

        public void addStatus(Status status){
            if(this.statusSet.contains(status)){
                return;
            }
            this.statusSet.add(status);
        }

        public void removeStatus(Status status){
            this.statusSet.remove(status);
        }

        public int getCountBeforeDelete() {
            return countBeforeDelete;
        }

        public void setCountBeforeDelete(int countBeforeDelete) {
            this.countBeforeDelete = countBeforeDelete;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "dataMap=" + dataMap +
                    ", uuidByNameMap=" + uuidByNameMap +
                    ", capacity=" + capacity +
                    ", statusSet=" + statusSet +
                    '}';
        }
    }

    public static class Status {

        private static final ServerManagerLogger.Logger LOGGER = new ServerManagerLogger.Logger("Server Status");
        private static final Map<String, Status> statusMap = new HashMap<>();

        public static final Status READY = registerNewStatus("ready");
        public static final Status ALLOCATED = registerNewStatus("allocated");

        public static Status registerNewStatus(String name){
            name = name.toUpperCase();
            if(statusMap.containsKey(name)){
                return valueOf(name);
            }
            Status status = new Status(name);
            statusMap.put(name, status);
            return status;
        }

        public static Status valueOf(String name){
            return statusMap.get(name.toUpperCase());
        }

        public static Set<Status> getAll(){
            return new HashSet<>(statusMap.values());
        }


        private final String statusName;

        private Status(String statusName) {
            this.statusName = statusName;
        }

        public boolean is(Status... status){
            for (Status s : status) {
                if(this == s){
                    return true;
                }
            }
            return false;
        }

        public String name() {
            return this.statusName;
        }

        @Override
        public String toString() {
            return this.statusName;
        }
    }
}
