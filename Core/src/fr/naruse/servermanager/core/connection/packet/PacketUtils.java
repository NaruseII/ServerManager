package fr.naruse.servermanager.core.connection.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketUtils {

    public static void writeByteArray(DataOutputStream stream, byte[] bytes) throws IOException {
        stream.writeInt(bytes.length);
        stream.write(bytes);
    }

    public static byte[] readByteArray(DataInputStream stream) throws IOException {
        int length = stream.readInt();

        if(length > 0) {
            byte[] bytes = new byte[length];
            stream.readFully(bytes, 0, bytes.length);
            return bytes;
        }
        return null;
    }

}
