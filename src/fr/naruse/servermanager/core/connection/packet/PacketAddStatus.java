package fr.naruse.servermanager.core.connection.packet;

import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.server.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PacketAddStatus implements IPacket {

    public PacketAddStatus() {
    }

    private List<String> status;
    public PacketAddStatus(List<String> status) {
        this.status = status;
    }

    @Override
    public void write(DataOutputStream stream) throws IOException {
        stream.writeInt(this.status.size());
        for (String s : this.status) {
            stream.writeUTF(s);
        }
    }

    @Override
    public void read(DataInputStream stream) throws IOException {
        int size = stream.readInt();
        this.status = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            this.status.add(stream.readUTF());
        }
    }

    @Override
    public void process(ServerManager serverManager) {
        for (String s : this.status) {
            serverManager.getCurrentServer().getData().addStatus(Server.Status.registerNewStatus(s));
        }
    }

    public List<String> getStatus() {
        return status;
    }
}
