package fr.naruse.servermanager.core.connection.packet;

import fr.naruse.servermanager.core.ServerManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketTeleportToPlayer implements IPacket {

    public PacketTeleportToPlayer() {
    }

    private String playerName;
    private String targetName;

    public PacketTeleportToPlayer(String playerName, String targetName) {
        this.playerName = playerName;
        this.targetName = targetName;
    }

    @Override
    public void write(DataOutputStream stream) throws IOException {
        stream.writeUTF(this.playerName);
        stream.writeUTF(this.targetName);
    }

    @Override
    public void read(DataInputStream stream) throws IOException {
        this.playerName = stream.readUTF();
        this.targetName = stream.readUTF();
    }

    @Override
    public void process(ServerManager serverManager) {

    }

    public String getPlayerName() {
        return playerName;
    }

    public String getTargetName() {
        return targetName;
    }
}
