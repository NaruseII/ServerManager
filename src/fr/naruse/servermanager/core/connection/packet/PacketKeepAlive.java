package fr.naruse.servermanager.core.connection.packet;

import fr.naruse.servermanager.core.*;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.packetmanager.KeepAliveBuffer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

public class PacketKeepAlive implements IPacket {

    public PacketKeepAlive() {
    }

    private String name;
    private int port;
    private CoreServerType coreServerType;
    private int capacity;
    private Map<String, String> uuidByNameMap;
    private Map<String, Object> dataMap;

    public PacketKeepAlive(Server server) {
        this.name = server.getName();
        this.port = server.getPort();
        this.coreServerType = server.getCoreServerType();
        this.capacity = server.getData().getCapacity();
        this.uuidByNameMap = server.getData().getUUIDByNameMap();
        this.dataMap = server.getData().getDataMap();
    }

    @Override
    public void write(DataOutputStream stream) throws IOException {
        stream.writeInt(this.port);
        stream.writeUTF(this.name);
        stream.writeUTF(this.coreServerType.name());
        stream.writeInt(this.capacity);
        stream.writeUTF(Utils.GSON.toJson(this.uuidByNameMap));
        stream.writeUTF(Utils.GSON.toJson(this.dataMap));
    }

    @Override
    public void read(DataInputStream stream) throws IOException {
        this.port = stream.readInt();
        this.name = stream.readUTF();
        this.coreServerType = CoreServerType.valueOf(stream.readUTF());
        this.capacity = stream.readInt();
        this.uuidByNameMap = Utils.GSON.fromJson(stream.readUTF(), Utils.MAP_STRING_TYPE);
        this.dataMap = Utils.GSON.fromJson(stream.readUTF(), Utils.MAP_TYPE);
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

    public int getCapacity() {
        return capacity;
    }

    public Map<String, String> getUUIDByNameMap() {
        return uuidByNameMap;
    }

    public Map<String, Object> getDataMap() {
        return dataMap;
    }
}
