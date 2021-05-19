package fr.naruse.servermanager.core.api.events.packet;

import fr.naruse.servermanager.core.api.events.ICancellableEvent;
import fr.naruse.servermanager.core.connection.packet.IPacket;

import java.net.InetAddress;

public class AsyncPacketSendEvent extends AsyncPacketEvent implements ICancellableEvent {

    private InetAddress destinationAddress;
    private int destinationPort;

    private boolean cancelled;

    public AsyncPacketSendEvent(IPacket packet, String packetName, InetAddress destinationAddress, int destinationPort) {
        super(packet, packetName);
        this.destinationAddress = destinationAddress;
        this.destinationPort = destinationPort;
    }

    public InetAddress getDestinationAddress() {
        return destinationAddress;
    }

    public int getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationAddress(InetAddress destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public void setDestinationPort(int destinationPort) {
        this.destinationPort = destinationPort;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }
}
