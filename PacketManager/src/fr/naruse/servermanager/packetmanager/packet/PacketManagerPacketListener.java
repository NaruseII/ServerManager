package fr.naruse.servermanager.packetmanager.packet;

import fr.naruse.servermanager.core.command.AbstractCoreCommand;
import fr.naruse.servermanager.core.connection.packet.PacketExecuteConsoleCommand;
import fr.naruse.servermanager.core.connection.packet.ProcessPacketListener;

public class PacketManagerPacketListener extends ProcessPacketListener {

    @Override
    public void processExecuteConsoleCommand(PacketExecuteConsoleCommand packet) {
        AbstractCoreCommand.get().execute(packet.getCommand());
    }
}
