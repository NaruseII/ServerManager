package fr.naruse.servermanager.core.connection.packet;

import fr.naruse.servermanager.core.ServerManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketSendTemplate implements IPacket {

    public PacketSendTemplate() {
    }

    private String json;
    public PacketSendTemplate(String json) {
        this.json = json;
    }

    @Override
    public void write(DataOutputStream stream) throws IOException {
        stream.writeUTF(this.json);
    }

    @Override
    public void read(DataInputStream stream) throws IOException {
        this.json = stream.readUTF();
    }

    @Override
    public void process(ServerManager serverManager) {

    }

    public String getJson() {
        return json;
    }
}
