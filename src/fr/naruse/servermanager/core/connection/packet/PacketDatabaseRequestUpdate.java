package fr.naruse.servermanager.core.connection.packet;

import fr.naruse.servermanager.core.ServerManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketDatabaseRequestUpdate implements IPacket {

    public PacketDatabaseRequestUpdate() {
    }

    private PacketDatabaseRequest packet;
    public PacketDatabaseRequestUpdate(PacketDatabaseRequest packet) {
        this.packet = packet;
    }

    @Override
    public void write(DataOutputStream stream) throws IOException {
        packet.write(stream);
    }

    @Override
    public void read(DataInputStream stream) throws IOException {
        this.packet = new PacketDatabaseRequest();
        this.packet.read(stream);
    }

    @Override
    public void process(ServerManager serverManager) {

    }

    public PacketDatabaseRequest getPacket() {
        return packet;
    }
}
