package fr.naruse.servermanager.core.api.events.packet;

import fr.naruse.servermanager.core.api.events.IEvent;
import fr.naruse.servermanager.core.connection.packet.IPacket;

import java.util.Map;

public class PacketLoadEvent implements IEvent {

    private final Map<String, Class<? extends IPacket>> packetByName;
    private final Map<Class<? extends IPacket>, String> nameByPacket;

    public PacketLoadEvent(Map<String, Class<? extends IPacket>> packetByName, Map<Class<? extends IPacket>, String> nameByPacket) {
        this.packetByName = packetByName;
        this.nameByPacket = nameByPacket;
    }

    public Map<Class<? extends IPacket>, String> getNameByPacket() {
        return nameByPacket;
    }

    public Map<String, Class<? extends IPacket>> getPacketByName() {
        return packetByName;
    }
}
