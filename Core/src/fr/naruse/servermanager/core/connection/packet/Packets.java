package fr.naruse.servermanager.core.connection.packet;

import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.api.events.packet.PacketLoadEvent;
import fr.naruse.api.logging.GlobalLogger;
import fr.naruse.servermanager.core.utils.SimpleImmutableMap;

public class Packets {

    private static final GlobalLogger.Logger LOGGER = new GlobalLogger.Logger("PacketManager");

    private static final SimpleImmutableMap<String, Class<? extends IPacket>> packetByName = new SimpleImmutableMap<>();
    private static final SimpleImmutableMap<Class<? extends IPacket>, String> nameByPacket = new SimpleImmutableMap<>();

    public static void load(){
        LOGGER.info("Loading packets...");

        registerPacket("CONNECTION", PacketConnection.class);
        registerPacket("DISCONNECTION", PacketDisconnection.class);
        registerPacket("KEEP_ALIVE", PacketKeepAlive.class);
        registerPacket("CREATE_SERVER", PacketCreateServer.class);
        registerPacket("SHUTDOWN", PacketShutdown.class);
        registerPacket("SERVER_LIST", PacketServerList.class);
        registerPacket("EXECUTE_CONSOLE_COMMAND", PacketExecuteConsoleCommand.class);
        registerPacket("SWITCH_SERVER", PacketSwitchServer.class);
        registerPacket("BROADCAST", PacketBroadcast.class);
        registerPacket("TELEPORT_TO_LOCATION", PacketTeleportToLocation.class);
        registerPacket("TELEPORT_TO_PLAYER", PacketTeleportToPlayer.class);
        registerPacket("KICK_PLAYER", PacketKickPlayer.class);
        registerPacket("DATABASE_UPDATE", PacketDatabase.Update.class);
        registerPacket("DATABASE_DESTROY", PacketDatabase.Destroy.class);
        registerPacket("DATABASE_UPDATE_CACHE", PacketDatabase.UpdateCache.class);
        registerPacket("SEND_TEMPLATE", PacketSendTemplate.class);
        registerPacket("FILE_MANAGER_REQUEST_NEW_NAME", PacketFileManagerRequest.NewName.class);
        registerPacket("FILE_MANAGER_REQUEST_NEW_NAME_RESPONSE", PacketFileManagerRequest.NewNameResponse.class);
        registerPacket("ADD_STATUS", PacketAddStatus.class);
        registerPacket("CREATE_TEMPLATE", PacketCreateTemplate.class);
        registerPacket("SAVE_SERVER", PacketSaverServer.class);

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
