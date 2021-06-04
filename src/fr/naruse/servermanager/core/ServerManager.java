package fr.naruse.servermanager.core;

import fr.naruse.servermanager.core.api.events.*;
import fr.naruse.servermanager.core.config.ConfigurationManager;
import fr.naruse.servermanager.core.connection.ConnectionManager;
import fr.naruse.servermanager.core.connection.KeepAliveServerThread;
import fr.naruse.servermanager.core.connection.packet.*;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import fr.naruse.servermanager.core.utils.Utils;

import java.util.HashSet;
import java.util.Set;

public class ServerManager {

    public static final String VERSION = "1.0.0";

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
    private final Set<ProcessPacketListener> processPacketListenerSet = new HashSet<>();
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
        ServerManagerLogger.setDebug(configurationManager.getConfig().contains("debug") ? configurationManager.getConfig().get("debug") : false);
        if(coreData.getServerName() == null){
            coreData.setServerName(configurationManager.getConfig().get("currentServerName"));
        }
        coreData.setServerPort(Utils.getIntegerFromPacket(configurationManager.getConfig().get("serverPort")));


        this.server = new Server(coreData.getServerName(), coreData.getPort(), coreData.getServerManagerPort(), coreData.getCoreServerType());
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
        ServerManagerLogger.saveLogs();
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

    public void registerPacketProcessing(ProcessPacketListener processPacketListener){
        this.processPacketListenerSet.add(processPacketListener);
    }

    public void processPacket(IPacket packet){
        this.processPacketListenerSet.forEach(processPacketListener -> processPacketListener.processAllPackets(packet));
        if(packet instanceof PacketReloadProxyServers){
            this.processPacketListenerSet.forEach(processPacketListener -> processPacketListener.processReloadProxyServers((PacketReloadProxyServers) packet));
        }else if(packet instanceof PacketProxyRequestConfigWrite){
            this.processPacketListenerSet.forEach(processPacketListener -> processPacketListener.processProxyRequestConfigWrite((PacketProxyRequestConfigWrite) packet));
        }else if(packet instanceof PacketExecuteConsoleCommand){
            this.processPacketListenerSet.forEach(processPacketListener -> processPacketListener.processExecuteConsoleCommand((PacketExecuteConsoleCommand) packet));
        }else if(packet instanceof PacketSwitchServer){
            this.processPacketListenerSet.forEach(processPacketListener -> processPacketListener.processSwitchServer((PacketSwitchServer) packet));
        }else if(packet instanceof PacketBroadcast){
            this.processPacketListenerSet.forEach(processPacketListener -> processPacketListener.processBroadcast((PacketBroadcast) packet));
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

    public boolean isShutDowned() {
        return isShutDowned;
    }

    public void printStatus() {
        ServerManagerLogger.info("Server list:");
        for (Server server : ServerList.getAll()) {
            ServerManagerLogger.info("");
            ServerManagerLogger.info(" -> "+server.getName()+" ["+server.getCoreServerType()+"]");
            ServerManagerLogger.info("    Port: "+server.getPort());
            ServerManagerLogger.info("    ServerManagerPort: "+server.getServerManagerPort());
            ServerManagerLogger.info("    Capacity: "+server.getData().getCapacity());
            ServerManagerLogger.info("    PlayerSize: "+server.getData().getPlayerSize());
            ServerManagerLogger.info("    Players: "+server.getData().getUUIDByNameMap().toString());
            ServerManagerLogger.info("    Status: "+server.getData().getStatusSet().toString());
        }
    }
}
