package fr.naruse.servermanager.core;

import java.io.File;

public class CoreData {

    private final CoreServerType coreServerType;
    private final File dataFolder;
    private String packetManagerHost;
    private int packetManagerPort;
    private int serverManagerPort = 0;
    private String serverName;
    private int port;
    private String currentAddress;

    public CoreData(CoreServerType coreServerType, File dataFolder, String serverName, int port) {
        this.coreServerType = coreServerType;
        this.dataFolder = dataFolder;
        this.packetManagerHost = "localhost";
        this.packetManagerPort = 4848;
        this.serverName = serverName;
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public int getServerManagerPort() {
        return serverManagerPort;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setPacketManagerPort(int packetManagerPort) {
        this.packetManagerPort = packetManagerPort;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public CoreServerType getCoreServerType() {
        return coreServerType;
    }

    public File getDataFolder() {
        return dataFolder;
    }

    public int getPacketManagerPort() {
        return packetManagerPort;
    }

    public String getPacketManagerHost() {
        return packetManagerHost;
    }

    public void setPacketManagerHost(String packetManagerHost) {
        this.packetManagerHost = packetManagerHost;
    }

    public String getCurrentAddress() {
        return currentAddress;
    }

    public void setCurrentAddress(String currentAddress) {
        this.currentAddress = currentAddress;
    }
}