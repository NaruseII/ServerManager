package fr.naruse.servermanager.core.connection.packet;

import fr.naruse.servermanager.core.ServerManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketShutdown implements IPacket {

    public PacketShutdown() {
    }

    @Override
    public void write(DataOutputStream stream) throws IOException {

    }

    @Override
    public void read(DataInputStream stream) throws IOException {

    }

    @Override
    public void process(ServerManager serverManager) {
        switch (serverManager.getCoreData().getCoreServerType()) {
            case PACKET_MANAGER:
            case FILE_MANAGER:
                System.exit(0);
                return;
            case BUKKIT_MANAGER:
            case BUNGEE_MANAGER:
                serverManager.getPlugin().shutdown();
                return;
        }
    }
}
