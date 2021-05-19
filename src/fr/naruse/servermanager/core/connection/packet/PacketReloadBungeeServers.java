package fr.naruse.servermanager.core.connection.packet;

import fr.naruse.servermanager.core.ServerManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketReloadBungeeServers implements IPacket{

    public PacketReloadBungeeServers() {
    }

    @Override
    public void write(DataOutputStream stream) throws IOException {

    }

    @Override
    public void read(DataInputStream stream) throws IOException {

    }

    @Override
    public void process(ServerManager serverManager) {
        serverManager.processPacket(this);
    }
}
