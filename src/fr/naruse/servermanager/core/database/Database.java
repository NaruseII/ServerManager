package fr.naruse.servermanager.core.database;

import fr.naruse.servermanager.core.utils.Utils;

import java.util.*;

public class Database {

    private static final Map<String, DataObject> MAP = new HashMap<>();

    public static <T> T get(String key){
        return (T) MAP.get(key);
    }

    public static Set<DataObject> getAll(){
        return new HashSet<>(MAP.values());
    }

    public static void clear(){
        MAP.clear();
    }

    public static void put(String key, DataObject value){
        MAP.put(key, value);
    }

    public static void remove(String key){
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

        public static DataType<Integer> INTEGER = new DataType<Integer>() {
            @Override
            public Integer toObject(String value) {
                return Integer.valueOf(value);
            }
        };
        public static DataType<Double> DOUBLE = new DataType<Double>() {
            @Override
            public Double toObject(String value) {
                return Double.valueOf(value);
            }
        };
        public static DataType<Float> FLOAT = new DataType<Float>() {
            @Override
            public Float toObject(String value) {
                return Float.valueOf(value);
            }
        };
        public static DataType<String> STRING = new DataType<String>() {
            @Override
            public String toObject(String value) {
                return value;
            }
        };
        public static DataType<Boolean> BOOLEAN = new DataType<Boolean>() {
            @Override
            public Boolean toObject(String value) {
                return Boolean.valueOf(value);
            }
        };
        public static DataType<Character> CHAR = new DataType<Character>() {
            @Override
            public Character toObject(String value) {
                return value.charAt(0);
            }
        };
        public static DataType<Long> LONG = new DataType<Long>() {
            @Override
            public Long toObject(String value) {
                return Long.valueOf(value);
            }
        };
        public static DataType<List> LIST = new DataType<List>() {

            @Override
            public String toString(List value) {
                return Utils.GSON.toJson(value);
            }

            @Override
            public List toObject(String value) {
                return Utils.GSON.fromJson(value, Utils.LIST_GENERIC_TYPE);
            }
        };
        public static DataType<Map> MAP = new DataType<Map>() {

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

        private int id;
        private DataType() {
            map.put(this.id = map.size(), this);
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
