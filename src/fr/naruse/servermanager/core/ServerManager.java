package fr.naruse.servermanager.core;

import fr.naruse.servermanager.core.api.events.*;
import fr.naruse.servermanager.core.api.events.server.ServerRegisterEvent;
import fr.naruse.servermanager.core.config.ConfigurationManager;
import fr.naruse.servermanager.core.connection.ConnectionManager;
import fr.naruse.servermanager.core.connection.KeepAliveServerThread;
import fr.naruse.servermanager.core.connection.packet.*;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;
import fr.naruse.servermanager.core.server.Server;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class ServerManager {

    private static ServerManager instance;
    public static ServerManager get() {
        return instance;
    }

    private final CoreData coreData;
    private final ConfigurationManager configurationManager;
    private final ConnectionManager connectionManager;
    private final Thread primaryThread;
    private final IServerManagerPlugin plugin;

    private final Server server;
    private final Set<EventListener> eventListenerSet = new HashSet<>();
    private final Set<PacketProcessing> packetProcessingSet = new HashSet<>();
    private boolean isShutDowned = false;

    public ServerManager(CoreData coreData) {
        this(coreData, null);
    }

    public ServerManager(CoreData coreData, IServerManagerPlugin plugin) {
        ServerManagerLogger.info("Initialising ServerManager Core on '"+coreData.getCoreServerType().name()+"'...");
        if(plugin == null){
            plugin = new BasicServerManagerPlugin(this.eventListenerSet);
        }
        plugin.callEvent(new InitializationStartEvent());

        instance = this;
        this.primaryThread = Thread.currentThread();
        this.coreData = coreData;
        this.plugin = plugin;

        this.configurationManager = new ConfigurationManager(this);
        if(coreData.getServerName() == null){
            coreData.setServerName(configurationManager.getConfig().get("currentServerName"));
        }
        this.server = new Server(coreData.getServerName(), coreData.getPort(), coreData.getCoreServerType());
        Packets.load();
        this.connectionManager = new ConnectionManager(this);

        KeepAliveServerThread.launch(this);

        plugin.callEvent(new InitializationEndedEvent());
        ServerManagerLogger.info("ServerManager Core initialised");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> this.shutdown()));
    }

    public void shutdown(){
        if(this.isShutDowned){
            return;
        }
        this.isShutDowned = true;

        this.plugin.callEvent(new ShutdownEvent());

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

    public void registerEventListener(EventListener eventListener){
        this.eventListenerSet.add(eventListener);
    }

    public void registerPacketProcessing(PacketProcessing packetProcessing){
        this.packetProcessingSet.add(packetProcessing);
    }

    public void processPacket(IPacket packet){
        if(packet instanceof PacketReloadBungeeServers){
            this.packetProcessingSet.forEach(packetProcessing -> packetProcessing.processReloadBungeeServers());
        }else if(packet instanceof PacketBungeeRequestConfigWrite){
            this.packetProcessingSet.forEach(packetProcessing -> packetProcessing.processBungeeRequestConfigWrite((PacketBungeeRequestConfigWrite) packet));
        }
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

    public IServerManagerPlugin getPlugin() {
        return plugin;
    }
}
