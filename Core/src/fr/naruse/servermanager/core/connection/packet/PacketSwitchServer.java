package fr.naruse.servermanager.core.connection.packet;

import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class PacketSwitchServer implements IPacket {

    public PacketSwitchServer() { }

    private String[] names;
    private Server server;
    public PacketSwitchServer(Server server, String... names) {
        this.server = server;
        this.names = names;
    }

    @Override
    public void write(DataOutputStream stream) throws IOException {
        stream.writeUTF(this.server.getName());
        stream.writeInt(this.names.length);
        for (int i = 0; i < this.names.length; i++) {
            stream.writeUTF(this.names[i]);
        }
    }

    @Override
    public void read(DataInputStream stream) throws IOException {
        Set<String> set = new HashSet<>();

        this.server = ServerList.getByName(stream.readUTF());
        int size = stream.readInt();
        for (int i = 0; i < size; i++) {
            set.add(stream.readUTF());
        }
        this.names = set.toArray(new String[0]);
    }

    @Override
    public void process(ServerManager serverManager) {

    }

    public String[] getNames() {
        return names;
    }

    public Server getServer() {
        return server;
    }
}
