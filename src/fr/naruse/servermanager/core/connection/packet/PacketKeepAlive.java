package fr.naruse.servermanager.core.connection.packet;

import fr.naruse.servermanager.core.*;
import fr.naruse.servermanager.core.server.MultiMap;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.utils.Utils;
import fr.naruse.servermanager.packetmanager.KeepAliveBuffer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class PacketKeepAlive implements IPacket {

    public PacketKeepAlive() {
    }

    private String name;
    private int port;
    private int serverManagerPort;
    private CoreServerType coreServerType;
    private int capacity;
    private MultiMap<String, UUID> uuidByNameMap;
    private Map<String, Object> dataMap;
    private Set<Server.Status> statusSet;

    public PacketKeepAlive(Server server) {
        this.name = server.getName();
        this.port = server.getPort();
        this.serverManagerPort = server.getServerManagerPort();
        this.coreServerType = server.getCoreServerType();
        this.capacity = server.getData().getCapacity();
        this.uuidByNameMap = server.getData().getUUIDByNameMap();
        this.dataMap = server.getData().getDataMap();
        this.statusSet = server.getData().getStatusSet();
    }

    @Override
    public void write(DataOutputStream stream) throws IOException {
        stream.writeInt(this.port);
        stream.writeInt(this.serverManagerPort);
        stream.writeUTF(this.name);
        stream.writeUTF(this.coreServerType.name());
        stream.writeInt(this.capacity);

        Map<String, String> map = new HashMap<>();
        this.uuidByNameMap.forEach((s, uuid) -> map.put(s, uuid.toString()));

        stream.writeUTF(Utils.GSON.toJson(map));
        stream.writeUTF(Utils.GSON.toJson(this.dataMap));
        stream.writeUTF(Utils.GSON.toJson(this.statusSet.stream().map(status -> status.name()).collect(Collectors.toSet())));
    }

    @Override
    public void read(DataInputStream stream) throws IOException {
        this.port = stream.readInt();
        this.serverManagerPort = stream.readInt();
        this.name = stream.readUTF();
        this.coreServerType = CoreServerType.valueOf(stream.readUTF());
        this.capacity = stream.readInt();

        Map<String, String> map = Utils.GSON.fromJson(stream.readUTF(), Utils.MAP_STRING_TYPE);
        this.uuidByNameMap = new MultiMap<>();
        map.forEach((s, s2) -> this.uuidByNameMap.put(s, UUID.fromString(s2)));

        this.dataMap = Utils.GSON.fromJson(stream.readUTF(), Utils.MAP_TYPE);
        this.statusSet = ((Set<String>) Utils.GSON.fromJson(stream.readUTF(), Utils.SET_TYPE)).stream().map(s -> {
            Server.Status status = Server.Status.valueOf(s);
            if(status == null){
                status = Server.Status.registerNewStatus(s);
            }
            return status;
        }).collect(Collectors.toSet());
    }

    @Override
    public void process(ServerManager serverManager) {
        KeepAliveBuffer.put(this);
    }

    public int getPort() {
        return this.port;
    }

    public String getName() {
        return this.name;
    }

    public CoreServerType getCoreServerType() {
        return this.coreServerType;
    }

    public int getCapacity() {
        return this.capacity;
    }

    public MultiMap<String, UUID> getUUIDByNameMap() {
        return this.uuidByNameMap;
    }

    public Map<String, Object> getDataMap() {
        return this.dataMap;
    }

    public int getServerManagerPort() {
        return this.serverManagerPort;
    }

    public Set<Server.Status> getStatusSet() {
        return this.statusSet;
    }
}
