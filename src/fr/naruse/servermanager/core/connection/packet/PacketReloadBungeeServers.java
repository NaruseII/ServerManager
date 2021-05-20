package fr.naruse.servermanager.core.connection.packet;

import fr.naruse.servermanager.core.ServerManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketReloadBungeeServers implements IPacket{

    public PacketReloadBungeeServers() {
    }

    private String defaultServer;
    private boolean transformToLocalhostIfPossible;
    public PacketReloadBungeeServers(String defaultServer, boolean transformToLocalhostIfPossible) {
        this.defaultServer = defaultServer;
    }

    @Override
    public void write(DataOutputStream stream) throws IOException {
        stream.writeUTF(this.defaultServer);
        stream.writeBoolean(this.transformToLocalhostIfPossible);
    }

    @Override
    public void read(DataInputStream stream) throws IOException {
        this.defaultServer = stream.readUTF();
        this.transformToLocalhostIfPossible = stream.readBoolean();
    }

    @Override
    public void process(ServerManager serverManager) {
        serverManager.processPacket(this);
    }

    public String getDefaultServer() {
        return defaultServer;
    }

    public boolean transformToLocalhostIfPossible() {
        return transformToLocalhostIfPossible;
    }
}
