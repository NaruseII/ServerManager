package fr.naruse.servermanager.core.connection.packet;

import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import fr.naruse.servermanager.core.ServerManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketDisconnection implements IPacket {

    public PacketDisconnection() {
    }

    private String name;
    public PacketDisconnection(Server server) {
        this.name = server.getName();
    }

    @Override
    public void write(DataOutputStream stream) throws IOException {
        stream.writeUTF(this.name);
    }

    @Override
    public void read(DataInputStream stream) throws IOException {
        this.name = stream.readUTF();
    }

    @Override
    public void process(ServerManager serverManager) {
        ServerList.deleteServer(this.name);
    }

}
