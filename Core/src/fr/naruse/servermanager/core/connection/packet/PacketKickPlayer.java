package fr.naruse.servermanager.core.connection.packet;

import fr.naruse.servermanager.core.ServerManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketKickPlayer implements IPacket {

    public PacketKickPlayer() {
    }

    private String playerName;
    private String reason;

    public PacketKickPlayer(String playerName) {
        this.playerName = playerName;
    }

    public PacketKickPlayer(String playerName, String reason) {
        this(playerName);
        this.reason = reason;
    }

    @Override
    public void write(DataOutputStream stream) throws IOException {
        stream.writeUTF(this.playerName);
        if(this.reason == null){
            stream.writeBoolean(false);
        }else{
            stream.writeBoolean(true);
            stream.writeUTF(this.reason);
        }
    }

    @Override
    public void read(DataInputStream stream) throws IOException {
        this.playerName = stream.readUTF();
        if(stream.readBoolean()){
            this.reason = stream.readUTF();
        }
    }

    @Override
    public void process(ServerManager serverManager) {

    }

    public String getPlayerName() {
        return playerName;
    }

    public String getReason() {
        return reason;
    }
}
