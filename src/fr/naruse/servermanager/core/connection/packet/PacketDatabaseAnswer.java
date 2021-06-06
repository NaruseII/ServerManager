package fr.naruse.servermanager.core.connection.packet;

import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.callback.Callback;
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
        if(this.dataObjects == null){
            stream.writeInt(0);
            return;
        }
        stream.writeInt(this.dataObjects.length);
        for (int i = 0; i < this.dataObjects.length; i++) {
            Database.DataObject dataObject = this.dataObjects[i];
            if(dataObject == null){
                stream.writeInt(-1);
            }else{
                stream.writeInt(dataObject.getDataType().getId());
                stream.writeUTF(dataObject.getDataType().toString(dataObject.getValue()));
            }
        }
    }

    @Override
    public void read(DataInputStream stream) throws IOException {
        this.callBackId = stream.readInt();
        Set<Database.DataObject> set = new HashSet<>();
        int count = stream.readInt();
        if(count == 0){
            return;
        }
        for (int i = 0; i < count; i++) {
            int id = stream.readInt();
            if(id == -1){
                return;
            }else{
                Database.DataType dataType = Database.DataType.byId(id);
                set.add(new Database.DataObject(dataType, dataType.toObject(stream.readUTF())));
            }
        }
        this.dataObjects = set.toArray(new Database.DataObject[0]);
    }

    @Override
    public void process(ServerManager serverManager) {
        Callback.process(this.callBackId, this.dataObjects);
    }
}
