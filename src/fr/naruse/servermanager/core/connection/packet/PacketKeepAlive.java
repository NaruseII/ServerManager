package fr.naruse.servermanager.core.connection.packet;

import fr.naruse.servermanager.core.*;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.packetmanager.KeepAliveBuffer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PacketKeepAlive implements IPacket {

    public PacketKeepAlive() {
    }

    private String name;
    private int port;
    private int serverManagerPort;
    private CoreServerType coreServerType;
    private int capacity;
    private Map<String, String> uuidByNameMap;
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
        stream.writeUTF(Utils.GSON.toJson(this.uuidByNameMap));
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
        this.uuidByNameMap = Utils.GSON.fromJson(stream.readUTF(), Utils.MAP_STRING_TYPE);
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

    public int getServerManagerPort() {
        return serverManagerPort;
    }

    public Set<Server.Status> getStatusSet() {
        return statusSet;
    }
}
