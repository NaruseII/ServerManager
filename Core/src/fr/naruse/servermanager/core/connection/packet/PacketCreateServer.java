package fr.naruse.servermanager.core.connection.packet;

import fr.naruse.api.config.Configuration;
import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.filemanager.FileManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

public class PacketCreateServer implements IPacket {

    public PacketCreateServer() {
    }

    private String templateName;
    public PacketCreateServer(String templateName) {
        this.templateName = templateName;
    }

    private boolean isSavedKey;
    public PacketCreateServer(String savedKey, boolean isSavedServer) {
        this(savedKey);
        this.isSavedKey = isSavedServer;
    }

    private Map<String, Object> initialServerData;
    public PacketCreateServer(String templateName, Map<String, Object> initialServerData) {
        this(templateName);
        this.initialServerData = initialServerData;
    }

    public PacketCreateServer(String savedKey, boolean isSavedServer, Map<String, Object> initialServerData) {
        this(savedKey, isSavedServer);
        this.initialServerData = initialServerData;
    }

    @Override
    public void write(DataOutputStream stream) throws IOException {
        stream.writeUTF(this.templateName);
        stream.writeBoolean(this.initialServerData != null);
        if(this.initialServerData != null){
            PacketUtils.writeByteArray(stream, new Configuration(this.initialServerData).toJson().getBytes());
        }
        stream.writeBoolean(this.isSavedKey);
    }

    @Override
    public void read(DataInputStream stream) throws IOException {
        this.templateName = stream.readUTF();

        if(stream.readBoolean()){
            this.initialServerData = new Configuration(new String(PacketUtils.readByteArray(stream))).getMainSection().getAll();
        }

        this.isSavedKey = stream.readBoolean();
    }

    @Override
    public void process(ServerManager serverManager) {
        switch (serverManager.getCoreData().getCoreServerType()){
            case FILE_MANAGER:
                FileManager.get().createServer(this.templateName, this.isSavedKey, this.initialServerData);
                return;
            case PACKET_MANAGER:
                Server server = ServerList.getAll().stream().filter(s -> s.getCoreServerType() == CoreServerType.FILE_MANAGER).findFirst().get();
                if(server != null){
                    server.sendPacket(new PacketCreateServer(this.templateName));
                }
                return;
        }
    }
}
