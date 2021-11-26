package fr.naruse.servermanager.core.connection.packet;

import fr.naruse.servermanager.core.ServerManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketBroadcast implements IPacket {

    public PacketBroadcast() {
    }

    private String msg;
    public PacketBroadcast(String msg) {
        this.msg = msg;
    }

    @Override
    public void write(DataOutputStream stream) throws IOException {
        stream.writeUTF(this.msg);
    }

    @Override
    public void read(DataInputStream stream) throws IOException {
        this.msg = stream.readUTF();
    }

    @Override
    public void process(ServerManager serverManager) {

    }

    public String getMessage() {
        return msg;
    }
}
