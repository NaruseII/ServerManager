package fr.naruse.servermanager.core.connection.packet;

import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PacketSwitchServer implements IPacket {

    public PacketSwitchServer() { }

    private UUID[] uuids;
    private Server server;
    public PacketSwitchServer(Server server, UUID... uuids) {
        this.server = server;
        this.uuids = uuids;
    }

    @Override
    public void write(DataOutputStream stream) throws IOException {
        stream.writeUTF(this.server.getName());
        stream.writeInt(this.uuids.length);
        for (int i = 0; i < this.uuids.length; i++) {
            stream.writeUTF(this.uuids[i].toString());
        }
    }

    @Override
    public void read(DataInputStream stream) throws IOException {
        Set<UUID> set = new HashSet<>();

        this.server = ServerList.getByName(stream.readUTF());
        int size = stream.readInt();
        for (int i = 0; i < size; i++) {
            set.add(UUID.fromString(stream.readUTF()));
        }
    }

    @Override
    public void process(ServerManager serverManager) {
        serverManager.processPacket(this);
    }

    public UUID[] getUUIDs() {
        return uuids;
    }

    public Server getServer() {
        return server;
    }
}
