package fr.naruse.servermanager.core.connection.packet;

import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.callback.Callback;
import fr.naruse.servermanager.core.callback.CallbackPlural;
import fr.naruse.servermanager.core.callback.CallbackSingle;
import fr.naruse.servermanager.core.database.Database;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketDatabaseRequest implements IPacket {

    public PacketDatabaseRequest() {
    }

    private Server server = ServerManager.get().getCurrentServer();
    private Action action;
    private String key;
    private Database.DataObject dataObject;
    private int callbackId = -1;
    private boolean sendUpdatePacket;

    private PacketDatabaseRequest(Action action, String key, Database.DataObject dataObject, boolean sendUpdatePacket) {
        this(action, key, sendUpdatePacket);
        this.dataObject = dataObject;
    }

    private PacketDatabaseRequest(Action action, String key, boolean sendUpdatePacket) {
        this(action, sendUpdatePacket);
        this.key = key;
    }

    private PacketDatabaseRequest(Action action, String key, int callbackId, boolean sendUpdatePacket) {
        this(action, key, sendUpdatePacket);
        this.callbackId = callbackId;
    }

    private PacketDatabaseRequest(Action action, boolean sendUpdatePacket) {
        this.action = action;
        this.sendUpdatePacket = sendUpdatePacket;
    }

    private PacketDatabaseRequest(Action action, int callbackId, boolean sendUpdatePacket) {
        this(action, sendUpdatePacket);
        this.callbackId = callbackId;
    }

    @Override
    public void write(DataOutputStream stream) throws IOException {
        stream.writeUTF(this.server.getName());
        stream.writeInt(this.callbackId);
        stream.writeBoolean(this.sendUpdatePacket);
        stream.writeUTF(this.action.name());
        if(this.key != null){
            stream.writeUTF(this.key);
            if(this.dataObject != null){
                stream.writeInt(this.dataObject.getDataType().getId());
                stream.writeUTF(this.dataObject.getDataType().toString(this.dataObject.getValue()));
            }
        }
    }

    @Override
    public void read(DataInputStream stream) throws IOException {
        String serverName = stream.readUTF();
        this.server = ServerList.getByName(serverName);

        if(server == null){
            if(ServerManager.get().getCurrentServer().getName().equals(serverName)){
                this.server = ServerManager.get().getCurrentServer();
            }else{
                return;
            }
        }

        this.callbackId = stream.readInt();
        this.sendUpdatePacket = stream.readBoolean();

        this.action = Action.valueOf(stream.readUTF());
        switch (this.action){
            case CLEAR:
            case GET_ALL:
                return;
        }

        this.key = stream.readUTF();

        if(this.action == Action.PUT){
            Database.DataType dataType = Database.DataType.byId(stream.readInt());
            this.dataObject = new Database.DataObject(dataType, dataType.toObject(stream.readUTF()));
        }
    }

    @Override
    public void process(ServerManager serverManager) {

    }

    @Override
    public String toString() {
        return "PacketDatabaseRequest{" +
                "server=" + server +
                ", action=" + action +
                ", key='" + key + '\'' +
                ", dataObject=" + dataObject +
                ", callbackId=" + callbackId +
                ", sendUpdatePacket=" + sendUpdatePacket +
                '}';
    }

    public Server getServer() {
        return server;
    }

    public Action getAction() {
        return action;
    }

    public Database.DataObject getDataObject() {
        return dataObject;
    }

    public int getCallbackId() {
        return callbackId;
    }

    public String getKey() {
        return key;
    }

    public boolean needToSendUpdatePacket() {
        return sendUpdatePacket;
    }

    public static class Builder {

        public static PacketDatabaseRequest put(String key, Database.DataObject dataObject, boolean sendUpdatePacket){
            return new PacketDatabaseRequest(Action.PUT, key, dataObject, sendUpdatePacket);
        }

        public static PacketDatabaseRequest get(String key, CallbackSingle callbackSingle, boolean sendUpdatePacket){
            int callbackId = Callback.CALLBACK_ID_MAP.size();
            Callback.CALLBACK_ID_MAP.put(callbackId, callbackSingle);
            return new PacketDatabaseRequest(Action.GET, key, callbackId, sendUpdatePacket);
        }

        public static PacketDatabaseRequest remove(String key, boolean sendUpdatePacket){
            return new PacketDatabaseRequest(Action.REMOVE, key, sendUpdatePacket);
        }

        public static PacketDatabaseRequest clear(boolean sendUpdatePacket){
            return new PacketDatabaseRequest(Action.CLEAR, sendUpdatePacket);
        }

        public static PacketDatabaseRequest getAll(CallbackPlural callbackPlural, boolean sendUpdatePacket){
            int callbackId = Callback.CALLBACK_ID_MAP.size();
            Callback.CALLBACK_ID_MAP.put(callbackId, callbackPlural);
            return new PacketDatabaseRequest(Action.GET_ALL, callbackId, sendUpdatePacket);
        }

    }

    public enum Action {

        PUT,
        GET,
        GET_ALL,
        REMOVE,
        CLEAR,

    }
}
