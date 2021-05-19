package fr.naruse.servermanager.core.connection.packet;

import fr.naruse.servermanager.core.*;
import fr.naruse.servermanager.packetmanager.KeepAliveBuffer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class PacketKeepAlive implements IPacket {

    public PacketKeepAlive() {
    }

    private String name;
    private int port;
    private CoreServerType coreServerType;
    private int capacity;
    private int playerSize;
    private Map<String, String> uuidByNameMap;

    public PacketKeepAlive(Server server) {
        this.name = server.getName();
        this.port = server.getPort();
        this.coreServerType = server.getCoreServerType();
        this.capacity = server.getData().getCapacity();
        this.playerSize = server.getData().getPlayerSize();
        this.uuidByNameMap = server.getData().getUUIDByNameMap();
    }

    @Override
    public void write(DataOutputStream stream) throws IOException {
        stream.writeInt(this.port);
        stream.writeUTF(this.name);
        stream.writeUTF(this.coreServerType.name());
        stream.writeInt(this.capacity);
        stream.writeInt(this.playerSize);
        stream.writeUTF(Utils.GSON.toJson(this.uuidByNameMap));
    }

    @Override
    public void read(DataInputStream stream) throws IOException {
        this.port = stream.readInt();
        this.name = stream.readUTF();
        this.coreServerType = CoreServerType.valueOf(stream.readUTF());
        this.capacity = stream.readInt();
        this.playerSize = stream.readInt();
        this.uuidByNameMap = Utils.GSON.fromJson(stream.readUTF(), Utils.MAP_STRING_TYPE);
    }

    @Override
    public void process(ServerManager serverManager) {
        KeepAliveBuffer.put(this);
    }

    public int getPort() {
        return port;
    }

    public String getName() {
        return name;
    }

    public CoreServerType getCoreServerType() {
        return coreServerType;
    }

    public int getPlayerSize() {
        return playerSize;
    }

    public int getCapacity() {
        return capacity;
    }

    public Map<String, String> getUUIDByNameMap() {
        return uuidByNameMap;
    }
}
