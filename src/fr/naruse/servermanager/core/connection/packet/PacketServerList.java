package fr.naruse.servermanager.core.connection.packet;

import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class PacketServerList implements IPacket {

    public PacketServerList() {
    }

    private Set<Server> set;
    public PacketServerList(Set<Server> set) {
        this.set = set;
    }

    @Override
    public void write(DataOutputStream stream) throws IOException {
        stream.writeInt(this.set.size());
        for (Server server : this.set) {
            PacketKeepAlive packetKeepAlive = new PacketKeepAlive(server);
            packetKeepAlive.write(stream);
        }
    }

    @Override
    public void read(DataInputStream stream) throws IOException {
        this.set = new HashSet<>();
        int size = stream.readInt();
        for (int i = 0; i < size; i++) {
            PacketKeepAlive packetKeepAlive = new PacketKeepAlive();
            packetKeepAlive.read(stream);
            Server server = ServerList.getByName(packetKeepAlive.getName());
            if(server == null){
                server = ServerList.createNewServer(packetKeepAlive.getName(), packetKeepAlive.getPort(), packetKeepAlive.getCoreServerType());
            }

            server.getData().setCapacity(packetKeepAlive.getCapacity());
            server.getData().setUUIDByNameMap(packetKeepAlive.getUUIDByNameMap());
            server.getData().setDataMap(packetKeepAlive.getDataMap());

            this.set.add(server);
        }
    }

    @Override
    public void process(ServerManager serverManager) {
        ServerList.getAll().stream().filter(server -> !set.contains(server)).forEach(server -> {
            ServerList.deleteServer(server.getName(), server.getPort());
        });
    }
}
