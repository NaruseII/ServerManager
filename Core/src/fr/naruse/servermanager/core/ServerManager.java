package fr.naruse.servermanager.core;

import com.diogonunes.jcolor.Attribute;
import fr.naruse.api.logging.GlobalLogger;
import fr.naruse.servermanager.core.api.events.*;
import fr.naruse.api.config.Configuration;
import fr.naruse.servermanager.core.config.ConfigurationManager;
import fr.naruse.servermanager.core.connection.ConnectionManager;
import fr.naruse.servermanager.core.connection.KeepAliveServerThread;
import fr.naruse.servermanager.core.connection.packet.*;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import fr.naruse.servermanager.core.utils.Utils;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class ServerManager {

    public static final String VERSION = "1.0.20";

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
    private boolean isShuttingDowned = false;

    public ServerManager(CoreData coreData) {
        this(coreData, null);
    }

    public ServerManager(CoreData coreData, IServerManagerPlugin plugin) {
        GlobalLogger.info(Attribute.GREEN_TEXT(), "Initialising ServerManager Core on '"+coreData.getCoreServerType().name()+"'...");
        if(plugin == null){
            plugin = new BasicServerManagerPlugin(this.eventListenerSet);
        }

        plugin.callEvent(new InitializationStartEvent());

        instance = this;
        this.primaryThread = Thread.currentThread();
        this.coreData = coreData;
        this.plugin = plugin;

        this.configurationManager = new ConfigurationManager(this);
        GlobalLogger.loadConfigData(this.configurationManager.getConfig());
        if(coreData.getServerName() == null){
            coreData.setServerName(this.configurationManager.getConfig().get("currentServerName"));
            GlobalLogger.info("Server name is '"+coreData.getServerName()+"'");
        }
        Configuration.ConfigurationSection packetManagerSection = this.configurationManager.getConfig().getSection("packet-manager");
        coreData.setPacketManagerPort(packetManagerSection.getInt("serverPort"));
        coreData.setPacketManagerHost(packetManagerSection.get("serverAddress"));
        coreData.setCurrentAddress(Utils.getCurrentAddress());

        this.server = new Server(coreData.getServerName(), coreData.getPort(), coreData.getCurrentAddress(), coreData.getServerManagerPort(), coreData.getCoreServerType());

        Packets.load();

        this.connectionManager = new ConnectionManager(this);

        KeepAliveServerThread.launch(this);

        plugin.callEvent(new InitializationEndedEvent());
        GlobalLogger.info(Attribute.GREEN_TEXT(), "ServerManager Core initialised");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> this.preShutdown()));
    }

    public void preShutdown(){
        if(this.isShuttingDowned){
            return;
        }
        this.shutdown();
    }

    public void shutdown(){
        this.isShuttingDowned = true;
        this.plugin.callEvent(new ShutdownEvent());

        GlobalLogger.info("Shutting down...");
        KeepAliveServerThread.shutdown();
        this.configurationManager.shutdown();
        this.connectionManager.shutdown();
        GlobalLogger.info(Attribute.MAGENTA_TEXT(), "Server stopped. See you soon!");
        GlobalLogger.saveLogs(new File(this.coreData.getDataFolder(), "lastest.log"));
    }

    public String generateNewSecretKey(){
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < Utils.RANDOM.nextInt(100)+10; i++) {
            builder.append(Utils.randomLetters(4));
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
        if(packet instanceof PacketExecuteConsoleCommand){
            this.processPacketListenerSet.forEach(processPacketListener -> processPacketListener.processExecuteConsoleCommand((PacketExecuteConsoleCommand) packet));
        }else if(packet instanceof PacketSwitchServer){
            this.processPacketListenerSet.forEach(processPacketListener -> processPacketListener.processSwitchServer((PacketSwitchServer) packet));
        }else if(packet instanceof PacketBroadcast){
            this.processPacketListenerSet.forEach(processPacketListener -> processPacketListener.processBroadcast((PacketBroadcast) packet));
        }else if(packet instanceof PacketTeleportToLocation){
            this.processPacketListenerSet.forEach(processPacketListener -> processPacketListener.processTeleportToLocation((PacketTeleportToLocation) packet));
        }else if(packet instanceof PacketTeleportToPlayer){
            this.processPacketListenerSet.forEach(processPacketListener -> processPacketListener.processTeleportToPlayer((PacketTeleportToPlayer) packet));
        }else if(packet instanceof PacketKickPlayer){
            this.processPacketListenerSet.forEach(processPacketListener -> processPacketListener.processKickPlayer((PacketKickPlayer) packet));
        }else if(packet instanceof PacketSendTemplate){
            this.processPacketListenerSet.forEach(processPacketListener -> processPacketListener.processSendTemplate((PacketSendTemplate) packet));
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

    public boolean isShuttingDowned() {
        return isShuttingDowned;
    }

    public void printStatus() {
        GlobalLogger.info(Attribute.CYAN_TEXT(), "Server list:");
        for (Server server : ServerList.getAll()) {
            GlobalLogger.info("");
            GlobalLogger.info(" -> "+server.getName()+" ["+server.getCoreServerType()+"]");
            GlobalLogger.info("    Port: "+server.getPort());
            GlobalLogger.info("    ServerManagerPort: "+server.getServerManagerPort());
            GlobalLogger.info("    Capacity: "+server.getData().getCapacity());
            GlobalLogger.info("    PlayerSize: "+server.getData().getPlayerSize());
            GlobalLogger.info("    Players: "+server.getData().getUUIDByNameMap().toString());
            GlobalLogger.info("    Status: "+server.getData().getStatusSet().toString());
        }
    }
}
