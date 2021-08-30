package fr.naruse.servermanager.core.database.structure;

import java.util.*;

public class TableStructure {

    private final Map<String, ColumnStructure> columnStructureMap = new HashMap<>();

    public TableStructure registerColumnStructure(ColumnStructure columnStructure){
        this.columnStructureMap.put(columnStructure.getColumnName(), columnStructure);
        return this;
    }

    public ColumnStructure getColumnStructure(int index){
        return new ArrayList<>(this.columnStructureMap.values()).get(index);
    }

    public ColumnStructure getColumnStructure(String columnName){
        return this.columnStructureMap.get(columnName);
    }

    public List<String> getAllNames(){
        return new ArrayList<>(this.columnStructureMap.keySet());
    }

    public List<ColumnStructure> getAllColumnStructure(){
        return new ArrayList<>(this.columnStructureMap.values());
    }

    public List<Map<String, Object>> serialize(){
        List<Map<String, Object>> list = new ArrayList<>();

        for (ColumnStructure columnStructure : new HashSet<>(columnStructureMap.values())) {
            list.add(columnStructure.serialize());
        }

        return list;
    }
}
