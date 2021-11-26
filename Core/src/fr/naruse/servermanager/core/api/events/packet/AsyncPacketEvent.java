package fr.naruse.servermanager.core.api.events.packet;

import fr.naruse.servermanager.core.api.events.IEvent;
import fr.naruse.servermanager.core.connection.packet.IPacket;

public class AsyncPacketEvent implements IEvent {

    private final IPacket packet;
    private final String packetName;

    public AsyncPacketEvent(IPacket packet, String packetName) {
        this.packet = packet;
        this.packetName = packetName;
    }

    public String getPacketName() {
        return packetName;
    }

    public IPacket getPacket() {
        return packet;
    }
}
