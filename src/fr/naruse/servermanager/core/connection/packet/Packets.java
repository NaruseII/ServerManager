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

        addPacket("CONNECTION", PacketConnection.class);
        addPacket("DISCONNECTION", PacketDisconnection.class);
        addPacket("KEEP_ALIVE", PacketKeepAlive.class);
        addPacket("CREATE_SERVER", PacketCreateServer.class);
        addPacket("SHUTDOWN", PacketShutdown.class);
        addPacket("SERVER_LIST", PacketServerList.class);
        addPacket("RELOAD_BUNGEE_SERVERS", PacketReloadBungeeServers.class);
        addPacket("BUNGEE_REQUEST_CONFIG_WRITE", PacketBungeeRequestConfigWrite.class);

        ServerManager.get().getPlugin().callEvent(new PacketLoadEvent(packetByName, nameByPacket));

        LOGGER.info(packetByName.size()+" Packets loaded");
    }

    public static void addPacket(String name, Class<? extends IPacket> clazz){
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
