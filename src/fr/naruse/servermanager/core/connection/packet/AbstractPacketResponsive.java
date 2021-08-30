package fr.naruse.servermanager.core.connection.packet;

import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class AbstractPacketResponsive implements IPacket {

    private long threadId;
    private String serverNameToRespond;

    public AbstractPacketResponsive() {
    }

    public AbstractPacketResponsive(String serverNameToRespond, long threadId) {
        this.serverNameToRespond = serverNameToRespond;
        this.threadId = threadId;
    }

    @Override
    public void write(DataOutputStream stream) throws IOException {
        stream.writeLong(this.threadId);
        stream.writeUTF(this.serverNameToRespond);
    }

    @Override
    public void read(DataInputStream stream) throws IOException {
        this.threadId = stream.readLong();
        this.serverNameToRespond = stream.readUTF();
    }

    public void respond(AbstractPacketResponsive packet){
        Server server = ServerList.getByName(this.serverNameToRespond);
        if(server != null){
            server.sendPacket(packet);
        }
    }

    public long getThreadId() {
        return threadId;
    }

    public String getServerNameToRespond() {
        return serverNameToRespond;
    }
}
