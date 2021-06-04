package fr.naruse.servermanager.core.connection.packet;

import fr.naruse.servermanager.core.ServerManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketReloadProxyServers implements IPacket{

    public PacketReloadProxyServers() {
    }

    private String defaultServer;
    private boolean transformToLocalhostIfPossible;
    public PacketReloadProxyServers(String defaultServer, boolean transformToLocalhostIfPossible) {
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

    }

    public String getDefaultServer() {
        return defaultServer;
    }

    public boolean transformToLocalhostIfPossible() {
        return transformToLocalhostIfPossible;
    }
}
