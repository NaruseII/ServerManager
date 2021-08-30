package fr.naruse.servermanager.core.connection.packet;

import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.packetmanager.utils.ServerNameIncrement;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketFileManagerRequest {

    public static class NewName extends AbstractPacketResponsive {

        public NewName() {
        }

        private String baseName;
        public NewName(String serverToRespond, long threadId, String baseName) {
            super(serverToRespond, threadId);
            this.baseName = baseName;
        }

        @Override
        public void write(DataOutputStream stream) throws IOException {
            super.write(stream);
            stream.writeUTF(this.baseName);
        }

        @Override
        public void read(DataInputStream stream) throws IOException {
            super.read(stream);
            this.baseName = stream.readUTF();
        }

        @Override
        public void process(ServerManager serverManager) {
            if(serverManager.getCoreData().getCoreServerType().is(CoreServerType.PACKET_MANAGER)){
                super.respond(new NewNameResponse(this.getThreadId(), ServerNameIncrement.findNewName(this.baseName)));
            }
        }
    }

    public static class NewNameResponse extends AbstractPacketResponsive {

        public NewNameResponse() {
        }

        private String newName;
        public NewNameResponse(long threadId, String newName) {
            super("", threadId);
            this.newName = newName;
        }

        @Override
        public void write(DataOutputStream stream) throws IOException {
            super.write(stream);
            stream.writeUTF(this.newName);
        }

        @Override
        public void read(DataInputStream stream) throws IOException {
            super.read(stream);
            this.newName = stream.readUTF();
        }

        @Override
        public void process(ServerManager serverManager) {

        }

        public String getNewName() {
            return newName;
        }
    }
}
