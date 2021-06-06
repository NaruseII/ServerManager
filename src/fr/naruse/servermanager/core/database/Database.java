package fr.naruse.servermanager.core.database;

import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.callback.CallbackPlural;
import fr.naruse.servermanager.core.callback.CallbackSingle;
import fr.naruse.servermanager.core.connection.packet.PacketDatabaseRequest;
import fr.naruse.servermanager.core.utils.Utils;

import java.util.*;

public class Database {

    public static void sendPut(String key, DataObject dataObject){
        sendPut(key, dataObject, false);
    }

    public static void sendPut(String key, DataObject dataObject, boolean sendUpdatePacket){
        ServerManager.get().getConnectionManager().sendPacket(PacketDatabaseRequest.Builder.put(key, dataObject, sendUpdatePacket));
    }

    public static void sendGet(String key, CallbackSingle callbackSingle){
        sendGet(key, callbackSingle, false);
    }

    public static void sendGet(String key, CallbackSingle callbackSingle, boolean sendUpdatePacket){
        ServerManager.get().getConnectionManager().sendPacket(PacketDatabaseRequest.Builder.get(key, callbackSingle, sendUpdatePacket));
    }

    public static void sendRemove(String key){
        sendRemove(key, false);
    }

    public static void sendRemove(String key, boolean sendUpdatePacket){
        ServerManager.get().getConnectionManager().sendPacket(PacketDatabaseRequest.Builder.remove(key, sendUpdatePacket));
    }

    public static void sendClear(){
        sendClear(false);
    }

    public static void sendClear(boolean sendUpdatePacket){
        ServerManager.get().getConnectionManager().sendPacket(PacketDatabaseRequest.Builder.clear(sendUpdatePacket));
    }

    public static void sendGetAll(CallbackPlural callbackPlural){
        sendGetAll(callbackPlural, false);
    }

    public static void sendGetAll(CallbackPlural callbackPlural, boolean sendUpdatePacket){
        ServerManager.get().getConnectionManager().sendPacket(PacketDatabaseRequest.Builder.getAll(callbackPlural, sendUpdatePacket));
    }



    private final Map<String, DataObject> MAP = new HashMap<>();

    public <T> T get(String key){
        return (T) MAP.get(key);
    }

    public Set<DataObject> getAll(){
        return new HashSet<>(MAP.values());
    }

    public Map<String, DataObject> getMap() {
        return MAP;
    }

    public void clear(){
        MAP.clear();
    }

    public void put(String key, DataObject value){
        MAP.put(key, value);
    }

    public void remove(String key){
        MAP.remove(key);
    }

    public static class DataObject {

        private final DataType dataType;
        private final Object value;

        public DataObject(DataType dataType, Object value) {
            this.dataType = dataType;
            this.value = value;
        }

        public DataType getDataType() {
            return dataType;
        }

        public Object getValue() {
            return value;
        }
    }


    public abstract static class DataType<T> {

        private static final Map<Integer, DataType> map = new HashMap<>();

        public static DataType<Integer> INTEGER = new DataType<Integer>(0) {
            @Override
            public Integer toObject(String value) {
                return Integer.valueOf(value);
            }
        };
        public static DataType<Double> DOUBLE = new DataType<Double>(1) {
            @Override
            public Double toObject(String value) {
                return Double.valueOf(value);
            }
        };
        public static DataType<Float> FLOAT = new DataType<Float>(2) {
            @Override
            public Float toObject(String value) {
                return Float.valueOf(value);
            }
        };
        public static DataType<String> STRING = new DataType<String>(3) {
            @Override
            public String toObject(String value) {
                return value;
            }
        };
        public static DataType<Boolean> BOOLEAN = new DataType<Boolean>(4) {
            @Override
            public Boolean toObject(String value) {
                return Boolean.valueOf(value);
            }
        };
        public static DataType<Character> CHAR = new DataType<Character>(5) {
            @Override
            public Character toObject(String value) {
                return value.charAt(0);
            }
        };
        public static DataType<Long> LONG = new DataType<Long>(6) {
            @Override
            public Long toObject(String value) {
                return Long.valueOf(value);
            }
        };
        public static DataType<List> LIST = new DataType<List>(7) {

            @Override
            public String toString(List value) {
                return Utils.GSON.toJson(value);
            }

            @Override
            public List toObject(String value) {
                return Utils.GSON.fromJson(value, Utils.LIST_GENERIC_TYPE);
            }
        };
        public static DataType<Map> MAP = new DataType<Map>(8) {

            @Override
            public String toString(Map value) {
                return Utils.GSON.toJson(value);
            }

            @Override
            public Map toObject(String value) {
                return Utils.GSON.fromJson(value, Utils.MAP_GENERIC_TYPE);
            }
        };

        public static DataType byId(int id){
            return map.get(id);
        }

        private final int id;
        private DataType(int id) {
            this.id = id;
            map.put(id, this);
        }

        public int getId() {
            return id;
        }

        public <T> T cast(Object value){
            return (T) value;
        }

        public String toString(T value) {
            return value.toString();
        }

        public abstract T toObject(String value) ;

    }
}
