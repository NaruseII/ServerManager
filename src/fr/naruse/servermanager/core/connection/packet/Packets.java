package fr.naruse.servermanager.core.connection.packet;

import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.api.events.packet.PacketLoadEvent;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;

import java.util.HashMap;
import java.util.Map;

public class Packets {

    private static final ServerManagerLogger.Logger LOGGER = new ServerManagerLogger.Logger("PacketManager");

    private static final Map<String, Class<? extends IPacket>> packetByName = new HashMap<>();
    private static final Map<Class<? extends IPacket>, String> nameByPacket = new HashMap<>();

    public static void load(){
        LOGGER.info("Loading packets...");

        registerPacket("CONNECTION", PacketConnection.class);
        registerPacket("DISCONNECTION", PacketDisconnection.class);
        registerPacket("KEEP_ALIVE", PacketKeepAlive.class);
        registerPacket("CREATE_SERVER", PacketCreateServer.class);
        registerPacket("SHUTDOWN", PacketShutdown.class);
        registerPacket("SERVER_LIST", PacketServerList.class);
        registerPacket("RELOAD_BUNGEE_SERVERS", PacketReloadBungeeServers.class);
        registerPacket("BUNGEE_REQUEST_CONFIG_WRITE", PacketBungeeRequestConfigWrite.class);
        registerPacket("EXECUTE_CONSOLE_COMMAND", PacketExecuteConsoleCommand.class);
        registerPacket("SWITCH_SERVER", PacketSwitchServer.class);
        registerPacket("BROADCAST", PacketBroadcast.class);

        ServerManager.get().getPlugin().callEvent(new PacketLoadEvent(packetByName, nameByPacket));

        LOGGER.info(packetByName.size()+" Packets loaded");
    }

    public static void registerPacket(String name, Class<? extends IPacket> clazz){
        packetByName.put(name, clazz);
        nameByPacket.put(clazz, name);
    }

    public static IPacket buildPacket(String packetName) throws Exception {
        Class<? extends IPacket> clazz = packetByName.get(packetName);
        return clazz.getConstructor().newInstance();
    }

    public static String getNameByPacket(Class<? extends IPacket> clazz) throws Exception {
        return nameByPacket.get(clazz);
    }

}
