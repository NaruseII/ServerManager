package fr.naruse.servermanager.core;

import fr.naruse.servermanager.core.config.ConfigurationManager;
import fr.naruse.servermanager.core.connection.ConnectionManager;
import fr.naruse.servermanager.core.connection.KeepAliveServerThread;
import fr.naruse.servermanager.core.connection.packet.Packets;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;

import java.util.Random;

public class ServerManager {

    private static ServerManager instance;
    public static ServerManager get() {
        return instance;
    }

    private final CoreData coreData;
    private final ConfigurationManager configurationManager;
    private final ConnectionManager connectionManager;
    private final Thread primaryThread;

    private final Server server;
    private boolean isShutDowned = false;

    public ServerManager(CoreData coreData) {
        ServerManagerLogger.info("Initialising ServerManager Core on '"+coreData.getCoreServerType().name()+"'...");
        instance = this;
        this.primaryThread = Thread.currentThread();

        this.coreData = coreData;
        this.configurationManager = new ConfigurationManager(this);
        this.server = new Server(coreData.getServerName(), coreData.getPort(), coreData.getCoreServerType());
        Packets.load();
        this.connectionManager = new ConnectionManager(this);

        KeepAliveServerThread.launch(this);

        ServerManagerLogger.info("ServerManager Core initialised");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> this.shutdown()));
    }

    public void shutdown(){
        if(this.isShutDowned){
            return;
        }
        this.isShutDowned = true;
        ServerManagerLogger.info("Shutting down...");
        KeepAliveServerThread.shutdown();
        this.configurationManager.shutdown();
        this.connectionManager.shutdown();
        ServerManagerLogger.info("Server stopped. See you soon !");
    }

    public String generateNewSecretKey(){
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < Utils.RANDOM.nextInt(200)+100; i++) {
            builder.append(Utils.randomLetters());
        }

        String finalString = builder.toString();
        this.configurationManager.getConfig().set("key", finalString);
        this.configurationManager.getConfig().save();
        return finalString;
    }

    public boolean isPrimaryThread(){
        return Thread.currentThread() == this.primaryThread;
    }

    public CoreData getCoreData() {
        return coreData;
    }

    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public Server getCurrentServer() {
        return server;
    }
}
