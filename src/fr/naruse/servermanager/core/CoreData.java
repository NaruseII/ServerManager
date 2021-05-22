package fr.naruse.servermanager.core;

import java.io.File;

public class CoreData {

    private final CoreServerType coreServerType;
    private final File dataFolder;
    private int serverPort;
    private int serverManagerPort = 0;
    private String serverName;
    private int port;

    public CoreData(CoreServerType coreServerType, File dataFolder, int serverPort, String serverName, int port) {
        this.coreServerType = coreServerType;
        this.dataFolder = dataFolder;
        this.serverPort = serverPort;
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

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
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

    public int getServerPort() {
        return serverPort;
    }
}