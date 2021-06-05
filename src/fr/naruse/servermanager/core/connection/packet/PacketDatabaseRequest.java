package fr.naruse.servermanager.core.connection.packet;

import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.database.Database;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

public class PacketDatabaseRequest implements IPacket {

    public PacketDatabaseRequest() {
    }

    private Server server = ServerManager.get().getCurrentServer();
    private Action action;
    private String key;
    private Database.DataObject dataObject;
    private int callbackId = -1;

    private PacketDatabaseRequest(Action action, String key, Database.DataObject dataObject) {
        this.action = action;
        this.key = key;
        this.dataObject = dataObject;
    }

    private PacketDatabaseRequest(Action action, String key) {
        this.action = action;
        this.key = key;
    }

    private PacketDatabaseRequest(Action action, String key, int callbackId) {
        this(action, key);
        this.callbackId = callbackId;
    }

    private PacketDatabaseRequest(Action action) {
        this.action = action;
    }

    private PacketDatabaseRequest(Action action, int callbackId) {
        this(action);
        this.callbackId = callbackId;
    }

    @Override
    public void write(DataOutputStream stream) throws IOException {
        stream.writeUTF(this.server.getName());
        stream.writeInt(this.callbackId);
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
        this.server = ServerList.getByName(stream.readUTF());

        if(server == null){
            return;
        }

        this.callbackId = stream.readInt();

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
        if(serverManager.getCoreData().getCoreServerType().is(CoreServerType.PACKET_MANAGER)){
            switch (this.action) {
                case CLEAR:
                    Database.clear();
                    return;
                case REMOVE:
                    Database.remove(this.key);
                    return;
                case PUT:
                    Database.put(this.key, this.dataObject);
                    return;
                case GET_ALL:
                    this.server.sendPacket(new PacketDatabaseAnswer(this.callbackId, Database.getAll().toArray(new Database.DataObject[0])));
                    return;
                case GET:
                    Database.DataObject dataObject = Database.get(this.key);
                    if(dataObject == null){
                        return;
                    }
                    this.server.sendPacket(new PacketDatabaseAnswer(this.callbackId, dataObject));
                    return;
            }
        }
    }

    public static class Builder {

        public static PacketDatabaseRequest put(String key, Database.DataObject dataObject){
            return new PacketDatabaseRequest(Action.PUT, key, dataObject);
        }

        public static PacketDatabaseRequest get(String key, CallbackSingle callbackSingle){
            int callbackId = CALLBACK_MAP.size();
            CALLBACK_MAP.put(callbackId, callbackSingle);
            return new PacketDatabaseRequest(Action.GET, key, callbackId);
        }

        public static PacketDatabaseRequest remove(String key){
            return new PacketDatabaseRequest(Action.REMOVE, key);
        }

        public static PacketDatabaseRequest clear(){
            return new PacketDatabaseRequest(Action.CLEAR);
        }

        public static PacketDatabaseRequest getAll(CallbackPlural callbackPlural){
            int callbackId = CALLBACK_MAP.size();
            CALLBACK_MAP.put(callbackId, callbackPlural);
            return new PacketDatabaseRequest(Action.GET_ALL, callbackId);
        }

    }

    private static final Map<Integer, Callback> CALLBACK_MAP = new HashMap<>();

    public static abstract class Callback<T> {

        public static void process(int id, Database.DataObject[] array){
            Callback callback = CALLBACK_MAP.remove(id);
            if(callback != null){
                List list = Arrays.asList(array.clone());
                if(list.size() == 1){
                    callback.runSingle(list.get(0));
                }
                callback.runPlural(list);
            }
        }

        public abstract void runPlural(List<T> values) ;

        public abstract void runSingle(T value);

    }

    public static abstract class CallbackSingle extends Callback<Database.DataObject> {
        @Override
        public void runPlural(List<Database.DataObject> values) {

        }
    }

    public static abstract class CallbackPlural extends Callback<Database.DataObject> {
        @Override
        public void runSingle(Database.DataObject value) {

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
