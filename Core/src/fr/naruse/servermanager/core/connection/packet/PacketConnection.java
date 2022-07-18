package fr.naruse.servermanager.core.connection.packet;

import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.utils.Utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PacketConnection implements IPacket {

    public PacketConnection() {
    }

    private String name;
    private int port;
    private String host;
    private int serverManagerPort;
    private CoreServerType coreServerType;
    private Map<String, Object> dataMap;

    public PacketConnection(Server server) {
        this.name = server.getName();
        this.port = server.getPort();
        this.host = server.getAddress().getHostAddress();
        this.serverManagerPort = server.getServerManagerPort();
        this.coreServerType = server.getCoreServerType();
        this.dataMap = server.getData().getDataMap();
    }

    @Override
    public void write(DataOutputStream stream) throws IOException {
        stream.writeInt(this.port);
        stream.writeUTF(this.host);
        stream.writeInt(this.serverManagerPort);
        stream.writeUTF(this.name);
        stream.writeUTF(this.coreServerType.name());

        stream.writeBoolean(!this.dataMap.isEmpty());
        if(!this.dataMap.isEmpty()){
            stream.writeUTF(Utils.GSON.toJson(this.dataMap));
        }
    }

    @Override
    public void read(DataInputStream stream) throws IOException {
        this.port = stream.readInt();
        this.host = stream.readUTF();
        this.serverManagerPort = stream.readInt();
        this.name = stream.readUTF();
        this.coreServerType = CoreServerType.valueOf(stream.readUTF());

        if(stream.readBoolean()){
            this.dataMap = Utils.GSON.fromJson(stream.readUTF(), Utils.MAP_TYPE);
        }else{
            this.dataMap = new HashMap<>();
        }
    }

    @Override
    public void process(ServerManager serverManager) {
        ServerList.createNewServer(this.name, this.port, this.host, this.serverManagerPort, this.coreServerType, this.dataMap);
    }
}
