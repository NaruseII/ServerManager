package fr.naruse.servermanager.core.connection.packet;

import fr.naruse.servermanager.core.ServerManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketBungeeRequestConfigWrite implements IPacket {

    public PacketBungeeRequestConfigWrite() {
    }

    private String bungeeName;
    private String serverTarget;
    private boolean delete;
    public PacketBungeeRequestConfigWrite(String bungeeName, String serverTarget, boolean delete) {
        this.bungeeName = bungeeName;
        this.serverTarget = serverTarget;
        this.delete = delete;
    }

    @Override
    public void write(DataOutputStream stream) throws IOException {
        stream.writeUTF(this.bungeeName);
        stream.writeUTF(this.serverTarget);
        stream.writeBoolean(this.delete);
    }

    @Override
    public void read(DataInputStream stream) throws IOException {
        this.bungeeName = stream.readUTF();
        this.serverTarget = stream.readUTF();
        this.delete = stream.readBoolean();
    }

    @Override
    public void process(ServerManager serverManager) {

    }

    public String getBungeeName() {
        return bungeeName;
    }

    public String getServerTarget() {
        return serverTarget;
    }

    public boolean needToDelete() {
        return delete;
    }
}
