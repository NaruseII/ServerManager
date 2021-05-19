package fr.naruse.servermanager.core.connection.packet;

import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.Server;
import fr.naruse.servermanager.core.ServerList;
import fr.naruse.servermanager.core.ServerManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketConnection implements IPacket {

    public PacketConnection() {
    }

    private String name;
    private int port;
    private CoreServerType coreServerType;
    public PacketConnection(Server server) {
        this.name = server.getName();
        this.port = server.getPort();
        this.coreServerType = server.getCoreServerType();
    }

    @Override
    public void write(DataOutputStream stream) throws IOException {
        stream.writeInt(this.port);
        stream.writeUTF(this.name);
        stream.writeUTF(this.coreServerType.name());
    }

    @Override
    public void read(DataInputStream stream) throws IOException {
        this.port = stream.readInt();
        this.name = stream.readUTF();
        this.coreServerType = CoreServerType.valueOf(stream.readUTF());
    }

    @Override
    public void process(ServerManager serverManager) {
        ServerList.createNewServer(this.name, this.port, this.coreServerType);
    }
}
