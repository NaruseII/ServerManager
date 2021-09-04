package fr.naruse.servermanager.core.connection.packet;

import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.config.Configuration;
import fr.naruse.servermanager.core.database.DatabaseAPI;
import fr.naruse.servermanager.core.database.DatabaseTable;
import fr.naruse.servermanager.core.database.IDatabaseTable;
import fr.naruse.servermanager.packetmanager.PacketManager;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class PacketDatabase {

    public static class Update implements IPacket {

        private IDatabaseTable table;
        private String serverSender;

        public Update() { }

        public Update(IDatabaseTable table) {
            this.table = table;
        }

        @Override
        public void write(DataOutputStream stream) throws IOException {
            stream.writeUTF(ServerManager.get().getCurrentServer().getName());
            byte[] jsonBytes = this.table.serialize().getBytes();

            PacketUtils.writeByteArray(stream, jsonBytes);
        }

        @Override
        public void read(DataInputStream stream) throws IOException {
            this.serverSender = stream.readUTF();
            byte[] bytes = PacketUtils.readByteArray(stream);
            if(bytes != null){
                this.table = DatabaseTable.Builder.deserialize(new Configuration(new String(bytes)));
            }
        }

        @Override
        public void process(ServerManager serverManager) {
            if(!ServerManager.get().getCoreData().getCoreServerType().is(CoreServerType.PACKET_MANAGER)){
                return;
            }
            PacketManager.get().getDatabase().update(this.table, this.serverSender);
        }

        public String getServerSender() {
            return serverSender;
        }

        public IDatabaseTable getTable() {
            return table;
        }
    }

    public static class Destroy implements IPacket {

        private String table;

        public Destroy() { }

        public Destroy(String table) {
            this.table = table;
        }

        @Override
        public void write(DataOutputStream stream) throws IOException {
            stream.writeUTF(this.table);
        }

        @Override
        public void read(DataInputStream stream) throws IOException {
            this.table = stream.readUTF();
        }

        @Override
        public void process(ServerManager serverManager) {
            if(ServerManager.get().getCoreData().getCoreServerType().is(CoreServerType.PACKET_MANAGER)){
                PacketManager.get().getDatabase().destroy(this.table);
            }else{
                ((DatabaseAPI.Cache) DatabaseAPI.getCache()).destroy(this.table);
            }
        }

        public String getTable() {
            return table;
        }
    }

    public static class UpdateCache implements IPacket {

        private Set<IDatabaseTable> set;

        public UpdateCache() { }

        public UpdateCache(Set<IDatabaseTable> set) {
            this.set = set;
        }

        @Override
        public void write(DataOutputStream stream) throws IOException {
            stream.writeInt(this.set.size());
            for (IDatabaseTable table : this.set) {
                new Update(table).write(stream);
            }
        }

        @Override
        public void read(DataInputStream stream) throws IOException {
            this.set = new HashSet<>();
            int size = stream.readInt();

            for (int i = 0; i < size; i++) {
                Update update = new Update();
                update.read(stream);
                this.set.add(update.getTable());
            }
        }

        @Override
        public void process(ServerManager serverManager) {
            this.set.forEach(iDatabaseTable -> ((DatabaseAPI.Cache) DatabaseAPI.getCache()).updateCache(iDatabaseTable));
        }

        public Set<IDatabaseTable> getTableSet() {
            return set;
        }
    }
}
