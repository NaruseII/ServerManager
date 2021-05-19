package fr.naruse.servermanager.core.server;

import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.connection.packet.IPacket;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Server {

    private final Data data = new Data();
    private final ServerManager serverManager;
    private final String name;
    private final CoreServerType coreServerType;
    private int port;

    public Server(String name, int port, CoreServerType coreServerType) {
        this.serverManager = ServerManager.get();
        this.name = name;
        this.port = port;
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

    public Data getData() {
        return data;
    }

    public void sendPacket(IPacket packet){
        this.serverManager.getConnectionManager().sendPacket(this, packet);
    }

    public InetAddress getAddress() {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class Data {

        private Map<String, Object> dataMap = new HashMap<>();
        private Map<String, String> uuidByNameMap = new HashMap<>(); // Name -> UUID
        private int capacity;

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
            String uuid = this.uuidByNameMap.get(name);
            if(uuid == null){
                return null;
            }
            return UUID.fromString(uuid);
        }

        public Map<String, String> getUUIDByNameMap() {
            return uuidByNameMap;
        }

        public void setUUIDByNameMap(Map<String, String> uuidByNameMap) {
            this.uuidByNameMap = uuidByNameMap;
        }

        public Map<String, Object> getDataMap() {
            return dataMap;
        }

        public void setDataMap(Map<String, Object> dataMap) {
            this.dataMap = dataMap;
        }

    }
}
