package fr.naruse.servermanager.core.connection.packet;

import fr.naruse.servermanager.core.ServerManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketExecuteConsoleCommand implements IPacket {

    public PacketExecuteConsoleCommand() {
    }

    private String command;
    public PacketExecuteConsoleCommand(String command) {
        this.command = command;
    }

    @Override
    public void write(DataOutputStream stream) throws IOException {
        stream.writeUTF(this.command);
    }

    @Override
    public void read(DataInputStream stream) throws IOException {
        this.command = stream.readUTF();
    }

    @Override
    public void process(ServerManager serverManager) {
        serverManager.processPacket(this);
    }

    public String getCommand() {
        return command;
    }
}
