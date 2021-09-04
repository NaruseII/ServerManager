package fr.naruse.servermanager.core.database.structure;

import fr.naruse.servermanager.core.database.ValueType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColumnStructure {

    private final int columnId;
    private final String columnName;
    private final ValueType valueType;

    public ColumnStructure(int columnId, String columnName, ValueType valueType) {
        this.columnId = columnId;
        this.columnName = columnName;
        this.valueType = valueType;
    }

    public int getColumnId() {
        return columnId;
    }

    public String getColumnName() {
        return columnName;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public Object transform(Object object){
        switch (this.valueType){
            case STRING: return object.toString();
            case INTEGER: return Integer.valueOf((int) (double) object);
            case LONG: return Long.valueOf((long) (double) object);
            case DOUBLE: return Double.valueOf((double) object);
            case ARRAY:
                if(object instanceof List){
                    List list = (List) object;
                    if(list.size() == 1 && list.get(0) instanceof List){
                        return list.get(0);
                    }
                }
                return object;
        }
        return object;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();

        map.put("id", this.columnId);
        map.put("name", this.columnName);
        map.put("valueType", this.valueType);

        return map;
    }

    @Override
    public String toString() {
        return "ColumnStructure{" +
                "columnId=" + columnId +
                ", columnName='" + columnName + '\'' +
                ", valueType=" + valueType +
                '}';
    }
}
