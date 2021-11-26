package fr.naruse.servermanager.core.connection.packet;

import fr.naruse.servermanager.core.ServerManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketTeleportToLocation implements IPacket {

    public PacketTeleportToLocation() {
    }

    private String playerName;
    private String worldName;
    private double x;
    private double y;
    private double z;
    private float pitch;
    private float yaw;

    public PacketTeleportToLocation(String playerName, String worldName, double x, double y, double z, float yaw, float pitch) {
        this.playerName = playerName;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    @Override
    public void write(DataOutputStream stream) throws IOException {
        stream.writeUTF(this.playerName);
        stream.writeUTF(this.worldName);
        stream.writeDouble(this.x);
        stream.writeDouble(this.y);
        stream.writeDouble(this.z);
        stream.writeFloat(this.yaw);
        stream.writeFloat(this.pitch);
    }

    @Override
    public void read(DataInputStream stream) throws IOException {
        this.playerName = stream.readUTF();
        this.worldName = stream.readUTF();
        this.x = stream.readDouble();
        this.y = stream.readDouble();
        this.z = stream.readDouble();
        this.yaw = stream.readFloat();
        this.pitch = stream.readFloat();
    }

    @Override
    public void process(ServerManager serverManager) {

    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public double getZ() {
        return z;
    }

    public double getY() {
        return y;
    }

    public double getX() {
        return x;
    }

    public String getWorldName() {
        return worldName;
    }

    public String getPlayerName() {
        return playerName;
    }
}
