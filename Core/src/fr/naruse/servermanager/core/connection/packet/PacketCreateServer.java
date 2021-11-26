package fr.naruse.servermanager.core.connection.packet;

import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.filemanager.FileManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketCreateServer implements IPacket {

    public PacketCreateServer() {
    }

    private String templateName;
    public PacketCreateServer(String templateName) {
        this.templateName = templateName;
    }

    @Override
    public void write(DataOutputStream stream) throws IOException {
        stream.writeUTF(this.templateName);
    }

    @Override
    public void read(DataInputStream stream) throws IOException {
        this.templateName = stream.readUTF();
    }

    @Override
    public void process(ServerManager serverManager) {
        switch (serverManager.getCoreData().getCoreServerType()){
            case FILE_MANAGER:
                FileManager.get().createServer(this.templateName);
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
