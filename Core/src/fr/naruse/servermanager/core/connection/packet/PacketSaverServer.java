package fr.naruse.servermanager.core.connection.packet;

import fr.naruse.servermanager.core.ServerManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketSaverServer implements IPacket {

    public PacketSaverServer() {
    }

    private String serverName;
    private boolean shouldSave;
    private String saveKey;

    public PacketSaverServer(String serverName, boolean shouldSave, String saveKey){
        this.serverName = serverName;
        this.shouldSave = shouldSave;
        this.saveKey = saveKey;
    }

    @Override
    public void write(DataOutputStream stream) throws IOException {
        stream.writeUTF(this.serverName);
        stream.writeBoolean(this.shouldSave);
        stream.writeUTF(this.saveKey);
    }

    @Override
    public void read(DataInputStream stream) throws IOException {
        this.serverName = stream.readUTF();
        this.shouldSave = stream.readBoolean();
        this.saveKey = stream.readUTF();
    }

    @Override
    public void process(ServerManager serverManager) {

    }

    public String getServerName() {
        return this.serverName;
    }

    public boolean shouldSaveOnShutdown() {
        return this.shouldSave;
    }

    public String getSaveKey() {
        return this.saveKey;
    }
}
