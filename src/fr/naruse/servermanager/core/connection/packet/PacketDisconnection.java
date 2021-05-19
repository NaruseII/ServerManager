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

    private int port;
    private String name;
    public PacketDisconnection(Server server) {
        this.port = server.getPort();
        this.name = server.getName();
    }

    @Override
    public void write(DataOutputStream stream) throws IOException {
        stream.writeInt(this.port);
        stream.writeUTF(this.name);
    }

    @Override
    public void read(DataInputStream stream) throws IOException {
        this.port = stream.readInt();
        this.name = stream.readUTF();
    }

    @Override
    public void process(ServerManager serverManager) {
        ServerList.deleteServer(this.name, this.port);
    }

}
