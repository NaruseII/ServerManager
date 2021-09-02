package fr.naruse.servermanager.filemanager.packet;

import fr.naruse.servermanager.core.command.AbstractCoreCommand;
import fr.naruse.servermanager.core.connection.packet.PacketExecuteConsoleCommand;
import fr.naruse.servermanager.core.connection.packet.ProcessPacketListener;

public class FileManagerPacketListener extends ProcessPacketListener {

    @Override
    public void processExecuteConsoleCommand(PacketExecuteConsoleCommand packet) {
        AbstractCoreCommand.get().execute(packet.getCommand());
    }
}
