package fr.naruse.servermanager.core.connection.packet;

import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.database.Database;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class PacketDatabaseAnswer implements IPacket {

    public PacketDatabaseAnswer() {
    }

    private int callBackId;
    private Database.DataObject[] dataObjects;
    public PacketDatabaseAnswer(int callBackId, Database.DataObject... dataObjects) {
        this.callBackId = callBackId;
        this.dataObjects = dataObjects;
    }

    @Override
    public void write(DataOutputStream stream) throws IOException {
        stream.writeInt(this.callBackId);
        stream.writeInt(this.dataObjects.length);
        for (int i = 0; i < this.dataObjects.length; i++) {
            Database.DataObject dataObject = this.dataObjects[i];
            stream.writeInt(dataObject.getDataType().getId());
            stream.writeUTF(dataObject.getDataType().toString(dataObject.getValue()));
        }
    }

    @Override
    public void read(DataInputStream stream) throws IOException {
        this.callBackId = stream.readInt();
        Set<Database.DataObject> set = new HashSet<>();
        int count = stream.readInt();
        for (int i = 0; i < count; i++) {
            Database.DataType dataType = Database.DataType.byId(stream.readInt());
            set.add(new Database.DataObject(dataType, dataType.toObject(stream.readUTF())));
        }
        this.dataObjects = set.toArray(new Database.DataObject[0]);
    }

    @Override
    public void process(ServerManager serverManager) {
        PacketDatabaseRequest.Callback.process(this.callBackId, this.dataObjects);
    }
}
