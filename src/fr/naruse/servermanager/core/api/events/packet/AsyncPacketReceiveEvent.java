package fr.naruse.servermanager.core.api.events.packet;

import fr.naruse.servermanager.core.api.events.ICancellableEvent;
import fr.naruse.servermanager.core.connection.packet.IPacket;

public class AsyncPacketReceiveEvent extends AsyncPacketEvent implements ICancellableEvent {

    private boolean cancelled;

    public AsyncPacketReceiveEvent(IPacket packet, String packetName) {
        super(packet, packetName);
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
