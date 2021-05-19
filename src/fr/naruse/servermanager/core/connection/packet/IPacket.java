package fr.naruse.servermanager.core.connection.packet;

import fr.naruse.servermanager.core.ServerManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface IPacket {

    void write(DataOutputStream stream) throws IOException;

    void read(DataInputStream stream) throws IOException;

    void process(ServerManager serverManager);
}
